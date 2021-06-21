package org.openjump.core.ui.plugin.measuretoolbox.scale;


import org.openjump.core.ui.plugin.measuretoolbox.utils.CoordinateListMetrics_extended;

import com.vividsolutions.jump.workbench.ui.Viewport;

/**
 * Modified and renamed to work with Geographic coordinates (EPSG 4326);
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class GeoScreenScale {

  // Some refactoring needed here, resolution should not be static

  // [mmichaud 2013-03-27] Toolkit.getDefaultToolkit().getScreenResolution()
  // does not return the correct value as it does not know the physical
  // screen size.
  // On modern computers, 96 ppi is a good approximation when screen is
  // at full resolution, while resolution returned by Toolkit is 120;
  // Moreover, it seems that changing the screen resolution does not
  // change the value returned by Toolkit.getDefaultToolkit().getScreenResolution()
  //double SCREENRES = Toolkit.getDefaultToolkit().getScreenResolution(); //72 dpi or 96 dpi or ..

  private static int resolution = 96;

  public static int getResolution() {
    return resolution;
  }

  public static void setResolution(int res) {
    resolution = res;
  }

  /**
   * Delivers the scale of the map shown on the display. The scale is
   * calculated for the horizontal map direction<p>
   * note: The scale may differ for horizontal and vertical direction
   * due to the type of map projection.
   *
   * @param port the viewport
   * @return actual scale
   */
  public static double getHorizontalMapScale(Viewport port) {

    double horizontalScale = 0;

    double INCHTOCM = 2.54; //cm

    double panelWidth = port.getPanel().getWidth(); //pixel

    //double modelWidth = port.getEnvelopeInModelCoordinates().getWidth(); //m

    double minx = port.getEnvelopeInModelCoordinates().getMinX();
    double maxx = port.getEnvelopeInModelCoordinates().getMaxX();
    double miny = port.getEnvelopeInModelCoordinates().getMinY();
    double maxy = port.getEnvelopeInModelCoordinates().getMaxY();
    double modelWidth = CoordinateListMetrics_extended.computeGeographicLenght(miny, maxx, miny, minx);
    // double modelWidth = Formula.VicentyDistance(miny, maxx, miny, minx);
    //-----
    // example:
    // screen resolution: 72 dpi
    // 1 inch = 2.54 cm
    // ratio = 2.54/72 (cm/pix) ~ 0.35mm
    // mapLength[cm] = noPixel * ratio
    // scale = realLength *100 [m=>cm] / mapLength
    //-----
    horizontalScale = modelWidth * 100 / (INCHTOCM / resolution * panelWidth);

    return horizontalScale;
  }

}
