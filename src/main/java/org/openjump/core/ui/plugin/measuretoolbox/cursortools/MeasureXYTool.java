package org.openjump.core.ui.plugin.measuretoolbox.cursortools;

import java.awt.BasicStroke;
import java.awt.Cursor;

import javax.swing.Icon;

import org.openjump.core.ui.plugin.measuretoolbox.icons.IconLoader;
import org.openjump.core.ui.plugin.measuretoolbox.plugins.ToolboxMeasurePlugIn;
import org.openjump.core.ui.plugin.measuretoolbox.utils.CoordinateListMetrics_extended;
import org.openjump.core.ui.plugin.measuretoolbox.utils.Measure_NClickTool;
import org.openjump.core.ui.plugin.measuretoolbox.utils.MeasurementLayerFinder;
import org.openjump.core.ui.plugin.measuretoolbox.utils.UnitConverter;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


/**
 * Computes coordinates
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class MeasureXYTool extends Measure_NClickTool {

  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasureTools.Coordinates");
  public static final Icon ICON = IconLoader
      .icon("Ruler_XY.gif");

  UnitConverter unitConverter = new UnitConverter();

  @Override
  public Cursor getCursor() {
    return createCursor(IconLoader.icon("RulerCursor_p.gif").getImage());
  }

  PlugInContext context;

  public MeasureXYTool(PlugInContext context) {
    super(1);
    this.context = context;
    setStroke(new BasicStroke(2));
    setMetricsDisplay(new CoordinateListMetrics_extended());
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
    Feature feat =
        toFeature_xy(measureGeometry, MeasurementLayerFinder.measureLayer(context)
            .getFeatureCollectionWrapper().getFeatureSchema());


    if (ToolboxMeasurePlugIn.saveCheck.isSelected()) {
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(feat);

    } else {
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().clear();
      MeasurementLayerFinder.measureLayer(context).getFeatureCollectionWrapper().add(feat);

    }

  }

  /*
   * End do the common part
   */

  /*
   * Change only NewCoordinateListMetric And "TXT" attribute
   */
  private Feature toFeature_xy(Geometry measureGeometry, FeatureSchema schema) {

    double coordX = Math.round(getPoint().getX() * 10000) / 10000.0;
    double coordY = Math.round(getPoint().getY() * 10000) / 10000.0;

    Feature feature = new BasicFeature(MeasurementLayerFinder.measureLayer(context)
        .getFeatureCollectionWrapper().getFeatureSchema());
    feature.setGeometry(measureGeometry);
    feature.setAttribute("TYPE", "XY");
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      feature.setAttribute("TEXT",
          coordX + "° - " + coordY + "°");
    } else {
      feature.setAttribute("TEXT",
          coordX + " - " + coordY);
    }
    feature.setAttribute("X", unitConverter.decimalformat(coordX));
    feature.setAttribute("Y", unitConverter.decimalformat(coordY));
    feature.setAttribute("GEOM", getPoint());

    return feature;
  }

  protected Point getPoint() {
    return new GeometryFactory().createPoint(getCoordinates()
        .get(0));
  }
}