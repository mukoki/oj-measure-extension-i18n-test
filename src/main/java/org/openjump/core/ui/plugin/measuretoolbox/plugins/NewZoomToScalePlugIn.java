/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * JUMP is Copyright (C) 2003 Vivid Solutions
 *
 * This program implements extensions to JUMP and is
 * Copyright (C) Stefan Steiniger.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 * Stefan Steiniger
 * perriger@gmx.de
 */
/*****************************************************
 * created:         04.01.2005
 * last modified:   01.10.2005 [scale obtained now from other class 
 *                              and change in layout]
 *
 * description:
 *   zooms to a given map scale, which is received from an input dialog 
 *
 *****************************************************/

package org.openjump.core.ui.plugin.measuretoolbox.plugins;

import javax.swing.ImageIcon;

import org.openjump.core.ui.plugin.measuretoolbox.icons.IconLoader;
//import org.openjump.core.ui.plugin.measuretoolbox.language.I18NPlug;
import org.openjump.core.ui.plugin.measuretoolbox.scale.GeoShowScaleRenderer;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.Viewport;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


/**
 * Zooms to a given map scale, received from a input dialog
 * Modified from ZoomToScalePlugIn.class from OpenJUMP to
 * work also with Geographic Coordinates (EPSG4326)
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class NewZoomToScalePlugIn extends AbstractPlugIn {

  public static ImageIcon ICON = IconLoader.icon("zoom_scale.png");
  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasurePlugin.NewZoomToScalePlugIn");

  int scale = 25000;
  double oldHorizontalScale; // is calculated for panel-width not heigth!!)
  //double modelWidth = 0;
  //double panelWidth = 0;
  String text = I18N.getInstance()
      .get("org.openjump.core.ui.plugin.view.ZoomToScalePlugIn.set-new-scale-to-zoom")
      + ":  1 : ";

  public ImageIcon getIcon() {
    return ICON;
  }

  @Override
  public String getName() {
      return NAME;
  }

  public static MultiEnableCheck createEnableCheck(
      WorkbenchContext workbenchContext) {
    EnableCheckFactory checkFactory = new EnableCheckFactory(
        workbenchContext);

    return new MultiEnableCheck().add(checkFactory
        .createAtLeastNLayerablesMustExistCheck(1));
  }

  @Override
  public boolean execute(PlugInContext context) throws Exception {

    Viewport port = context.getLayerViewPanel().getViewport();
    this.oldHorizontalScale = GeoShowScaleRenderer.getScale(port);

    MultiInputDialog dialog = new MultiInputDialog(
        context.getWorkbenchFrame(),
        I18N.getInstance().get("org.openjump.core.ui.plugin.view.ZoomToScalePlugIn.zoom-to-scale"),
        true);
    setDialogValues(dialog, context);
    GUIUtil.centreOnWindow(dialog);
    dialog.setVisible(true);
    if (!dialog.wasOKPressed()) {
      return false;
    }
    getDialogValues(dialog);

    zoomToNewScale(context);

    return true;
  }

  public void zoomToNewScale(PlugInContext context) throws Exception {
    Viewport port = context.getLayerViewPanel().getViewport();
    this.oldHorizontalScale = GeoShowScaleRenderer.getScale(port);

    // -- get zoom factor
    double factor = this.scale / this.oldHorizontalScale;

    // --calculating new screen using the envelope of the corner LineString
    Envelope oldEnvelope = port.getEnvelopeInModelCoordinates();

    double xc = 0.5 * (oldEnvelope.getMaxX() + oldEnvelope.getMinX());
    double yc = 0.5 * (oldEnvelope.getMaxY() + oldEnvelope.getMinY());
    double xmin = xc - 1 / 2.0 * factor * oldEnvelope.getWidth();
    double xmax = xc + 1 / 2.0 * factor * oldEnvelope.getWidth();
    double ymin = yc - 1 / 2.0 * factor * oldEnvelope.getHeight();
    double ymax = yc + 1 / 2.0 * factor * oldEnvelope.getHeight();
    Coordinate[] coords = new Coordinate[]{new Coordinate(xmin, ymin),
        new Coordinate(xmax, ymax)};
    Geometry g1 = new GeometryFactory().createLineString(coords);
    port.zoom(g1.getEnvelopeInternal());
  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {

    dialog.addLabel(I18N.getInstance()
        .get("org.openjump.core.ui.plugin.view.ZoomToScalePlugIn.actual-scale-in-horizontal-direction")
        + " 1 : " + (int) this.oldHorizontalScale);
    int scaleD = (int) this.oldHorizontalScale;
    dialog.addIntegerField(text, scaleD, 7, text);
  }

  private void getDialogValues(MultiInputDialog dialog) {
    this.scale = dialog.getInteger(text);
  }

  public void setScale(double scale) {
    this.scale = (int) scale;
  }

}
