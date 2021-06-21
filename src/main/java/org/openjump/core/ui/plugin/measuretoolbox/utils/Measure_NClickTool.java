package org.openjump.core.ui.plugin.measuretoolbox.utils;

import java.awt.event.MouseEvent;

import org.locationtech.jts.geom.Coordinate;

/**
 * Duplicate from NClickTool.class from OpenJUMP to use new
 * Measure/conversion tools.
 * Giuseppe Aruta - Sept 1th 2015
 */
public abstract class Measure_NClickTool extends Measure_MultiClickTool {

  // This class has been tested only with n=1 and n=2. [Jon Aquino]
  private int n;

  public Measure_NClickTool(int n) {
    this.n = n;
  }

  public int numClicks() {
    return n;
  }

  protected Coordinate getModelSource() {
    return getCoordinates().get(0);
  }

  protected Coordinate getModelDestination() {
    return getCoordinates().get(n - 1);
  }

  @Override
  protected boolean isFinishingRelease(MouseEvent e) {
    // A double click will generate two events: one with click-count = 1,
    // and one with click-count = n. We just want to finish the gesture
    // once, so handle the click-count = 1 case, and ignore the others.
    // [Jon Aquino]
    return (e.getClickCount() == 1) && shouldGestureFinish();
  }

  private boolean shouldGestureFinish() {
    return getCoordinates().size() == n;
  }

}
