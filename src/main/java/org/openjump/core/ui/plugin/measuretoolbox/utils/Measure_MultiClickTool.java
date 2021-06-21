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

import java.awt.Point;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openjump.core.ui.plugin.edittoolbox.tab.ConstraintManager;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
//import org.openjump.core.ui.plugin.measuretoolbox.language.I18NPlug;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


/**
 * Modified from MultiClickTool.class from OpenJUMP to use new
 * Measure/conversion tools
 * Giuseppe Aruta - Sept 1th 2015
 */
public abstract class Measure_MultiClickTool extends AbstractCursorTool {

  public static String LAYER_NAME = I18NPlug
      .get("MeasureToolbox.layer");
  public static String sArea = I18NPlug
      .get("MeasureToolbox.Area");
  public static String sAzimuth = I18NPlug
      .get("MeasureToolbox.Azimuth");
  public static String sAngle = I18NPlug
      .get("MeasureToolbox.Angle");
  public static String sLength = I18NPlug
      .get("MeasureToolbox.Length");
  public static String sPerimeter = I18NPlug
      .get("MeasureToolbox.Perimeter");
  public static String sRadius = I18NPlug
      .get("MeasureToolbox.Radius");
  public static String sArc = I18NPlug
      .get("MeasureToolbox.Arc");
  public static final String ERROR = I18NPlug
      .get("MeasureToolbox.error_geometry");


  // protected Image origImage;
  // protected Image auxImage = null;
  // protected double scale = 1.0D;
  //protected int mouseWheelCount = 0;

  protected List<Coordinate> coordinates = new ArrayList<>();
  protected Coordinate tentativeCoordinate;
  public boolean closeRing = false;
  private CoordinateListMetrics_extended metrics = null;

  protected LayerViewPanel panel;
  private WorkbenchFrame frame;
  private ConstraintManager constraintManager;
  protected boolean drawClosed = false;

  public Measure_MultiClickTool() {

  }

  protected void setMetricsDisplay(CoordinateListMetrics_extended metrics) {
    this.metrics = metrics;
  }

  protected CoordinateListMetrics_extended getMetrics() {
    return metrics;
  }

  //protected void setCloseRing(boolean closeRing) {
  public void setCloseRing(boolean closeRing) {
    this.closeRing = closeRing;
  }


  /**
   * Will return an empty List once the shape is cleared.
   */
  public List<Coordinate> getCoordinates() {
    return Collections.unmodifiableList(coordinates);
  }

  @Override
  public void cancelGesture() {
    super.cancelGesture();
    coordinates.clear();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    try {
      if (e.getClickCount() == 1) {
        tentativeCoordinate = snap(e.getPoint());

      }
      super.mouseReleased(e);
      if (isFinishingRelease(e)) {
        finishGesture();
      }
    } catch (Throwable t) {
      getPanel().getContext().handleThrowable(t);
    }
  }

  protected Coordinate doConstraint(MouseEvent e)
      throws NoninvertibleTransformException {
    Coordinate retPt = snap(e.getPoint());
    retPt = this.constraintManager.constrain(getPanel(), getCoordinates(),
        retPt, e);
    return retPt;
  }

  protected Point mouseLastLoc;

