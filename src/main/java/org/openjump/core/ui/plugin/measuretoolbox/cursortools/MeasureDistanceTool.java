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
import org.locationtech.jts.geom.LineString;
//import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


/**
 * measures length;
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class MeasureDistanceTool extends Measure_MultiClickTool {


  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasureTools.Distance");
  public static final Icon ICON = IconLoader
      .icon("Ruler_linestring.gif");

  //String sDistance = I18N.get("ui.cursortool.CoordinateListMetrics.Distance");
  public static String sLastSegment = I18NPlug.get("MeasureToolbox.last_segment");

  double area;
  double perimeter;
  double length;
  double lastLength;
  UnitConverter unitConverter = new UnitConverter();

  @Override
  public Cursor getCursor() {
    return createCursor(IconLoader.icon("RulerCursor.gif").getImage());
  }

  PlugInContext context;

  public MeasureDistanceTool(PlugInContext context) {
    this.context = context;
    setStroke(new BasicStroke(2));
    //setMetricsDisplay(new CoordinateListMetrics_extended());
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
    if (ToolboxMeasurePlugIn.saveCheck.isSelected()) {
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(feat);
    } else {
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().clear();
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(feat);
    }
    display(getCoordinates(), getPanel());

    getPanel().setViewportInitialized(true);
  }

  /*
   * End do the common part
   */

  /*
   * Change only NewCoordinateListMetric And "TXT" attribute
   */
  private Feature toFeature(Geometry measureGeometry,
                            FeatureSchema schema) {
    area = Math.round(CoordinateListMetrics_extended
        .area(getCoordinates()) * 10000) / 10000.0 * unitConverter.areaConverter();

    length = Math.round(CoordinateListMetrics_extended
        .distance(getCoordinates(), false) * 10000)
        / 10000.0 * unitConverter.lengthConverter();


    Feature feature = new BasicFeature(MeasurementLayerFinder.measureLayer(context)
        .getFeatureCollectionWrapper().getFeatureSchema());


    feature.setGeometry(measureGeometry);
    feature.setAttribute("TYPE", "Length");
    feature.setAttribute("TEXT", unitConverter.decimalformat(length) + unitConverter.measuredLengthUnit());
    feature.setAttribute("LENGHT", length);
    feature.setAttribute("UNIT", unitConverter.measureUnit());
    feature.setAttribute("GEOM", getLineString());
    return feature;
  }

  private LineString getLineString() {
    return new GeometryFactory()
        .createLineString(toArray(getCoordinates()));
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
    // Double measure = area;
    area = Math.round(CoordinateListMetrics_extended
        .area(coordinates) * 100) / 100.0D;
    length = Math.round(CoordinateListMetrics_extended
        .distance(coordinates, false) * 100) / 100.0D;
    perimeter = Math.round(CoordinateListMetrics_extended
        .distance(coordinates, true) * 100) / 100.0D;
    lastLength = Math.round(CoordinateListMetrics_extended.distanceLastSegment(coordinates) * 10000)
        / 10000.0 * unitConverter.lengthConverter();

    double lastsegment = lastLength;
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      distString = "(Geographics) - ";
    } else {
      distString = "(Projected) - ";
    }

    String measureUnit = ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString();
    String mapUnit = ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString();
    if (mapUnit.equals(measureUnit)) {
      distString += sLength + ": "
          + unitConverter.decimalformat(length) + unitConverter.mapLengthUnit()
          + " -> " + sLastSegment + " :" + unitConverter.decimalformat(lastsegment)
          + unitConverter.mapLengthUnit();
    } else {
      distString += sLength + ": "
          + unitConverter.decimalformat(length) + unitConverter.mapLengthUnit()
          + " [" + unitConverter.decimalformat(length * unitConverter.lengthConverter())
          + unitConverter.measuredLengthUnit() + "]"

          + " -> " + sLastSegment + " :" + unitConverter.decimalformat(lastsegment)
          + unitConverter.mapLengthUnit()
          + " [" + unitConverter.decimalformat(lastsegment * unitConverter.lengthConverter())
          + unitConverter.measuredLengthUnit() + "]";
    }
    panel.getContext().setStatusMessage(distString);
  }
}

