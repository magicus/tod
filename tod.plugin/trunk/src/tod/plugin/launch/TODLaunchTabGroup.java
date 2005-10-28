/*
 * Created on Jun 16, 2005
 */
package tod.plugin.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;

import reflex.ide.eclipse.launcher.options.OptionsTab;

public class TODLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {

	/**
	 * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new JavaMainTab(),
				new JavaArgumentsTab(),
//				new MyOptionsTab(),
				new JavaJRETab(),
				new JavaClasspathTab(),
				new SourceLookupTab(),
				new EnvironmentTab(),
				new CommonTab()
			};
			setTabs(tabs);
	}
	
//	private static class MyOptionsTab extends OptionsTab
//	{
//		/**
//		 * We don't require the project to be a reflex project.
//		 */
//		@Override
//		protected void checkReflexProject(ILaunchConfiguration aLaunchConfiguration) throws InvalidLaunchConfiguration
//		{
//		}
//	}

}