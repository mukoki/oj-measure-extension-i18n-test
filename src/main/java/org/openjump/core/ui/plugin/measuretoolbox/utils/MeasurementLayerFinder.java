package org.openjump.core.ui.plugin.measuretoolbox.utils;

import java.awt.*;

import org.locationtech.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
//import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.SystemLayerFinder;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;

import de.latlon.deejump.plugin.style.CircleVertexStyle;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


public class MeasurementLayerFinder extends SystemLayerFinder {

  public MeasurementLayerFinder(String layerName,
                                LayerManagerProxy layerManagerProxy) {
    super(LAYER_NAME, layerManagerProxy);
    // TODO Auto-generated constructor stub
  }


  /**
   * Modified from MeasureLayerFinder.class from OpenJUMP
   * Giuseppe Aruta - Sept 1th 2015
   */

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
    schema.addAttribute("UNIT", AttributeType.STRING);
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


  public Geometry getFence() {
    if (getLayer() == null) {
      return null;
    }

    if (getLayer().getFeatureCollectionWrapper().isEmpty()) {
      return null;
    }

    return getLayer().getFeatureCollectionWrapper().iterator().next().getGeometry();
  }

  @Override
  protected void applyStyles(Layer layer) {
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

  }


}
