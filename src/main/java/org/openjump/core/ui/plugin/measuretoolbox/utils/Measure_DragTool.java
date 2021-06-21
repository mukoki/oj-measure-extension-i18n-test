/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
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
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package org.openjump.core.ui.plugin.measuretoolbox.utils;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openjump.core.ui.util.ScreenScale;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;

/**
 * The default implementation draws a selection box, but this can be overridden
 * (even to draw nothing).
 */
public abstract class Measure_DragTool extends AbstractCursorTool {

  public static final int DEFAULT_VIEW_CLICK_BUFFER = 2;
  private int viewClickBuffer = DEFAULT_VIEW_CLICK_BUFFER;
  /**
   * Modify using #setSource
   */
  protected Coordinate modelSource = null;
  /**
   * Modify using #setDestination
   */
  protected Coordinate modelDestination = null;
  private boolean dragApproved = false;
  private boolean closeRing = false;

  protected List<Coordinate> coordinates = new ArrayList<>();
  private Coordinate tentativeCoordinate;
  private CoordinateListMetrics_extended metrics = null;


  protected void setMetricsDisplay(CoordinateListMetrics_extended metrics) {
    this.metrics = metrics;
  }

  protected CoordinateListMetrics_extended getMetrics() {
    return metrics;
  }

  protected void setCloseRing(boolean closeRing) {
    this.closeRing = closeRing;
  }

  /**
   * Begins handling of the drag. Subclasses can prevent handling of the drag
   * by overriding this method and not calling it.
   */
  public void mousePressed(MouseEvent e) {
    super.mousePressed(e);

    dragApproved = true;
    try {
      setViewSource(e.getPoint());
    } catch (NoninvertibleTransformException x) {
      getPanel().getContext().handleThrowable(x);

    }
    // Probably doesn't make sense to snap the source. Note that
    // MoveSelectedItem's
    // override of #snap assumes that it is only used on the destination.
    // [Jon Aquino]

  }

  protected void mouseLocationChanged(MouseEvent e) {
    try {
      if (coordinates.isEmpty()) {
        return;
      }

      tentativeCoordinate = snap(e.getPoint());
      getWorkbench().getFrame()
          .setTimeMessage(
              "1:"
                  + (int) Math.floor(ScreenScale
                  .getHorizontalMapScale(panel
                      .getViewport())));
      redrawShape();
      displayMetrics(e);
    } catch (Throwable t) {
      getPanel().getContext().handleThrowable(t);
    }
  }

  private void displayMetrics(MouseEvent e)
      throws NoninvertibleTransformException {
    if (metrics == null)
      return;
    if (isShapeOnScreen()) {
      List<Coordinate> currentCoordinates = new ArrayList<>(getCoordinates());
      currentCoordinates.add(getPanel().getViewport().toModelCoordinate(
          e.getPoint()));
      metrics.displayMetrics(currentCoordinates, getPanel());
    }
  }

  public List<Coordinate> getCoordinates() {
    return Collections.unmodifiableList(coordinates);
  }

  /**
   * A click is converted into a box by being expanded by this amount in the
   * four directions.
   */
  protected void setViewClickBuffer(int clickBuffer) {
    this.viewClickBuffer = clickBuffer;
  }

  protected boolean wasClick() {
    return getModelSource().equals(getModelDestination());
  }

  protected Envelope getBoxInModelCoordinates() {
    double minX = Math.min(getModelSource().x, getModelDestination().x);
    double maxX = Math.max(getModelSource().x, getModelDestination().x);
    double minY = Math.min(getModelSource().y, getModelDestination().y);
    double maxY = Math.max(getModelSource().y, getModelDestination().y);

    if (wasClick()) {
      minX -= modelClickBuffer();
      maxX += modelClickBuffer();
      minY -= modelClickBuffer();
      maxY += modelClickBuffer();
    }

    return new Envelope(minX, maxX, minY, maxY);
  }

  protected double modelClickBuffer() {
    return viewClickBuffer / getPanel().getViewport().getScale();
  }

  public void mouseDragged(MouseEvent e) {
    try {
      if (!dragApproved) {
        // dragApproved will be false if:
        // -- the drag began outside the panel
        // -- a subclass wanted to prevent handling of the drag by
        // overriding
        // #mousePressed and not calling it; for example,
        // EditDelineationTool.
        // [Jon Aquino]
        return;
      }

      setViewDestination(e.getPoint());
      redrawShape();
    } catch (Throwable t) {
      getPanel().getContext().handleThrowable(t);
    }
  }

  protected Coordinate getModelSource() {
    return modelSource;
  }

  protected Coordinate getModelDestination() {
    return modelDestination;
  }

  protected void setModelSource(Coordinate source) {
    this.modelSource = source;
  }

  protected void setViewSource(Point2D source)
      throws NoninvertibleTransformException {
    setModelSource(getPanel().getViewport().toModelCoordinate(source));
  }

  protected void setViewDestination(Point2D destination)
      throws NoninvertibleTransformException {
    setModelDestination(getPanel().getViewport().toModelCoordinate(
        destination));
  }

  protected void setModelDestination(Coordinate destination) {
    this.modelDestination = snap(destination);
  }

  public void mouseReleased(MouseEvent e) {
    try {
      boolean dragComplete = isShapeOnScreen();
      clearShape();
      if (e.getClickCount() == 1) {
        tentativeCoordinate = snap(e.getPoint());
        redrawShape();
      }
      super.mouseReleased(e);
      if (dragComplete) {
        fireGestureFinished();
      }
      dragApproved = false;
    } catch (Throwable t) {
      getPanel().getContext().handleThrowable(t);
    }
  }

  protected Shape getShape() throws Exception {
    return getShape(getViewSource(), getViewDestination());
  }

  protected Point2D getViewSource() throws NoninvertibleTransformException {
    return getPanel().getViewport().toViewPoint(getModelSource());
  }

  protected Point2D getViewDestination()
      throws NoninvertibleTransformException {
    return getPanel().getViewport().toViewPoint(getModelDestination());
  }

  /**
   * @return null if nothing should be drawn
   */
  protected Shape getShape(Point2D source, Point2D destination) {
    double minX = Math.min(source.getX(), destination.getX());
    double minY = Math.min(source.getY(), destination.getY());
    double maxX = Math.max(source.getX(), destination.getX());
    double maxY = Math.max(source.getY(), destination.getY());

    return new Rectangle.Double(minX, minY, maxX - minX, maxY - minY);
  }


}
