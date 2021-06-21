package org.openjump.core.ui.plugin.measuretoolbox.cursortools;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.openjump.core.ui.plugin.measuretoolbox.icons.IconLoader;
//import org.openjump.core.ui.plugin.measuretoolbox.language.I18NPlug;
import org.openjump.core.ui.plugin.measuretoolbox.plugins.ToolboxMeasurePlugIn;
import org.openjump.core.ui.plugin.measuretoolbox.utils.CoordinateListMetrics_extended;
import org.openjump.core.ui.plugin.measuretoolbox.utils.Measure_NClickTool;
import org.openjump.core.ui.plugin.measuretoolbox.utils.MeasurementLayerFinder;
import org.openjump.core.ui.plugin.measuretoolbox.utils.UnitConverter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


public class MeasureAzimuthTool extends Measure_NClickTool {

  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasureTools.Azimuth");
  public static final Icon ICON = IconLoader
      .icon("compass.png");

  UnitConverter unitConverter = new UnitConverter();

  @Override
  public Cursor getCursor() {
    return createCursor(IconLoader.icon("RulerCursor_azimuth.gif")
        .getImage());
  }

  PlugInContext context;

  public MeasureAzimuthTool(PlugInContext context) {
    super(2);
    this.context = context;
    setStroke(new BasicStroke(2));
    //  setMetricsDisplay(new CoordinateListMetrics_extended());
    allowSnapping();
  }

  double angle;
  //double length;