  protected void mouseLocationChanged(MouseEvent e) {
    try {
      if (coordinates.isEmpty()) {
        return;
      }
      mouseLastLoc = e.getPoint();
      tentativeCoordinate = snap(e.getPoint());
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
      ArrayList<Coordinate> currentCoordinates = new ArrayList<>(getCoordinates());
      currentCoordinates.add(snap(getPanel().getViewport()
          .toModelCoordinate(e.getPoint())));
      metrics.displayMetrics(currentCoordinates, getPanel());
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    mouseLocationChanged(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    mouseLocationChanged(e);
  }

  protected void add(Coordinate c) {
    coordinates.add(c);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    try {
      super.mousePressed(e);
      Assert.isTrue(e.getClickCount() > 0);

      // Don't add more than one point for double-clicks. A double-click
      // will
      // generate two events: one with click-count = 1 and another with
      // click-count = 2. Handle the click-count = 1 event and ignore the
      // rest.
      // [Jon Aquino]
      if (e.getClickCount() != 1) {
        return;
      }

      add(snap(e.getPoint()));
    } catch (Throwable t) {
      getPanel().getContext().handleThrowable(t);
    }
  }

  @Override
  protected Shape getShape() throws NoninvertibleTransformException {
    GeneralPath path = new GeneralPath();
    // sometimes the coordinates are empty and we get an
    // IndexOutOfBoundsExeption!
    // we get this if we use this tool, open a menu and then click with the
    // open menu in the map. In this moment we do not get the mousePressed
    // event and no coordinate will be added.
    if (!coordinates.isEmpty()) {
      Point2D firstPoint = getPanel().getViewport().toViewPoint(
          coordinates.get(0));
      path.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());

      for (int i = 1; i < coordinates.size(); i++) { // start 1 [Jon
        // Aquino]

        Coordinate nextCoordinate = coordinates.get(i);
        Point2D nextPoint = getPanel().getViewport().toViewPoint(
            nextCoordinate);
        path.lineTo((int) nextPoint.getX(), (int) nextPoint.getY());
      }
      Point2D tentativePoint = getPanel().getViewport().toViewPoint(
          tentativeCoordinate);
      path.lineTo((int) tentativePoint.getX(),
          (int) tentativePoint.getY());
      // close path (for rings only)
      if (closeRing)
        path.lineTo((int) firstPoint.getX(), (int) firstPoint.getY());

    }
    return path;
  }

  protected boolean isFinishingRelease(MouseEvent e) {
    return e.getClickCount() == 2;
  }

  protected Coordinate[] toArray(List<Coordinate> coordinates) {
    return coordinates.toArray(new Coordinate[]{});
  }

  protected void finishGesture() throws Exception {
    clearShape();

    try {
      fireGestureFinished();
    } finally {
      // If exception occurs, cancel. [Jon Aquino]
      cancelGesture();
    }
  }

  protected Coordinate getIntersection(Coordinate p1, Coordinate p2,
                                       Coordinate p3, Coordinate p4) {
    Coordinate e = new Coordinate(0.0D, 0.0D);
    Coordinate v = new Coordinate(0.0D, 0.0D);
    Coordinate w = new Coordinate(0.0D, 0.0D);
    double t1;
    double n;
    double d;
    p2.x -= p1.x;
    p2.y -= p1.y;
    p4.x -= p3.x;
    p4.y -= p3.y;
    n = w.y * (p3.x - p1.x) - w.x * (p3.y - p1.y);
    d = w.y * v.x - w.x * v.y;
    if (d != 0.0D) {
      t1 = n / d;
      p1.x += v.x * t1;
      p1.y += v.y * t1;
    } else {
      e.z = 999.0D;
    }
    return e;
  }

/*    @Override
	public void deactivate() {
        super.deactivate();
        frame = panel.getWorkBenchFrame();
        frame.removeEasyKeyListener(keyListener);
    }*/

  @Override
  public void activate(LayerViewPanel layerViewPanel) {
    // cancel gestures if we switch LayerViews (switch Tasks)
    if ((panel != null) && !(panel.equals(layerViewPanel))) {
      cancelGesture();
    }

    super.activate(layerViewPanel);
    panel = layerViewPanel;

    // following added to handle Backspace key deletes last vertex
    frame = panel.getWorkBenchFrame();
    frame.addEasyKeyListener(keyListener);
  }

  private KeyListener keyListener = new KeyListener() {
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
      // erase segment by segment via BACKSPACE, eventually cancel drawing
      if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
        if (coordinates.size() > 1) {
          int rem = coordinates.size() - 1;
          coordinates.remove(rem);
          try {
            redrawShape();
          } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
          }
        } else
          cancelGesture();
      }
      // cancel drawing via ESCAPE
      else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        cancelGesture();
      }
      // approve drawing via ENTER, emulates doubleclick
      else if (e.getKeyCode() == KeyEvent.VK_ENTER
          && mouseLastLoc != null) {
        mousePressed(new MouseEvent(panel, MouseEvent.MOUSE_PRESSED,
            0, InputEvent.BUTTON1_MASK, mouseLastLoc.x,
            mouseLastLoc.y, 1, false));
        mouseReleased(new MouseEvent(panel, MouseEvent.MOUSE_RELEASED,
            0, InputEvent.BUTTON1_MASK, mouseLastLoc.x,
            mouseLastLoc.y, 2, false));
      }
    }
  };

}
