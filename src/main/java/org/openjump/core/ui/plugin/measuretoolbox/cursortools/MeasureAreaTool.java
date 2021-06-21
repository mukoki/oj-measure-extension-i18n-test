package org.openjump.core.ui.plugin.measuretoolbox.cursortools;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.openjump.core.ui.plugin.measuretoolbox.icons.IconLoader;
//import org.openjump.core.ui.plugin.measuretoolbox.language.I18NPlug;
import org.openjump.core.ui.plugin.measuretoolbox.plugins.ToolboxMeasurePlugIn;
import org.openjump.core.ui.plugin.measuretoolbox.utils.CoordinateListMetrics_extended;
import org.openjump.core.ui.plugin.measuretoolbox.utils.MeasurementLayerFinder;
import org.openjump.core.ui.plugin.measuretoolbox.utils.UnitConverter;
import org.openjump.core.ui.plugin.measuretoolbox.utils.Measure_MultiClickTool;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


/**
 * measures area;
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class MeasureAreaTool extends Measure_MultiClickTool {


  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasureTools.Area");
  public static final Icon ICON = IconLoader
      .icon("Ruler_polygon.gif");


  double area;
  double perimeter;
  double length;
  UnitConverter unitConverter = new UnitConverter();

  @Override
  public Cursor getCursor() {
    return createCursor(IconLoader.icon("RulerCursor_area.gif").getImage());
  }

  PlugInContext context;

  public MeasureAreaTool(PlugInContext context) {
    this.context = context;
    setCloseRing(true);
    setStroke(new BasicStroke(2));
    //setFilling (true);

    allowSnapping();

  }


  @Override
  public Icon getIcon() {
    return ICON;
  }

  @Override
  public String getName() {
    return NAME;
  }

  protected void gestureFinished() {
    reportNothingToUndoYet();
    Geometry measureGeometry = null;
    Coordinate c = coordinates.get(0);

    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      if (c.y < -90.0 || c.y > 90.0 || c.x < -180.0 || c.x > 180.0) {
        JOptionPane
            .showMessageDialog(
                null,
                I18NPlug
                    .get("MeasureToolbox.geodesy-warning"),
                I18NPlug.get("MeasureToolbox.error"),
                JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    Feature feat =
        toFeature(measureGeometry, MeasurementLayerFinder.measureLayer(context)
            .getFeatureCollectionWrapper().getFeatureSchema());

    if (feat.getGeometry().isValid()) {
      if (ToolboxMeasurePlugIn.saveCheck.isSelected()) {
        MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(feat);
      } else {
        MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().clear();
        MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(feat);
      }
      display(getCoordinates(), getPanel());

    } else {

      JOptionPane
          .showMessageDialog(
              null,
              ERROR,
              null,
              JOptionPane.INFORMATION_MESSAGE);
    }


  }


  private Feature toFeature(Geometry measureGeometry,
                            FeatureSchema schema) {
    area = Math.round(CoordinateListMetrics_extended
        .area(getCoordinates()) * 100) / 100.0D * unitConverter.areaConverter();
    perimeter = Math.round(CoordinateListMetrics_extended
        .distance(getCoordinates(), true) * 10000)
         / 10000.0 * unitConverter.lengthConverter();
    Feature feature = new BasicFeature(MeasurementLayerFinder.measureLayer(context)
        .getFeatureCollectionWrapper().getFeatureSchema());

    feature.setGeometry(measureGeometry);
    feature.setAttribute("TYPE", "Area");
    feature.setAttribute("TEXT", unitConverter.decimalformat(area) + unitConverter.measuredAreaUnit()
        + " - " + unitConverter.decimalformat(perimeter) + unitConverter.measuredLengthUnit());
    feature.setAttribute("LENGHT", perimeter);
    feature.setAttribute("AREA", area);
    feature.setAttribute("UNIT", unitConverter.measureUnit());
    feature.setAttribute("GEOM", getPolygon());
    return feature;
  }

  protected Polygon getPolygon() {
    List<Coordinate> closedPoints = new ArrayList<>(getCoordinates());
    if (!closedPoints.get(0).equals(
        closedPoints.get(closedPoints.size() - 1))) {
      closedPoints.add(new Coordinate(closedPoints.get(0)));
    }
    return new GeometryFactory().createPolygon(
        new GeometryFactory().createLinearRing(toArray(closedPoints)),
        null);
  }

  public void mouseLocationChanged(MouseEvent e) {
    try {
      if (isShapeOnScreen()) {
        List<Coordinate> currentCoordinates = new ArrayList<>(getCoordinates());
        currentCoordinates.add(getPanel().getViewport().toModelCoordinate(e.getPoint()));
        display(currentCoordinates, getPanel());

      }
      snap(e.getPoint());
      super.mouseLocationChanged(e);

    } catch (Throwable t) {
      getPanel().getContext().handleThrowable(t);
    }
  }

  private void display(List<Coordinate> coordinates, LayerViewPanel panel) {
    String distString;

    area = Math.round(CoordinateListMetrics_extended
        .area(coordinates) * 100) / 100.0D;
    length = Math.round(CoordinateListMetrics_extended
        .distance(coordinates, false) * 100) / 100.0D;
    perimeter = Math.round(CoordinateListMetrics_extended
        .distance(coordinates, true) * 100) / 100.0D;


    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      distString = "(Geographics) - ";
    } else {
      distString = "(Projected) - ";
    }

    String measureUnit = ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString();
    String mapUnit = ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString();

    if (mapUnit.equals(measureUnit)) {
      if (getCoordinates().size() > 1) {
        distString += sArea + ": " + unitConverter.decimalformat(area) + unitConverter.mapAreaUnit();
        distString += "   " + sPerimeter + ": " + unitConverter.decimalformat(perimeter) + unitConverter.mapLengthUnit();
      } else {
        distString += sLength + ": " + unitConverter.decimalformat(length) + unitConverter.mapLengthUnit();
      }
    } else {
      if (getCoordinates().size() > 1) {
        distString += sArea + ": " + unitConverter.decimalformat(area) + unitConverter.mapAreaUnit()
            + " [" + unitConverter.decimalformat(area * unitConverter.areaConverter()) + unitConverter.measuredAreaUnit() + "]";
        distString += "   " + sPerimeter + ": " + unitConverter.decimalformat(perimeter) + unitConverter.mapLengthUnit()
            + " [" + unitConverter.decimalformat(perimeter * unitConverter.lengthConverter()) + unitConverter.measuredLengthUnit() + "]";
      } else {
        distString += sLength + ": "
            + unitConverter.decimalformat(length) + unitConverter.mapLengthUnit()
            + " [" + unitConverter.decimalformat(length * unitConverter.lengthConverter()) + unitConverter.measuredLengthUnit() + "]";
      }
    }
    panel.getContext().setStatusMessage(distString);
  }
}