  @Override
  public Icon getIcon() {
    return ICON;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected void gestureFinished() {
    reportNothingToUndoYet();

    Geometry measureGeometry = null;

    Coordinate c = getPoint().getCoordinate();
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

    Feature feat_angle =
        toFeature_angle(measureGeometry, MeasurementLayerFinder.measureLayer(context)
            .getFeatureCollectionWrapper().getFeatureSchema());
    Feature feat_line =
        toFeature_line(measureGeometry, MeasurementLayerFinder.measureLayer(context)
            .getFeatureCollectionWrapper().getFeatureSchema());

    if (ToolboxMeasurePlugIn.saveCheck.isSelected()) {
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(feat_angle);
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(feat_line);

    } else {
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().clear();
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(feat_angle);
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(feat_line);

    }

  }

  /*
   * End do the common part
   */

  /*
   * Change only NewCoordinateListMetric And "TXT" attribute
   */

  private Feature toFeature_angle(Geometry measureGeometry,
                                  FeatureSchema schema) {

    angle = Math.round(CoordinateListMetrics_extended
        .azimuth(getCoordinates()) * 10000) / 10000.0;

    double coordX = Math.round(getPoint().getX() * 10000) / 10000.0;
    double coordY = Math.round(getPoint().getY() * 10000) / 10000.0;


    // Transform Decimal Angle in Degrees-Minutes-Seconds//
    double DD = Math.floor(angle);
    double MMAngle = ((angle - DD) * 60 * 100) / 100D;
    double MM = Math.floor(MMAngle);
    double SSAngle = ((MMAngle - MM) * 60 * 100) / 100D;
    double SS = Math.round(SSAngle);

    Feature feature = new BasicFeature(MeasurementLayerFinder.measureLayer(context)
        .getFeatureCollectionWrapper().getFeatureSchema());
    feature.setGeometry(measureGeometry);
    feature.setAttribute("TYPE", "Azimuth");
    feature.setAttribute("TEXT", "N " + angle
        // format.format(getDegreesAzimuth())
        + "° ");
    feature.setAttribute("DEGREE", angle);
    feature.setAttribute(
        "DDMMSS",
        panel.format(DD) + "° " + panel.format(MM) + "' "
            + panel.format(SS) + "''");
    feature.setAttribute("X", unitConverter.decimalformat(coordX));
    feature.setAttribute("Y", unitConverter.decimalformat(coordY));
    feature.setAttribute("GEOM", getPoint());

    return feature;
  }

  private Feature toFeature_line(Geometry measureGeometry,
                                 FeatureSchema schema) {
    Feature feature = new BasicFeature(MeasurementLayerFinder.measureLayer(context)
        .getFeatureCollectionWrapper().getFeatureSchema());
    feature.setGeometry(measureGeometry);
    feature.setAttribute("TYPE", "Azimuth");
    feature.setAttribute("GEOM", getLineString());
    return feature;
  }

  protected Point getPoint() {
    return new GeometryFactory().createPoint(getCoordinates()
        .get(0));
  }

  protected LineString getLineString() {
    List<Coordinate> points = new ArrayList<>(getCoordinates());
    if (points.size() > 1) {
      Coordinate a = points.get(0);
      Coordinate b = points.get(1);
      LineSegment ls = new LineSegment(a, b);
      double length = ls.getLength();
      double dy = a.y + length;
      Coordinate c = new Coordinate(a.x, dy);

      return new GeometryFactory().createLineString(new Coordinate[]{c,
          a, b});
    }
    return null;
  }


  protected Shape getShape() throws NoninvertibleTransformException {
    GeneralPath path = new GeneralPath();
    // sometimes the coordinates are empty and we get an
    // IndexOutOfBoundsExeption!
    // we get this if we use this tool, open a menu and then click with the
    // open menu in the map. In this moment we do not get the mousePressed
    // event and no coordinate will be added.
    if (!coordinates.isEmpty()) {
      Coordinate a = coordinates.get(0);
      Point2D firstPoint = getPanel().getViewport().toViewPoint(a);
      path.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());

      Coordinate b = tentativeCoordinate;
      Point2D secondPoint = getPanel().getViewport().toViewPoint(b);
      path.lineTo((float) secondPoint.getX(), (float) secondPoint.getY());
      LineSegment ls = new LineSegment(a, b);
      double length = ls.getLength();
      double dy = a.y + length;
      Coordinate c = new Coordinate(a.x, dy);
      Point2D thirdPoint = getPanel().getViewport().toViewPoint(c);
      path.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());
      path.lineTo((float) thirdPoint.getX(), (float) thirdPoint.getY());


    }
    return path;
  }


  public void mouseLocationChanged(MouseEvent e) {
    try {
      if (isShapeOnScreen()) {
        ArrayList<Coordinate> currentCoordinates = new ArrayList<>(getCoordinates());
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

    angle = Math.round(CoordinateListMetrics_extended.azimuth(coordinates) * 10000)
        / 10000.0;

    double DD = Math.floor(angle);
    double MMAngle = ((angle - DD) * 60 * 100) / 100D;
    double MM = Math.floor(MMAngle);
    double SSAngle = ((MMAngle - MM) * 60 * 100) / 100D;
    double SS = Math.round(SSAngle);

    double length1;


    Coordinate a = coordinates.get(0);
    Coordinate b = coordinates.get(1);

    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      distString = "(Geographics) - ";
      length1 = CoordinateListMetrics_extended.computeGeographicLenght(
          a.y, a.x, b.y, b.x);

    } else {
      distString = "(Projected) - ";
      length1 = a.distance(b);
    }
    String measureUnit = ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString();
    String mapUnit = ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString();

    if (mapUnit.equals(measureUnit)) {
      distString += sLength + ": " + unitConverter.decimalformat(length1) + unitConverter.mapLengthUnit();

    } else {
      distString += sLength + ": " + unitConverter.decimalformat(length1) + unitConverter.mapLengthUnit()
          + " [" + unitConverter.decimalformat(length1 * unitConverter.lengthConverter()) + unitConverter.measuredLengthUnit() + "]";

    }

    distString += "  " + sAngle + ": N " + unitConverter.decimalformat(angle) + "°" +
        " (" + panel.format(DD) + "° " + panel.format(MM)
        + "' " + panel.format(SS) + "\")";
    panel.getContext().setStatusMessage(distString);
  }
}


