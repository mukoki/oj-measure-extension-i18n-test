package org.openjump.core.ui.plugin.measuretoolbox.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.openjump.core.geomutils.Arc;
import org.openjump.core.ui.plugin.measuretoolbox.plugins.ToolboxMeasurePlugIn;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;


/**
 * CoordinateListMetrics_extended class derives form OpenJUMP class:
 * com.vividsolutions.jump.workbench.ui.cursortool.CoordinateListMetrics It: a)
 * computes Length, Area, Angle and Azimuth under Projected and Geographic
 * (EPSG4326) coordinates. b) computes Bearing between two coordinates c)
 * calculates Length and Area under Geographic Coordinates (see the end of the
 * class)
 * <p>
 * Giuseppe Aruta - Sept 1th 2015
 */

public class CoordinateListMetrics_extended {

  public static String sArea = I18N.getInstance().get("ui.cursortool.CoordinateListMetrics.Area");
  String sAzimuth = I18N.getInstance().get("ui.cursortool.CoordinateListMetrics.Azimuth");
  String sAngle = I18N.getInstance().get("ui.cursortool.CoordinateListMetrics.Angle");
  public static String sDistance = I18N.getInstance().get("ui.cursortool.CoordinateListMetrics.Distance");
  //CoordinateListMetrics coo = new CoordinateListMetrics();

  /**
   * WGS84 ellipsoid parameters (in meters)
   */
  static double SemiMajorAxis = 6378137;
  static double SemiMinorAxis = 6356752.314245;
  static double MeanAxis = 6371008.7714;
  static double Flattening = 1 / 298.257223563;
  //static double InverseFlattening = 298.257223563;

  /**
   * @return the square of the first numerical eccentricity
   */
  public static double SqFirsteccentricity() {
    return (Flattening * (2. - Flattening));
  }

  /**
   * The main class to define map and measure units and conversions
   */
  UnitConverter mum = new UnitConverter();

  double area;
  //double perimeter;
  Double length = area;

  public CoordinateListMetrics_extended() {
  }

  public void displayMetrics(List<Coordinate> coordinates, LayerViewPanel panel) {
    displayMetrics(coordinates, panel, false);
  }

  /**
   * Display the coordinates metrics with the option to compute the distance for a
   * closed geometry.
   *
   * @param coordinates    list of coordinates
   * @param panel          the LayerViewPanel
   * @param closedDistance true to add the distance from last to first Coordinate
   */
  public void displayMetrics(List<Coordinate> coordinates, LayerViewPanel panel, boolean closedDistance) {

    panel.getContext().setStatusMessage(getMetricsString(coordinates, panel, closedDistance));

  }

  public String getMetricsString(List<Coordinate> coordinates, LayerViewPanel panel) {

    return getMetricsString(coordinates, panel, false);

  }

  /**
   * Gets the projected metrics
   *
   * @param coordinates    list of coordinates
   * @param panel          the LayerViewPanel
   * @param closedDistance true to add the distance from last to first Coordinate
   * @return the String representing the geometry metrics
   */
  public String getMetricsString(List<Coordinate> coordinates, LayerViewPanel panel, boolean closedDistance) {

    // double dist = distanceWorld(coordinates);
    double dist = distance(coordinates, closedDistance);

    double area = area(coordinates);
    DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
    String formatPattern;
    // adaptive format pattern, thanks to Michaël
    // Michaud for his idea!
    if (length >= 10) {
      formatPattern = "#,##0.00";
    } else if (length >= 1) {
      formatPattern = "#,##0.000";
    } else if (length >= 0.1) {
      formatPattern = "#,##0.0000";
    } else if (length >= 0.01) {
      formatPattern = "#,##0.00000";
    } else
      formatPattern = "#,##0.000000";
    decimalFormat.applyPattern(formatPattern);
    String dispStr;
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      dispStr = "(Geographics) - ";
    } else {
      dispStr = "(Projected) - ";
    }
    dispStr += sDistance + ": " + decimalFormat.format(dist) + mum.mapLengthUnit() + " ["
        + decimalFormat.format(dist * mum.lengthConverter()) + mum.measuredLengthUnit() + "]";
    if (coordinates.size() > 2) {

      dispStr += "   " + sArea + ": " + decimalFormat.format(area) + mum.mapAreaUnit() + " ["
          + decimalFormat.format(area * mum.areaConverter()) + mum.measuredAreaUnit() + "]";
    }
    double angle = angle(coordinates);
    dispStr += "   " + sAngle + ": " + decimalFormat.format(angle) + "°";

