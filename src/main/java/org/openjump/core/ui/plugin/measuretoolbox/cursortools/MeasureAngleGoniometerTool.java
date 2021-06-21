package org.openjump.core.ui.plugin.measuretoolbox.cursortools;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.openjump.core.geomutils.GeoUtils;
import org.openjump.core.ui.plugin.measuretoolbox.icons.IconLoader;
//import org.openjump.core.ui.plugin.measuretoolbox.language.I18NPlug;
import org.openjump.core.ui.plugin.measuretoolbox.plugins.ToolboxMeasurePlugIn;
import org.openjump.core.ui.plugin.measuretoolbox.utils.CoordinateListMetrics_extended;
import org.openjump.core.ui.plugin.measuretoolbox.utils.Measure_MultiClickArcTool;
import org.openjump.core.ui.plugin.measuretoolbox.utils.MeasurementLayerFinder;
import org.openjump.core.ui.plugin.measuretoolbox.utils.UnitConverter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.valid.IsValidOp;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


/**
 * measures angle by goniometer;
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class MeasureAngleGoniometerTool extends Measure_MultiClickArcTool {

  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasureTools.Goniometer");
  public static final Icon ICON = IconLoader
      .icon("Ruler_goniometer.gif");

  UnitConverter unitConverter = new UnitConverter();
  double angle;
  double arc;
  double radius;

  PlugInContext context;

  public MeasureAngleGoniometerTool(PlugInContext context) {
    drawClosed = true;
    this.context = context;
    setStroke(new BasicStroke(2));
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


  @Override
  public Cursor getCursor() {
    return createCursor(IconLoader
        .icon("RulerCursor_goniometer.gif").getImage());
  }

  @Override
  protected void gestureFinished() {
    reportNothingToUndoYet();

    Geometry measureGeometry = null;
    Coordinate c = getCoordinates().get(0);
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
    display(getCoordinates(), getPanel());
    // getMetrics().displayMetrics(getCoordinates(), getPanel(),true);
    getPanel().setViewportInitialized(true);
  }


  /*
   * Change only NewCoordinateListMetric And "TXT" attribute
   */
  private Feature toFeature_angle(Geometry measureGeometry,
                                  FeatureSchema schema) {
    angle = Math.round(CoordinateListMetrics_extended
        .angleGoniometer(getCoordinates()) * 10000) / 10000.0;
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
    feature.setAttribute("TYPE", "Angle");
    feature.setAttribute("TEXT", +angle

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

  protected Point getPoint() {
    return new GeometryFactory().createPoint(getCoordinates()
        .get(0));
  }

  private Feature toFeature_line(Geometry measureGeometry,
                                 FeatureSchema schema) {
    Feature feature = new BasicFeature(MeasurementLayerFinder.measureLayer(context)
        .getFeatureCollectionWrapper().getFeatureSchema());
    feature.setGeometry(measureGeometry);
    feature.setAttribute("TYPE", "Angle");
    feature.setAttribute("GEOM", getLineString());
    return feature;
  }


  protected LineString getLineString() {
    List<Coordinate> points = new ArrayList<>(getCoordinates());
    if (points.size() > 1) {

      Coordinate a = points.get(0);
      Coordinate b = points.get(1);

      Point point = new GeometryFactory().createPoint(new Coordinate(GeoUtils.rotPt(
          b, a, fullAngle)));
      Coordinate d = point.getCoordinate();

      return new GeometryFactory().createLineString(new Coordinate[]{b,
          a, d});
    }
    return null;
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


  protected boolean checkArc() {
    if (getCoordinates().size() < 3) {
      getPanel().getContext().warnUser("Goniometer must have 3 points");

      return false;
    }

    IsValidOp isValidOp = new IsValidOp(getLineString());

    if (!isValidOp.isValid()) {
      getPanel().getContext().warnUser(
          isValidOp.getValidationError().getMessage());

      if (getWorkbench().getBlackboard().get(
          EditTransaction.ROLLING_BACK_INVALID_EDITS_KEY, false)) {
        return false;
      }
    }

    return true;
  }


  private void display(List<Coordinate> coordinates, LayerViewPanel panel) {
    String distString;

    angle = Math.round(CoordinateListMetrics_extended.angleGoniometer(coordinates) * 10000) / 10000.0;

    double DD = Math.floor(angle);
    double MMAngle = ((angle - DD) * 60 * 100) / 100D;
    double MM = Math.floor(MMAngle);
    double SSAngle = ((MMAngle - MM) * 60 * 100) / 100D;
    double SS = Math.round(SSAngle);

    Coordinate a = coordinates.get(0);
    Coordinate b = coordinates.get(1);
    radius = CoordinateListMetrics_extended.distanceBetweenAB(a, b);
    arc = radius * Math.toRadians(angle);
    if (angle > 180)
      arc = radius * Math.toRadians(360 - angle);

    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      distString = "(Geographics) - ";

    } else {
      distString = "(Projected) - ";

    }
    String measureUnit = ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString();
    String mapUnit = ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString();
    if (mapUnit.equals(measureUnit)) {
      distString += sRadius + ": " + unitConverter.decimalformat(radius) + unitConverter.mapLengthUnit() + "  ";
      if (getCoordinates().size() > 1) {
        distString += sArc + ": " + unitConverter.decimalformat(arc) + unitConverter.mapLengthUnit() + "  ";
        distString += sAngle + ": " + unitConverter.decimalformat(angle) + "°" +
            " (" + panel.format(DD) + "° " + panel.format(MM) + "' " + panel.format(SS) + "\")";
      }
    } else {
      distString += sRadius + ": " + unitConverter.decimalformat(radius) + unitConverter.mapLengthUnit()
          + " [" + unitConverter.decimalformat(radius * unitConverter.lengthConverter())
          + unitConverter.measuredLengthUnit() + "]  ";
      if (getCoordinates().size() > 1) {
        distString += sArc + ": " + unitConverter.decimalformat(arc) + unitConverter.mapLengthUnit()
            + " [" + unitConverter.decimalformat(arc * unitConverter.lengthConverter())
            + unitConverter.measuredLengthUnit() + "]  ";
        distString += sAngle + ": " + unitConverter.decimalformat(angle) + "°" +
            " (" + panel.format(DD) + "° " + panel.format(MM) + "' " + panel.format(SS) + "\")";
      }
    }
    panel.getContext().setStatusMessage(distString);
  }

}

 


