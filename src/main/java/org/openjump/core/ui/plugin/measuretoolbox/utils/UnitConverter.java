package org.openjump.core.ui.plugin.measuretoolbox.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.openjump.core.ui.plugin.measuretoolbox.plugins.ToolboxMeasurePlugIn;

/**
 * This class
 * a) defines conversion parameters between SI and non SI Units
 * (lenghtConverter and AreaConverted)
 * b) define text unit of the map (mapLengthUnit and (mapAreaUnit)
 * c) define text unit of the measurements (measureLengthUnit and (measureAreaUnit)
 * d) define an adaptative form for decimal number (decimalformat (double length), from
 * Michaël Michaud)
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class UnitConverter {

  /**
   * Length converter
   *
   * @return
   */
  public double lengthConverter() {
    String target = ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString();
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      return 1 * MeasureUnit();
    } else if ((ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals(target)

    )) {
      return 1;
    } else
      return MapUnit() * MeasureUnit();
  }

  /**
   * Area converter
   *
   * @return
   */
  public double areaConverter() {
    String target = ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString();
    if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
      return 1 * Math.pow(MeasureUnit(), 2);
    } else if ((ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals(target)

    )) {
      return 1;
    } else
      return Math.pow(MeasureUnit() * MapUnit(), 2);
  }


  public double MapUnit() {

    double mu = 0;
    if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("m"))
      mu = 1;
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("mm"))
      mu = 0.001;
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("cm"))
      mu = 0.01;
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("dm"))
      mu = 0.1;
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("km"))
      mu = 1000.00;
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("ft"))
      mu = 0.3048;
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("US ft"))
      mu = 0.3048006096;
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("in"))
      mu = 0.0254;
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("yd"))
      mu = 0.9144;
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("mi"))
      mu = 1609.344;
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("nmi"))
      mu = 1852.00;
    return mu;
  }


  public double MeasureUnit() {

    double mu = 0;
    if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("m"))
      mu = 1;
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("mm"))
      mu = 1 / 0.001;
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("cm"))
      mu = 1 / 0.01;
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("dm"))
      mu = 1 / 0.1;
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("km"))
      mu = 1 / 1000.00;
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("ft"))
      mu = 1 / 0.3048;
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("US ft"))
      mu = 1 / 0.3048006096;
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("in"))
      mu = 1 / 0.0254;
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("yd"))
      mu = 1 / 0.9144;
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("mi"))
      mu = 1 / 1609.344;
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("nmi"))
      mu = 1 / 1852.00;
    return mu;
  }


  public String measuredLengthUnit() {
    String mu = null;
    if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("m"))
      mu = " m";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("mm"))
      mu = " mm";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("cm"))
      mu = " cm";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("dm"))
      mu = " dm";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("km"))
      mu = " km";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("ft"))
      mu = " ft";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("US ft"))
      mu = " ft (US)";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("in"))
      mu = " in";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("yd"))
      mu = " yd";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("mi"))
      mu = " mi";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("nmi"))
      mu = " nmi";
    return mu;
  }

  public String measuredAreaUnit() {
    String mu = null;
    if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("m"))
      mu = " m\u00B2";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("mm"))
      mu = " mm\u00B2";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("cm"))
      mu = " cm\u00B2";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("dm"))
      mu = " dm\u00B2";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("km"))
      mu = " km\u00B2";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("ft"))
      mu = " ft\u00B2";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("US ft"))
      mu = " ft\u00B2 (US)";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("in"))
      mu = " in\u00B2";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("yd"))
      mu = " yd\u00B2";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("mi"))
      mu = " mi\u00B2";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("nmi"))
      mu = " nmi\u00B2";
    return mu;
  }

  public String measureUnit() {
    String mu = null;
    if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("m"))
      mu = " meter";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("mm"))
      mu = " millimeter";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("cm"))
      mu = " centimeter";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("dm"))
      mu = " decimeter";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("km"))
      mu = " kilometer";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("ft"))
      mu = " foot";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("US ft"))
      mu = " US survey foot";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("in"))
      mu = " inch";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("yd"))
      mu = " yard";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("mi"))
      mu = " mile";
    else if (ToolboxMeasurePlugIn.measureCombo.getSelectedItem().toString().equals("nmi"))
      mu = " nautic mile";
    return mu;
  }


  /**
   * Map length abbreviation (m, ft, etc)
   *
   * @return
   */

  public String mapLengthUnit() {
    String mu = null;
    if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("m"))
      mu = " m";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("mm"))
      mu = " mm";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("cm"))
      mu = " cm";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("dm"))
      mu = " dm";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("km"))
      mu = " km";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("ft"))
      mu = " ft";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("US ft"))
      mu = " ft (US)";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("in"))
      mu = " in";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("yd"))
      mu = " yd";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("mi"))
      mu = " mi";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("nmi"))
      mu = " nmi";
    return mu;
  }

  /**
   * Map area  abbreviation (m^2 etc)
   *
   * @return
   */
  public String mapAreaUnit() {
    String mu = null;
    if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("m"))
      mu = " m\u00B2";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("mm"))
      mu = " mm\u00B2";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("cm"))
      mu = " cm\u00B2";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("dm"))
      mu = " dm\u00B2";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("km"))
      mu = " km\u00B2";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("ft"))
      mu = " ft\u00B2";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("US ft"))
      mu = " ft\u00B2 (US)";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("in"))
      mu = " in\u00B2";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("yd"))
      mu = " yd\u00B2";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("mi"))
      mu = " mi\u00B2";
    else if (ToolboxMeasurePlugIn.mapCombo.getSelectedItem().toString().equals("nmi"))
      mu = " nmi\u00B2";
    return mu;
  }


  /**
   * Michaël Michaud - adaptive format pattern for decimal
   *
   * @param value value to format
   * @return String
   */
  public String decimalformat(double value) {
    DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
        .getInstance();
    String formatPattern;

    if (value >= 10) {
      formatPattern = "#,##0.00";
    } else if (value >= 1) {
      formatPattern = "#,##0.000";
    } else if (value >= 0.1) {
      formatPattern = "#,##0.0000";
    } else if (value >= 0.01) {
      formatPattern = "#,##0.00000";
    } else
      formatPattern = "#,##0.000000";
    decimalFormat.applyPattern(formatPattern);
    return decimalFormat.format(value);
  }

}
