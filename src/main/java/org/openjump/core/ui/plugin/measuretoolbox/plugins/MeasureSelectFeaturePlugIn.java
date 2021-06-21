package org.openjump.core.ui.plugin.measuretoolbox.plugins;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.openjump.core.ui.plugin.measuretoolbox.icons.IconLoader;
//import org.openjump.core.ui.plugin.measuretoolbox.language.I18NPlug;
import org.openjump.core.ui.plugin.measuretoolbox.utils.CoordinateListMetrics_extended;
import org.openjump.core.ui.plugin.measuretoolbox.utils.UnitConverter;
import org.openjump.core.ui.plugin.measuretoolbox.utils.MeasurementLayerFinder;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;

import de.latlon.deejump.plugin.style.CircleVertexStyle;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


/**
 * Computes:
 * length (Linestring);
 * area and perimeter (Polygons and MultiPolygons)
 * coordinates (Points)
 * of selected geometries
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class MeasureSelectFeaturePlugIn extends AbstractPlugIn {

  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasurePlugin.MeasureSelectedFeature.name");

  public static final Icon ICON = IconLoader.icon("Ruler_measurement.gif");
  //public static final ImageIcon ICON2 = IconLoader.icon("Ruler_select.gif");

  //Geometry measureGeometry = null;
  double area;
  //double distance;
  //double perimeter;
  UnitConverter unitConverter = new UnitConverter();

  @Override
  public void initialize(PlugInContext context) {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    FeatureInstaller featureInstaller = new FeatureInstaller(
        workbenchContext);
    JPopupMenu popupMenu = LayerViewPanel.popupMenu();
    featureInstaller.addPopupMenuPlugin(popupMenu, this, getName(), false,
        null, // to do: add icon
        createEnableCheck(workbenchContext));
  }

  @Override
  public boolean execute(PlugInContext context) throws Exception {
    reportNothingToUndoYet(context);
    SelectionManager manager = context.getLayerViewPanel()
        .getSelectionManager();
    Collection<Feature> features = manager.getFeaturesWithSelectedItems();
    int totalGeometries;
    totalGeometries = manager.getFeaturesWithSelectedItemsCount();

    if (totalGeometries == 0) {

      JOptionPane
          .showMessageDialog(
              null,
              I18NPlug.get("MeasureToolbox.MeasurePlugin.MeasureSelectedFeature.message1"),
              null, JOptionPane.INFORMATION_MESSAGE);
      context.getLayerViewPanel().setCurrentCursorTool(
          new SelectFeaturesTool());

      return false;
    }

    for (Feature feat : features) {
      Coordinate c = feat.getGeometry().getCentroid().getCoordinate();

      if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
        if (c.y < -90.0 || c.y > 90.0 || c.x < -180.0 || c.x > 180.0) {
          JOptionPane
              .showMessageDialog(
                  null,
                  I18NPlug
                      .get("MeasureToolbox.geodesy-warning"),
                  I18NPlug.get("MeasureToolbox.error"),
                  JOptionPane.ERROR_MESSAGE);
          return false;
        }

      }
      Geometry geom = feat.getGeometry();
      if (geom instanceof GeometryCollection
          || geom instanceof MultiPolygon
          || geom instanceof MultiLineString
          || geom instanceof MultiPoint) {

        JOptionPane
            .showMessageDialog(
                null,
                geom.getGeometryType()
                    + ": "
                    + I18NPlug
                    .get("MeasureToolbox.MeasurePlugin.MeasureSelectedFeature.message3"),
                null, JOptionPane.INFORMATION_MESSAGE);
        return false;
      }

      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(
          toFeature(feat, MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper()
              .getFeatureSchema(), context));

      manager.clear();
    }
    return true;
  }

  /*
   * Change only NewCoordinateListMetric And "TXT" attribute
   */
  private Feature toFeature(Feature feat, FeatureSchema schema,
                            PlugInContext context) {

    Coordinate[] coord = feat.getGeometry().getCoordinates();

    CoordinateList list = new CoordinateList();
    list.add(coord, false);
    // formatting area and length values
    DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
        .getInstance();
    String formatPattern;
    // adaptive format pattern, thanks to Michaël Michaud for his idea!
    double length = area, distance, perimeter;
    if (length >= 10) {
      formatPattern = "#,##0.00";
    } else if (length >= 1) {
      formatPattern = "#,##0.000";
    } else if (length >= 0.1) {
      formatPattern = "#,##0.0000";
    } else if (length >= 0.01) {
      formatPattern = "#,##0.00000";
    } else
      formatPattern = "#,##0.000000";
    decimalFormat.applyPattern(formatPattern);

      
/*        if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
            area = new Double(new Long(Math.round(CoordinateListMetrics_extended
                    .area_geographic(list) * 100)).doubleValue() / 100.0D)*mum.areaConverter();

            distance = new Double(
                    new Long(Math.round(CoordinateListMetrics_extended
                            .distance_geographic(list) * 100)).doubleValue() / 100.0D)*mum.lengthConverter();
            perimeter = new Double(
                    new Long(Math.round(CoordinateListMetrics_extended
                            .perimeter_geographic(list) * 100)).doubleValue() / 100.0D)*mum.lengthConverter();

        } else {*/
    area = Math.round(CoordinateListMetrics_extended
        .area(list) * 10000) / 10000.0 * unitConverter.areaConverter();

    distance = Math.round(CoordinateListMetrics_extended
        .distance(list, false) * 10000)
        / 10000.0 * unitConverter.lengthConverter();
    perimeter = Math.round(CoordinateListMetrics_extended.distance(list,
        true) * 10000) / 10000.0 * unitConverter.lengthConverter();
    //       }
    Feature feature = new BasicFeature(
        measureLayer(context).getFeatureCollectionWrapper()
            .getFeatureSchema());

    Geometry geom = feat.getGeometry();

    if (geom instanceof LineString) {

      feature.setAttribute("TYPE", "Length");
      feature.setAttribute("TEXT", decimalFormat.format(distance) + unitConverter.measuredLengthUnit());
      feature.setAttribute("LENGTH", distance);
      feature.setAttribute("AREA", area);
      feature.setAttribute("UNIT", unitConverter.measureUnit());

    } else if (geom instanceof Polygon || geom instanceof MultiPolygon) {
      feature.setAttribute("TYPE", "Area");
      feature.setAttribute("TEXT", decimalFormat.format(area)
          + unitConverter.measuredAreaUnit() + " - " + decimalFormat.format(perimeter)
          + unitConverter.measuredLengthUnit());
      feature.setAttribute("LENGTH", perimeter);
      feature.setAttribute("AREA", area);
      feature.setAttribute("UNIT", unitConverter.measureUnit());

    } else if (geom instanceof Point) {
      Point p = feat.getGeometry().getCentroid();
      double coordX = Math.round(p.getX() * 10000) / 10000.0;
      double coordY = Math.round(p.getY() * 10000) / 10000.0;
      feature.setAttribute("TYPE", "XY");
      feature.setAttribute("TEXT", coordX + " - " + coordY);
      feature.setAttribute("X", decimalFormat.format(coordX));
      feature.setAttribute("Y", decimalFormat.format(coordY));

    } else if (geom instanceof GeometryCollection
        || geom instanceof MultiLineString
        || geom instanceof MultiPoint) {

      JOptionPane
          .showMessageDialog(
              null,
              geom.getGeometryType()
                  + ": "
                  + I18NPlug
                  .get("MeasureToolbox.MeasurePlugin.MeasureSelectedFeature.message3"),
              null, JOptionPane.INFORMATION_MESSAGE);
      return null;
    }
    feature.setGeometry(feat.getGeometry());
    feature.setAttribute("GEOM", feat.getGeometry());

    return feature;
  }

  public static MultiEnableCheck createEnableCheck(
      WorkbenchContext workbenchContext) {
    EnableCheckFactory checkFactory = new EnableCheckFactory(
        workbenchContext);

    return new MultiEnableCheck()
        .add(checkFactory
            .createWindowWithSelectionManagerMustBeActiveCheck())
        .add(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(1))
        .add(checkFactory
            .createExactlyOneSelectedLayerMustBeEditableCheck());
  }

  public Icon getIcon() {
    return ICON;
  }

  @Override
  public String getName() {
    return NAME;
  }


  private Collection<Feature> getFeaturesToProcess(Layer lyr, PlugInContext context) {

    return context.getLayerViewPanel()
        .getSelectionManager().getFeaturesWithSelectedItems(lyr);

  }

  public static final String LAYER_NAME = I18NPlug
      .get("MeasureToolbox.layer");

  public static Layer measureLayer(PlugInContext context) {
    Layer measureLayer = context.getLayerManager().getLayer(LAYER_NAME);

    if (measureLayer != null) {
      return measureLayer;
    }
    FeatureSchema schema = new FeatureSchema();
    schema.addAttribute("TYPE", AttributeType.STRING);
    schema.addAttribute("TEXT", AttributeType.STRING);
    schema.addAttribute("DEGREE", AttributeType.DOUBLE);
    schema.addAttribute("DDMMSS", AttributeType.DOUBLE);
    schema.addAttribute("AREA", AttributeType.DOUBLE);
    schema.addAttribute("LENGHT", AttributeType.DOUBLE);
    schema.addAttribute("X", AttributeType.DOUBLE);
    schema.addAttribute("Y", AttributeType.DOUBLE);
    schema.addAttribute("GEOM", AttributeType.GEOMETRY);

    new FeatureSchema();
    new FeatureDataset(schema);

    FeatureCollection featureCollection = new FeatureDataset(schema);

    boolean firingEvents = context.getLayerManager().isFiringEvents();
    context.getLayerManager().setFiringEvents(false);
    Layer layer;
    try {
      layer = new Layer(LAYER_NAME, Color.red, featureCollection,
          context.getLayerManager());

      layer.removeStyle(layer.getVertexStyle());
      layer.addStyle(new CircleVertexStyle());
      layer.getBasicStyle().setLineColor(Color.red);

      layer.getBasicStyle().setFillColor(Color.pink);
      layer.getBasicStyle().setLineWidth(2);
      layer.getBasicStyle().setAlpha(128);
      layer.getBasicStyle().setRenderingLine(true);
      layer.getBasicStyle().setRenderingFill(true);
      layer.getVertexStyle().setEnabled(true);
      layer.getVertexStyle().setSize(0);
      layer.setDrawingLast(true);
      LabelStyle labelStyle = layer.getLabelStyle();
      labelStyle.setAttribute("TEXT");
      labelStyle.setVerticalAlignment("ON_LINE");
      labelStyle.setHorizontalAlignment(0);
      labelStyle.setEnabled(true);
      labelStyle.setColor(Color.black);
      labelStyle.setHeight(16.0D);
      labelStyle.setOutlineShowing(true);
      labelStyle.setOutlineColor(Color.white);
      labelStyle.setHidingOverlappingLabels(false);
      labelStyle.setFont(layer.getLabelStyle().getFont()
          .deriveFont(Font.BOLD, 14.0F));
      labelStyle.setHideAtScale(false);
      layer.setDrawingLast(true);
    } finally {
      context.getLayerManager().setFiringEvents(firingEvents);
    }

    context.getLayerManager().addLayer(StandardCategoryNames.SYSTEM, layer);

    return layer;
  }


}