    double azimuth = azimuth(coordinates);
    dispStr += "   " + sAzimuth + ": " + decimalFormat.format(azimuth) + "°";
    return dispStr;
  }

  //////////////////////////////////////////////////////////////
  // The following methods compute different geometric //
  // parameter. //
  // distance (of a linestring) //
  // distance_last (between the 2 last drawn points //
  // perimeter (of a polygon) //
  // area (of a polygon) //
  // angle (defined 3 points a, b, c - the angle abc //
  // angleGoniometer (the angle bac) //
  // azimuth //
  // bearing //
  //////////////////////////////////////////////////////////////

  /**
   * Giuseppe Aruta (Peppe - ma15569) 08-14-2014 Computes the distance with the
   * option to compute the distance for a closed geometry. Giuseppe Aruta (Peppe -
   * ma15569) 08-14-2014 Option to calculate area in Geographic coordinates
   *
   * @param coordinates    list of coordinates
   * @param closedDistance true to add the distance from last to first Coordinate
   * @return the sum of the distance between coordinates
   */
  public static double distance(List<Coordinate> coordinates, boolean closedDistance) {

    double distance = 0;
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      for (int i = 1; i < coordinates.size(); i++) {
        Coordinate b = coordinates.get(i);
        Coordinate c = coordinates.get(i - 1);

        distance += computeGeographicLenght(b.y, b.x, c.y, c.x);
      }
    } else {

      for (int i = 1; i < coordinates.size(); i++) {
        distance += coordinates.get(i - 1).distance(coordinates.get(i));
      }
      // compute the last distance part from the last coordinate to the first,
      // if we are in closed mode
      if (coordinates.size() > 2 && closedDistance) {
        distance += coordinates.get(coordinates.size() - 1).distance(coordinates.get(0));
      }
    }
    return distance;
  }

  public static double distance(List<Coordinate> coordinates) {
    return distance(coordinates, false);
  }

  /**
   * Giuseppe Aruta (Peppe - ma15569) 08-14-2014 Computes the distance between the
   * last 2 coordinates. Giuseppe Aruta (Peppe - ma15569) 08-14-2014 Option to
   * calculate area in Geographic coordinates
   *
   * @param coordinates list of coordinates
   * @return distance
   */
  public static double distanceLastSegment(List<Coordinate> coordinates) {

    double lastsegment = 0;
    for (int i = 1; i < coordinates.size(); i++) {

      Coordinate a = coordinates.get(i - 1);
      Coordinate b = coordinates.get(i);

      if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {

        lastsegment = CoordinateListMetrics_extended.computeGeographicLenght(a.y, a.x, b.y, b.x);

      } else {

        lastsegment = a.distance(b);
      }
    }
    return lastsegment;
  }

  /**
   * Giuseppe Aruta (Peppe - ma15569) 08-14-2014 Computes the distance between the
   * 2 coordinates. Giuseppe Aruta (Peppe - ma15569) 08-14-2014 Option to
   * calculate area in Geographic coordinates
   *
   * @param a first Coordinate
   * @param b second Coordinate
   * @return distance
   */
  public static double distanceBetweenAB(Coordinate a, Coordinate b) {
    double segment;
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      segment = CoordinateListMetrics_extended.computeGeographicLenght(a.y, a.x, b.y, b.x);
    } else {
      segment = a.distance(b);
    }
    return segment;
  }

  /**
   * Computes the area for the coordinates list. The area is forced to be
   * positive. The coordinate list can be open, and the closing coordinate is
   * supplied. Giuseppe Aruta (Peppe - ma15569) 08-14-2014 Option to calculate
   * area in Geographic coordinates
   *
   * @param coordinates list of coordinates
   * @return the area of polygon defined by coordinates
   */
  public static double area(List<Coordinate> coordinates) {

    double signedArea;
    if (coordinates.size() < 3)
      return 0.0D;
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {

      signedArea = computeGeographicArea(coordinates);

    } else {

      if (coordinates.size() < 3)
        return 0.0;
      double sum = 0.0;
      for (int i = 0; i < coordinates.size(); i++) {
        Coordinate b = coordinates.get(i);
        int nexti = i + 1;
        if (nexti > coordinates.size() - 1)
          nexti = 0;
        Coordinate c = coordinates.get(nexti);
        sum += (b.x + c.x) * (c.y - b.y);
      }
      signedArea = -sum / 2.0;
    }
    if (signedArea >= 0)
      return signedArea;
    return -signedArea;
  }

  /**
   * Giuseppe Aruta (Peppe - ma15569) 08-14-2014 Giving 3 sequential points A, B,
   * C. This algorithm computes the angle between the segments AB and BC. Option
   * to calculate angle in Geographic coordinates
   *
   * @param coordinates list of coordinates
   * @return the angle in degrees
   */
  public static double angle(List<Coordinate> coordinates) {

    double angRad;
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      int size = coordinates.size();
      if (size <= 1)
        return 0.0;
      Coordinate p1 = coordinates.get(size - 2);
      Coordinate p2 = coordinates.get(size - 1);
      // if only 2 coords, compute angle relative to X axis
      Coordinate p0;
      if (size > 2)
        p0 = coordinates.get(size - 3);
      else
        p0 = new Coordinate(p1.x + 1.0, p1.y);
      double AB = Math.sqrt(Math.pow(p1.x - p0.x, 2) + Math.pow(p1.y - p0.y, 2));
      double BC = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
      double AC = Math.sqrt(Math.pow(p2.x - p0.x, 2) + Math.pow(p2.y - p0.y, 2));
      angRad = Math.acos((BC * BC + AB * AB - AC * AC) / (2 * BC * AB));
      return Math.toDegrees(angRad);
    } else {
      int size = coordinates.size();
      if (size <= 1)
        return 0.0;
      Coordinate p1 = coordinates.get(size - 2);
      Coordinate p2 = coordinates.get(size - 1);
      // if only 2 coords, compute angle relative to X axis
      Coordinate p0;
      if (size > 2)
        p0 = coordinates.get(size - 3);
      else
        p0 = new Coordinate(p1.x + 1.0, p1.y);
      angRad = Angle.angleBetween(p1, p0, p2);
    }
    return Math.toDegrees(angRad);
  }

  /**
   * Giuseppe Aruta (Peppe - ma15569) 08-14-2014 Giving 3 sequential points A, B,
   * C. This algorithm computes the angle between the segments BA and CA. Option
   * to calculate angle in Geographic coordinates
   *
   * @param coordinates list of coordinates
   * @return the angle in degrees
   */
  public static double angleGoniometer(List<Coordinate> coordinates) {
    double angRad;
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      int size = coordinates.size();
      if (size <= 1)
        return 0.0;
      Coordinate p1 = coordinates.get(size - 2);
      Coordinate p2 = coordinates.get(size - 1);
      // if only 2 coords, compute angle relative to X axis
      Coordinate p0;
      if (size > 2)
        p0 = coordinates.get(size - 3);
      else
        p0 = new Coordinate(p1.x + 1.0, p1.y);
      double AB = Math.sqrt(Math.pow(p1.x - p0.x, 2) + Math.pow(p1.y - p0.y, 2));
      double BC = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
      double AC = Math.sqrt(Math.pow(p2.x - p0.x, 2) + Math.pow(p2.y - p0.y, 2));
      angRad = Math.acos((AC * AC + AB * AB - BC * BC) / (2 * AC * AB));
    } else {
      int size = coordinates.size();
      if (size <= 1)
        return 0.0;
      Coordinate p1 = coordinates.get(size - 2);
      Coordinate p2 = coordinates.get(size - 1);
      // if only 2 coords, compute angle relative to X axis
      Coordinate p0;
      if (size > 2)
        p0 = coordinates.get(size - 3);
      else
        p0 = new Coordinate(p1.x + 1.0, p1.y);

      angRad = Angle.angleBetween(p0, p1, p2);
    }
    return Math.toDegrees(angRad);
  }

  /**
   * Giuseppe Aruta (Peppe - ma15569) 03-14-2014 Computes the angle facing North
   * (upper side of the view) Option to calculate azimuth in Geographic
   * coordinates
   *
   * @param coordinates list of coordinates
   * @return the angle in degrees
   */
  public static double azimuth(List<Coordinate> coordinates) {
    double angle;
    double angle1;
    int size = coordinates.size();
    if (size <= 1)
      return 0.0D;
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      Coordinate b = coordinates.get(size - 2);
      Coordinate c = coordinates.get(size - 1);
      return computeGeographicNorth(b.y, b.x, c.y, c.x);
    } else {
      Coordinate p1 = coordinates.get(size - 2);
      Coordinate p2 = coordinates.get(size - 1);
      double d;
      LineSegment ls = new LineSegment(p1, p2);
      d = ls.angle();
      angle = 90.0D - d * 57.295779513082323D;
      angle1 = angle;
      if (angle < 0.0D)
        angle1 += 360.0D;
      return angle1;
    }

  }

  ///////////////////////////////
  // Some geometrics algorithms//
  // for future development //
  ///////////////////////////////

  protected static double fullAngle = 0.0D;

  /**
   * Giuseppe Aruta (Peppe - ma15569) 03-14-2014 Computes the length of an arc
   * Option to calculate azimuth in Geographic coordinates
   *
   * @param coordinates list of coordinates
   * @return length of an arc
   */
  public static double distanceArc(List<Coordinate> coordinates) {
    int size = coordinates.size();
    double dist = 0;
    if (size <= 1)
      return 0.0D;
    Coordinate a = coordinates.get(size - 2);
    Coordinate b = coordinates.get(size - 1);
    Arc arc = new Arc(a, b, fullAngle);

    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      for (int i = 1; i < coordinates.size(); i++) {
        Coordinate a1 = coordinates.get(i);
        Coordinate b1 = coordinates.get(i - 1);
        dist += computeGeographicLenght(a1.y, a1.x, b1.y, b1.x);
      }
    } else {
      dist = arc.getLineString().getLength();
    }
    return dist;
  }

  /**
   * Azimuth of an arc Bisector
   *
   * @param coordinates list of coordinates
   * @return azimuth
   */
  public static double azimuthBisector(List<Coordinate> coordinates) {
    int size = coordinates.size();
    if (size <= 1)
      return 0.0D;
    Coordinate a = coordinates.get(size - 3);
    Coordinate b = coordinates.get(size - 2);
    Coordinate c = coordinates.get(size - 1);
    LineSegment bc = new LineSegment(b, c);
    Coordinate middlepoint = bc.midPoint();
    double d3;
    LineSegment ls3 = new LineSegment(a, middlepoint);
    d3 = ls3.angle();
    double Bis = 90.0D - d3 * 57.295779513082323D;
    double Bisector = Bis;
    if (Bis < 0.0D)
      Bisector = Bis + 360.0D;
    return Bisector;
  }

  /**
   * Length of radius of a circle/circular sector
   *
   * @param coordinates list of coordinates
   * @return distance
   */
  public static double distanceRadius(List<Coordinate> coordinates) {
    int size = coordinates.size();
    if (size <= 1)
      return 0.0D;
    Coordinate a = coordinates.get(size - 2);
    Coordinate b = coordinates.get(size - 1);
    Arc arc = new Arc(a, b, fullAngle);
    Geometry middlepoint = arc.getMiddlePointArc();
    Coordinate d = middlepoint.getCoordinates()[0];
    LineSegment ls3 = new LineSegment(a, d);
    return ls3.getLength();
  }

  /////////////////////////////////////////////////////////////////////////////
  // Following code calculates area and distance using geographic coordinates//
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Geographic Distance Giuseppe Aruta 2013-11-22 Length module - Thaddeus
   * Vicenty Distance calculates the geodetic curve between two points on the
   * ellipsoid given the coordinates of the two points (lon1, lat1) and (lon2,
   * lat2) Thaddeus Vicenty's inverse formula (
   * http://en.wikipedia.org/wiki/Vincenty%27s_formulae)
   * http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
   *
   * @param lat1 latitude of first coordinate
   * @param lon1 longitude of first cordinate
   * @param lat2 latitude of second coordinate
   * @param lon2 longitude of second coordinate
   * @return distance
   */
  public static double computeGeographicLenght(double lat1, double lon1, double lat2, double lon2) {

    double L = Math.toRadians(lon2 - lon1);
    double U1 = Math.atan((1 - Flattening) * Math.tan(Math.toRadians(lat1)));
    double U2 = Math.atan((1 - Flattening) * Math.tan(Math.toRadians(lat2)));
    double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
    double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);
    double sinLambda, cosLambda, sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;
    double lambda = L, lambdaP, iterLimit = 100;
    do {
      sinLambda = Math.sin(lambda);
      cosLambda = Math.cos(lambda);
      sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
          + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
      if (sinSigma == 0)
        return 0; // co-incident points
      cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
      sigma = Math.atan2(sinSigma, cosSigma);
      sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
      cosSqAlpha = 1 - sinAlpha * sinAlpha;
      cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
      if (Double.isNaN(cos2SigmaM))
        cos2SigmaM = 0; // equatorial line: cosSqAlpha=0 (§6)
      double C = Flattening / 16 * cosSqAlpha * (4 + Flattening * (4 - 3 * cosSqAlpha));
      lambdaP = lambda;
      lambda = L + (1 - C) * Flattening * sinAlpha
          * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);
    if (iterLimit == 0)
      return Double.NaN; // formula failed to converge
    double uSq = cosSqAlpha * (SemiMajorAxis * SemiMajorAxis - SemiMinorAxis * SemiMinorAxis)
        / (SemiMinorAxis * SemiMinorAxis);
    double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
    double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
    double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)
        - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
    double distance = SemiMinorAxis * A * (sigma - deltaSigma);

    return distance;

  }

  /**
   * Geographic Area Giuseppe Aruta - 2013-11-22 Area module calculates an area on
   * a sphere giving a list of geographic coordinates. Modified from "Some
   * Algorithms for Polygons on a Sphere" (Robert. G. Chamberlain and William H.
   * Duquette, NASA JPL Publication 07-03)
   * http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409
   * <p>
   * This algorithm uses local geocentric axis Experimental. Not used for
   * MeasureTool plugin
   *
   * @param coordinates list of coordinates
   * @return area
   */
  private static double computeGeographicArea2(List<Coordinate> coordinates) {
    double A1 = 0.0D;
    double A2 = 0.0D;
    double B1 = 0.0D;
    double B2 = 0.0D;
    Coordinate a = coordinates.get(0);

    double ay = Math.tan(a.y);

    // Calculate geocentric latitude from geodetic one//
    // geodetic and geocentric latitude are the same at poles and equator//
    double fy = Math.atan((1 - SqFirsteccentricity()) * Math.tan(a.y));

    double K = (1 + Flattening * (1 + Math.cos(a.y)));

    double SemiMajorAxis2 = Math.pow(SemiMajorAxis, 2);
    double SemiMinorAxis2 = Math.pow(SemiMinorAxis, 2);

    A1 += Math.pow((SemiMajorAxis2 * Math.cos(ay)), 2);
    B1 += Math.pow((SemiMinorAxis2 * Math.sin(ay)), 2);

    A2 += Math.pow((SemiMajorAxis * Math.cos(ay)), 2);
    B2 += Math.pow((SemiMinorAxis * Math.sin(ay)), 2);

    // / Local geocentric axis///
    double axes = Math.sqrt((A1 + B1) / (A2 + B2));

    // Geodetic axis as function of geodetic (f) and geographic (a) latitude
    //double geodeticAxes = axes * Math.cos(fy) / Math.cos(ay);

    if (coordinates.size() < 3)
      return 0.0D;
    double sum = 0.0D;
    for (int i = 0; i < coordinates.size(); i++) {
      Coordinate b = coordinates.get(i);
      int nexti = i + 1;
      if (nexti > coordinates.size() - 1)
        nexti = 0;
      Coordinate c = coordinates.get(nexti);

      double cx = Math.toRadians(c.x);
      double bx = Math.toRadians(b.x);
      double cy = Math.toRadians(c.y);
      double by = Math.toRadians(b.y);

      sum += (cx - bx) * (2 + Math.sin(by) + Math.sin(cy));
    }
    double signedArea = Math.abs(sum * axes * axes / 2) * K;
    if (signedArea >= 0.0D)
      return signedArea;
    return -signedArea;
  }

  /**
   * Geographic Area Giuseppe Aruta - 2013-11-22. Area module calculates an area
   * on a sphere giving a list of geographic coordinates formulas come from "Some
   * Algorithms for Polygons on a Sphere" (Robert. G. Chamberlain and William H.
   * Duquette, NASA JPL Publication 07-03)
   * http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409 It uses mean axes of
   * Earth (WGS84) as radius of the sphere.
   *
   * @param coordinates list of coordinates
   * @return area
   */
  private static double computeGeographicArea(List<Coordinate> coordinates) {

    if (coordinates.size() < 3)
      return 0.0D;
    double sum = 0.0D;
    for (int i = 0; i < coordinates.size(); i++) {
      Coordinate b = coordinates.get(i);
      int nexti = i + 1;
      if (nexti > coordinates.size() - 1)
        nexti = 0;
      Coordinate c = coordinates.get(nexti);
      double cx = Math.toRadians(c.x);
      double bx = Math.toRadians(b.x);
      double cy = Math.toRadians(c.y);
      double by = Math.toRadians(b.y);

      sum += (cx - bx) * (2 + Math.sin(by) + Math.sin(cy));
    }
    double signedArea = Math.abs(sum * MeanAxis * MeanAxis / 2);
    if (signedArea >= 0.0D)
      return signedArea;
    return -signedArea;
  }

  /**
   * Giuseppe Aruta - 2013-11-22 Bearing module returns Bearing (Azimuth) between
   * two points knowing geographic lat/lon coordinates
   *
   * @param lat1 - Latitude in decimal degrees of point 1
   * @param lon1 - Longitude in decimal degrees of point 1
   * @param lat2 - Latitude in decimal degrees of point 2
   * @param lon2 - Longitude in decimal degrees of point 2
   * @return
   */
  public static double computeGeographicNorth(double lat1, double lon1, double lat2, double lon2) {

    lat1 = Math.toRadians(lat1);
    lon1 = Math.toRadians(lon1);
    lat2 = Math.toRadians(lat2);
    lon2 = Math.toRadians(lon2);

    double dLon = lon2 - lon1;

    double y = Math.sin(dLon) * Math.cos(lat2);
    double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
    double brng = Math.toDegrees(Math.atan2(y, x));
    return (brng + 360) % 360;
  }

}