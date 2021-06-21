package org.openjump.core.ui.plugin.measuretoolbox.cursortools;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.openjump.core.ui.plugin.measuretoolbox.icons.IconLoader;
import org.openjump.core.ui.plugin.measuretoolbox.plugins.ToolboxMeasurePlugIn;
import org.openjump.core.ui.plugin.measuretoolbox.utils.CoordinateListMetrics_extended;


import org.openjump.core.ui.plugin.measuretoolbox.utils.MeasurementLayerFinder;
import org.openjump.core.ui.plugin.measuretoolbox.utils.UnitConverter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


public class MeasureRectangleTool extends DragTool {

  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasureTools.Area");
  public static final Icon ICON = IconLoader
      .icon("Ruler_polygon.gif");

  public static String sArea = I18NPlug
      .get("MeasureToolbox.Area");
  public static String sPerimeter = I18NPlug
      .get("MeasureToolbox.Perimeter");
  public static final String ERROR = I18NPlug
      .get("MeasureToolbox.error_geometry");


  double area;
  double perimeter;
  //double length;
  //double distance;
  UnitConverter unitConverter = new UnitConverter();

  PlugInContext context;

  public Cursor getCursor() {
    return createCursor(IconLoader.icon("RulerCursor_area.gif").getImage());
  }

  public MeasureRectangleTool(PlugInContext context) {
    this.context = context;

    setStroke(new BasicStroke(2));
    allowSnapping();
  }



  public Icon getIcon() {
    return ICON;
  }

  public String getName() {
    return NAME;
  }

  protected void gestureFinished() {
    reportNothingToUndoYet();
    Geometry measureGeometry = null;
    Coordinate c = modelSource;

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

      display(getRectangleCoordinates(), getPanel());

    } else {

      JOptionPane
          .showMessageDialog(
              null,
              ERROR,
              null,
              JOptionPane.INFORMATION_MESSAGE);
    }


  }


  /*
   * Change only NewCoordinateListMetric And "TXT" attribute
   */
  public CoordinateList getRectangleCoordinates() {
    Envelope e = new Envelope(getModelSource().x, getModelDestination().x,
        getModelSource().y, getModelDestination().y);
    Coordinate a = new Coordinate(e.getMinX(), e.getMinY());
    Coordinate b = new Coordinate(e.getMinX(), e.getMaxY());
    Coordinate c = new Coordinate(e.getMaxX(), e.getMaxY());
    Coordinate d = new Coordinate(e.getMaxX(), e.getMinY());

    CoordinateList coordinates = new CoordinateList();

    coordinates.add(a);
    coordinates.add(b);
    coordinates.add(c);
    coordinates.add(d);
    return coordinates;
  }


  private Feature toFeature(Geometry measureGeometry,
                            FeatureSchema schema) {

    area = Math.round(CoordinateListMetrics_extended
        .area(getRectangleCoordinates()) * 100) / 100.0D * unitConverter.areaConverter();
    perimeter = Math.round(CoordinateListMetrics_extended
        .distance(getRectangleCoordinates(), true) * 10000)
        / 10000.0 * unitConverter.lengthConverter();


    Feature feature = new BasicFeature(MeasurementLayerFinder.measureLayer(context)
        .getFeatureCollectionWrapper().getFeatureSchema());

    feature.setGeometry(measureGeometry);
    feature.setAttribute("TYPE", "Area");
    feature.setAttribute("TEXT", unitConverter.decimalformat(area) + unitConverter.measuredAreaUnit()
        + " - " + unitConverter.decimalformat(perimeter) + unitConverter.measuredLengthUnit());
    feature.setAttribute("LENGHT", perimeter);
    feature.setAttribute("AREA", area);
    feature.setAttribute("GEOM", getPolygon());
    return feature;
  }

  protected Polygon getPolygon() {

    Envelope e = new Envelope(getModelSource().x, getModelDestination().x,
        getModelSource().y, getModelDestination().y);

    return new GeometryFactory().createPolygon(
        new GeometryFactory().createLinearRing(new Coordinate[]{
            new Coordinate(e.getMinX(), e.getMinY()),
            new Coordinate(e.getMinX(), e.getMaxY()),
            new Coordinate(e.getMaxX(), e.getMaxY()),
            new Coordinate(e.getMaxX(), e.getMinY()),
            new Coordinate(e.getMinX(), e.getMinY())}), null);
  }

  public void mouseLocationChanged(MouseEvent e) {
    try {
      if (isShapeOnScreen()) {
        List<Coordinate> currentCoordinates = new ArrayList<>(getRectangleCoordinates());
        currentCoordinates.add(getPanel().getViewport().toModelCoordinate(e.getPoint()));
        display(currentCoordinates, getPanel());
        // getPanel().getContext().setStatusMessage("");
      }
      snap(e.getPoint());
      //  super.mousePressed(e);
      super.mouseDragged(e);


    } catch (Throwable t) {
      getPanel().getContext().handleThrowable(t);
    }
  }

  private void display(List<Coordinate> coordinates, LayerViewPanel panel) {
    String distString;


    area = Math.round(CoordinateListMetrics_extended
        .area(coordinates) * 100) / 100.0D;
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
      distString += sArea + ": " + unitConverter.decimalformat(area) + unitConverter.mapAreaUnit();
      distString += "   " + sPerimeter + ": " + unitConverter.decimalformat(perimeter) + unitConverter.mapLengthUnit();
    } else {

      distString += sArea + ": " + unitConverter.decimalformat(area) + unitConverter.mapAreaUnit()
          + " [" + unitConverter.decimalformat(area * unitConverter.areaConverter()) + unitConverter.measuredAreaUnit() + "]";
      distString += "   " + sPerimeter + ": " + unitConverter.decimalformat(perimeter) + unitConverter.mapLengthUnit()
          + " [" + unitConverter.decimalformat(perimeter * unitConverter.lengthConverter()) + unitConverter.measuredLengthUnit() + "]";

    }
    panel.getContext().setStatusMessage(distString);
  }
}