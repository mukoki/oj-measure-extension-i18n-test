package org.openjump.core.ui.plugin.measuretoolbox.cursortools;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


/**
 * measures angle;
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class MeasureAngleTool extends Measure_NClickTool {

  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasureTools.Angle_between_two_segments");
  public static final Icon ICON = IconLoader
      .icon("Ruler_angle.gif");

  UnitConverter unitConverter = new UnitConverter();
  double angle;

  @Override
  public Cursor getCursor() {
    return createCursor(IconLoader.icon("RulerCursor_angle.gif").getImage());
  }

  PlugInContext context;

  public MeasureAngleTool(PlugInContext context) {
    super(3);
    this.context = context;
    setStroke(new BasicStroke(2));
    allowSnapping();
    //  setMetricsDisplay(new CoordinateListMetrics_extended());
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
    getPanel().setViewportInitialized(true);
  }


  private Feature toFeature_angle(Geometry measureGeometry,
                                  FeatureSchema schema) {

    angle = Math.round(CoordinateListMetrics_extended.angle(getCoordinates()) * 10000) / 10000.0;
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

  /*
   * Change only the "TYPE
   */
  private Feature toFeature_line(Geometry measureGeometry,
                                 FeatureSchema schema) {
    Feature feature = new BasicFeature(MeasurementLayerFinder.measureLayer(context)
        .getFeatureCollectionWrapper().getFeatureSchema());
    feature.setGeometry(measureGeometry);
    feature.setAttribute("TYPE", "Angle");
    feature.setAttribute("GEOM", getLineString());
    return feature;
  }

  public Point getPoint() {
    return new GeometryFactory().createPoint(getCoordinates()
        .get(1));
  }

  protected LineString getLineString() {
    Iterator<?> it = getCoordinates().iterator();
    Coordinate c1 = null;
    Coordinate c2 = null;
    Coordinate c3 = null;
    if (it.hasNext())
      c1 = (Coordinate) it.next();
    if (it.hasNext())
      c2 = (Coordinate) it.next();
    if (it.hasNext())
      c3 = (Coordinate) it.next();
    return new GeometryFactory().createLineString(new Coordinate[]{c1,
        c2, c3});
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

    angle = Math.round(CoordinateListMetrics_extended.angle(coordinates) * 10000) / 10000.0;

    double DD = Math.floor(angle);
    double MMAngle = ((angle - DD) * 60 * 100) / 100D;
    double MM = Math.floor(MMAngle);
    double SSAngle = ((MMAngle - MM) * 60 * 100) / 100D;
    double SS = Math.round(SSAngle);
    double length1;
    double length2;
    Coordinate a = coordinates.get(0);
    Coordinate b = coordinates.get(1);
    length1 = CoordinateListMetrics_extended.distanceBetweenAB(a, b);
    length2 = 0.0D;
    if (getCoordinates().size() > 1) {
      Coordinate c = coordinates.get(2);
      length2 = CoordinateListMetrics_extended.distanceBetweenAB(c, b);
    }
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      distString = "(Geographics) - ";

    } else {
      distString = "(Projected) - ";
    }
    String measureUnit = ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString();
    String mapUnit = ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString();
    if (mapUnit.equals(measureUnit)) {
      distString += sLength + ": " + unitConverter.decimalformat(length1) + "/" + unitConverter.decimalformat(length2) + unitConverter.mapLengthUnit();
    } else {
      distString += sLength + ": " + unitConverter.decimalformat(length1) + "/" + unitConverter.decimalformat(length2) + unitConverter.mapLengthUnit()
          + " [" + unitConverter.decimalformat(length1 * unitConverter.lengthConverter()) + "/" + unitConverter.decimalformat(length2 * unitConverter.lengthConverter()) + unitConverter.measuredLengthUnit() + "]";
    }
    distString += "   " + sAngle + ": " + unitConverter.decimalformat(angle) + "°" +
        " (" + panel.format(DD) + "° " + panel.format(MM) + "' " + panel.format(SS) + "\")";

    panel.getContext().setStatusMessage(distString);
  }
}

