package org.openjump.core.ui.plugin.measuretoolbox.plugins;

import javax.swing.ImageIcon;

import org.openjump.core.ui.plugin.measuretoolbox.cursortools.MeasureAreaTool;
import org.openjump.core.ui.plugin.measuretoolbox.cursortools.MeasureRectangleTool;
import org.openjump.core.ui.plugin.measuretoolbox.icons.IconLoader;
//import org.openjump.core.ui.plugin.measuretoolbox.language.I18NPlug;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.cursortool.OrCompositeTool;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;

import static org.openjump.core.ui.plugin.extension.MeasureExtension.I18NPlug;


/**
 * measures area and perimeter
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class MeasureAreaPlugIn extends AbstractPlugIn {

  public static ImageIcon ICON = IconLoader.icon("Ruler_polygon.gif");

  @Override
  public boolean execute(PlugInContext context) throws Exception {
    reportNothingToUndoYet(context);

    context.getLayerViewPanel().setCurrentCursorTool(QuasimodeTool.createWithDefaults((new OrCompositeTool() {
      public String getName() {
        return "Test";
      }
    }.add(new MeasureRectangleTool(context)).add(new MeasureAreaTool(context)))));

    return true;
  }

  public ImageIcon getIcon() {
    return ICON;
  }

  public static final String NAME = I18NPlug
      .get("MeasureToolbox.MeasureTools.Area");

  @Override
  public String getName() {
    return NAME;
  }

  public static MultiEnableCheck createEnableCheck(
      WorkbenchContext workbenchContext) {
    EnableCheckFactory checkFactory = new EnableCheckFactory(
        workbenchContext);

    return new MultiEnableCheck().add(checkFactory
        .createWindowWithSelectionManagerMustBeActiveCheck());
  }
}
