package org.openjump.core.ui.plugin.measuretoolbox.plugins;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import org.openjump.core.ui.plugin.measuretoolbox.icons.IconLoader;
//import org.openjump.core.ui.plugin.measuretoolbox.language.I18NPlug;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;

/**
 * clean measurements
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class CleanMeasurePlugIn extends AbstractPlugIn {

  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasurePlugin.CleanMeasurePlugIn.name");

  public static final Icon ICON = IconLoader.icon("cross.png");

  public static final String LAYER_NAME = I18NPlug.get("MeasureToolbox.layer");

  //Geometry measureGeometry = null;
  //double area;
  //double distance;

  @Override
  public void initialize(PlugInContext context) {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    FeatureInstaller featureInstaller = new FeatureInstaller(
        workbenchContext);
    JPopupMenu popupMenu = LayerViewPanel.popupMenu();
    featureInstaller.addPopupMenuPlugin(popupMenu, this, getName(), false,
        null, // to do: add icon
        createEnableCheck(workbenchContext));
  }

  @Override
  public boolean execute(final PlugInContext context) throws Exception {
    reportNothingToUndoYet(context);
    final Layer measureLayer = context.getLayerManager().getLayer(
        LAYER_NAME);
    final String catName = StandardCategoryNames.SYSTEM;
    if (measureLayer == null) {
      return false;

    } else {


      UndoableCommand cmd = new UndoableCommand(getName()) {
        @Override
        public void execute() {

          context.getLayerManager().remove(measureLayer);
        }

        @Override
        public void unexecute() {
          context.getLayerManager().addLayerable(catName,
              measureLayer);


        }
      };
      execute(cmd, context);
      return true;

    }

  }

  public static MultiEnableCheck createEnableCheck(
      WorkbenchContext workbenchContext) {
    EnableCheckFactory checkFactory = new EnableCheckFactory(
        workbenchContext);

    return new MultiEnableCheck()
        .add(checkFactory
            .createWindowWithSelectionManagerMustBeActiveCheck())
        .add(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(1))
        .add(checkFactory
            .createExactlyOneSelectedLayerMustBeEditableCheck());
  }

  public Icon getIcon() {
    return ICON;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
