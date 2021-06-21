package org.openjump.core.ui.plugin.measuretoolbox.plugins;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openjump.core.ui.plugin.measuretoolbox.icons.IconLoader;
import org.openjump.core.ui.plugin.measuretoolbox.scale.GeoShowScalePlugIn;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


/**
 * Measure Toolbox
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class ToolboxMeasurePlugIn extends ToolboxPlugIn {

  //WorkbenchContext workbenchContext;
  //public static final String LAYER_NAME = I18NPlug
  //    .getI18N("MeasureToolbox.layer");

  public static final String MAPUNIT = I18NPlug
      .get("MeasureToolbox.panel.map_unit");
  public static final String MEASUREUNIT = I18NPlug
      .get("MeasureToolbox.panel.measure_unit");

  public static final String GeographicCRS =
      I18NPlug.get("MeasureToolbox.panel.measure_in_EPSG4326");
  public static final String PlanarCRS =
      I18NPlug.get("MeasureToolbox.panel.measure_in_planar_coordinates");

  @Override
  public String getName() {
    return I18NPlug.get("MeasureToolbox.toolbox");
  }

  private JPanel centralPanel;
  private JPanel unitPanel;
  private JPanel measurePanel;
  public static JCheckBox coordinateCheck;
  public static JCheckBox saveCheck;
  public static JCheckBox scaleCheck;

  public static String[] mapUnits = {"m", "km", "ft", "US ft", "yd", "mi", "nmi"};
  public static String[] measureUnits = {"m", "km", "ft", "US ft", "yd", "mi", "nmi"};

  private static JLabel mapLabel = new JLabel(MAPUNIT);
  private static JLabel unitLabel = new JLabel(MEASUREUNIT);

  public static JComboBox mapCombo = new JComboBox(mapUnits);
  public static JComboBox measureCombo = new JComboBox(measureUnits);

  public static String[] mapCRS = {PlanarCRS, GeographicCRS};
  public static JComboBox CRSCombo = new JComboBox(mapCRS);

  public static ImageIcon ICON = IconLoader.icon("measure.png");

  public Icon getIcon() {

    return IconLoader.icon("measure.png");
  }

  @Override

  public void initialize(PlugInContext context) {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    new FeatureInstaller(workbenchContext);

    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[]{MenuNames.PLUGINS}, I18NPlug
            .get("MeasureToolbox.toolbox"), false,
        getIcon(), null);
  }

  private ToolboxDialog toolbox;

  @Override

  protected void initializeToolbox(final ToolboxDialog toolbox) {
    this.toolbox = toolbox;

    toolbox.setTitle(I18NPlug.get("MeasureToolbox.toolbox"));
    toolbox.setIconImage(ICON.getImage());
    toolbox.addToolBar();

    MeasureAreaPlugIn area = new MeasureAreaPlugIn();
    toolbox.addPlugIn(area, null, area.getIcon());

    MeasureDistancePlugIn line = new MeasureDistancePlugIn();
    toolbox.addPlugIn(line, null, line.getIcon());

    MeasureXYPlugIn eastnorth = new MeasureXYPlugIn();
    toolbox.addPlugIn(eastnorth, null, eastnorth.getIcon());

    MeasureAnglePlugIn angle = new MeasureAnglePlugIn();
    toolbox.addPlugIn(angle, null, angle.getIcon());

    MeasureAngleGoniometerPlugIn goniometer = new MeasureAngleGoniometerPlugIn();
    toolbox.addPlugIn(goniometer, null, goniometer.getIcon());

    MeasureAzimuthPlugIn azimuth = new MeasureAzimuthPlugIn();
    toolbox.addPlugIn(azimuth, null, azimuth.getIcon());

    toolbox.getToolBar().addSeparator();

    MeasureSelectFeaturePlugIn meas = new MeasureSelectFeaturePlugIn();

    toolbox.addPlugIn(meas, null, meas.getIcon());

    CleanMeasurePlugIn clean = new CleanMeasurePlugIn();
    toolbox.addPlugIn(clean, null, clean.getIcon());

    NewZoomToScalePlugIn zoom = new NewZoomToScalePlugIn();
    toolbox.addPlugIn(zoom, null, zoom.getIcon());


    toolbox.getCenterPanel().add(CentralPanel(), BorderLayout.CENTER);

    final GeoShowScalePlugIn scale = new GeoShowScalePlugIn();
    scaleCheck.addActionListener(AbstractPlugIn.toActionListener(
        scale, toolbox.getContext(), null));


    coordinateCheck.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateComponents();
        toolbox.updateEnabledState();
        toolbox.repaint();
      }
    });


    toolbox.setInitialLocation(new GUIUtil.Location(20, true, 20, false));
    toolbox.setResizable(false);
    toolbox.finishAddingComponents();
    toolbox.validate();

  }


  public boolean isGeographicCoordinate() {
    return coordinateCheck.isEnabled() && coordinateCheck.isSelected();
  }


  /*
   * Combo box of Map Units
   */
  private JPanel UnitPanel() {

    if (unitPanel == null) {

      mapCombo.setSelectedIndex(0);
      unitPanel = new JPanel(new GridBagLayout());
      FormUtils.addRowInGBL(unitPanel, 1, 0, mapLabel, mapCombo);
    }

    return unitPanel;
  }

  /*
   * Combo box of Measure Unit
   */
  private JPanel MeasurePanel() {

    measureCombo.setSelectedIndex(0);
    if (measurePanel == null) {
      measurePanel = new JPanel(new GridBagLayout());
      FormUtils.addRowInGBL(measurePanel, 1, 0, unitLabel, measureCombo);
    }
    return measurePanel;
  }


  private JPanel CentralPanel() {

    if (centralPanel == null) {
      centralPanel = new JPanel(new GridBagLayout());
      coordinateCheck = new JCheckBox(
          I18NPlug.get("MeasureToolbox.panel.measure_in_EPSG4326")); //$NON-NLS-1$

      saveCheck = new JCheckBox(
          I18NPlug.get("MeasureToolbox.panel.save_progressive_measurements")); //$NON-NLS-1$

      scaleCheck = new JCheckBox(
          I18NPlug.get("MeasureToolbox.panel.show_scale_bar")); //$NON-NLS-1$

      FormUtils.addRowInGBL(centralPanel, 2, 0, UnitPanel(), MeasurePanel());
      FormUtils.addRowInGBL(centralPanel, 3, 0, coordinateCheck);
      FormUtils.addRowInGBL(centralPanel, 4, 0, saveCheck);
      FormUtils.addRowInGBL(centralPanel, 5, 0, scaleCheck);

    }
    return centralPanel;
  }

  /*
   * Begin of code
   * If EPSG4326 CheckBox is selected,
   * Map Unit is deactivated and reset to meter
   */
  private JComboBox getmapList() {//List of map units combobox
    if (mapCombo == null) {
      mapCombo = new JComboBox();

      mapCombo.setLayout(new GridBagLayout());
    }
    return mapCombo;
  }

  private JLabel getmapLabel() {//Label of combobox
    if (mapLabel == null) {
      mapLabel = new JLabel();

      mapLabel.setLayout(new GridBagLayout());
    }
    return mapLabel;
  }

  public void updateComponents() {
    toolbox.updateEnabledState();
    mapCombo.setSelectedIndex(0);
    getmapList().setEnabled(!coordinateCheck
        .isSelected());
    getmapLabel().setEnabled(!coordinateCheck
        .isSelected());
  }

  public void updateComponents2() {
    toolbox.updateEnabledState();
    mapCombo.setSelectedIndex(0);
    getmapList().setEnabled(!coordinateCheck
        .isSelected());
    getmapLabel().setEnabled(!coordinateCheck
        .isSelected());
  }

  /*
   * End of code
   */


}
