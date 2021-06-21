package org.openjump.core.ui.plugin.extension;

import com.vividsolutions.jump.I18N;
import org.openjump.core.ui.plugin.measuretoolbox.plugins.ToolboxMeasurePlugIn;
import org.openjump.core.ui.plugin.measuretoolbox.scale.G_InstallShowScalePlugIn;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class MeasureExtension extends Extension {

	
    private static final String NAME = "Measure Extension for OpenJUMP(Giuseppe Aruta - http://sourceforge.net/projects/opensit/ - giuseppe_aruta@yahoo.it)";
    private static final String VERSION = "1.0 (2015-01-09)";
    static { I18N.setClassLoader(MeasureExtension.class.getClassLoader());}
    public static I18N I18NPlug = I18N.getInstance("org.openjump.core.ui.plugin.measuretoolbox");

    @Override
	public String getName() {
        return NAME;
    }

    @Override
	public String getVersion() {
        return VERSION;
    }

    @Override
	public void configure(PlugInContext context) throws Exception {
       
    	
        new G_InstallShowScalePlugIn().initialize(context);
      
        new ToolboxMeasurePlugIn().initialize(context);

    }
}
