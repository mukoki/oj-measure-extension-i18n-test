package org.openjump.core.ui.plugin.measuretoolbox.utils;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.openjump.core.geomutils.Arc;
import org.openjump.core.geomutils.GeoUtils;
import org.openjump.core.geomutils.MathVector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;

/**
 * Duplicate from MultiClickArcTool.class from OpenJUMP to use new
 * Measure/conversion tools
 * Giuseppe Aruta - Sept 1th 2015
 */
public abstract class Measure_MultiClickArcTool extends Measure_MultiClickTool {

  protected boolean clockwise = true;
  protected double fullAngle = 0.0;

  protected Shape getShape() throws NoninvertibleTransformException {
    if (coordinates.size() > 1) {
      GeneralPath path = new GeneralPath();
      Coordinate firstCoordinate = coordinates.get(0);
      Point2D firstPoint = getPanel().getViewport().toViewPoint(firstCoordinate);
      path.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());

      Coordinate secondCoordinate = coordinates.get(1);
      Point2D secondPoint = getPanel().getViewport().toViewPoint(secondCoordinate);
      path.lineTo((float) secondPoint.getX(), (float) secondPoint.getY());

      MathVector v1 = (new MathVector(secondCoordinate)).vectorBetween(new MathVector(firstCoordinate));
      MathVector v2 = (new MathVector(tentativeCoordinate)).vectorBetween(new MathVector(firstCoordinate));
      double arcAngle = v1.angleDeg(v2);

      boolean toRight = GeoUtils.pointToRight(tentativeCoordinate, firstCoordinate, secondCoordinate);

      boolean cwQuad = ((fullAngle >= 0.0) && (fullAngle <= 90.0) && clockwise);
      boolean ccwQuad = ((fullAngle < 0.0) && (fullAngle >= -90.0) && !clockwise);
      if ((arcAngle <= 90.0) && (cwQuad || ccwQuad)) {
        if (toRight)
          clockwise = true;
        else
          clockwise = false;
      }

      if ((fullAngle > 90.0) || (fullAngle < -90)) {
        if ((clockwise && !toRight) || (!clockwise && toRight))
          fullAngle = 360 - arcAngle;
        else
          fullAngle = arcAngle;
      } else {
        fullAngle = arcAngle;
      }

      if (!clockwise)
        fullAngle = -fullAngle;

      if (fullAngle > 180)
        fullAngle = -(360 - fullAngle);


      Arc arc = new Arc(firstCoordinate, secondCoordinate, fullAngle);


      CoordinateList coords = arc.getCoordinates();

      for (int i = 1; i < coords.size(); i++) {

        Point2D nextPoint = getPanel().getViewport().toViewPoint(coords.get(i));
        //   path.lineTo((int) nextPoint.getX(), (int) nextPoint.getY());
        path.moveTo((int) nextPoint.getX(), (int) nextPoint.getY());
        // path.lineTo(firstPoint.getX(), firstPoint .getY());
      }
      path.lineTo(firstPoint.getX(), firstPoint.getY());
      return path;
    } else {
      return super.getShape();
    }
  }
}
