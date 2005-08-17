/*
 * Created on Aug 16, 2005
 */
package tod.plugin.launch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

import reflex.ide.eclipse.launcher.AbstractReflexLaunchConfigurationDelegate;
import reflex.lib.logging.core.api.config.StaticConfig;
import reflex.lib.logging.core.impl.mop.Config;
import tod.plugin.TODPlugin;

public class TODLaunchDelegate extends AbstractReflexLaunchConfigurationDelegate
{

	/**
	 * Force "run" mode.
	 */
	public void launch(ILaunchConfiguration aConfiguration, String aMode, ILaunch aLaunch, IProgressMonitor aMonitor)
	throws CoreException
	{
		super.launch(aConfiguration, ILaunchManager.DEBUG_MODE, aLaunch, aMonitor);
	}
	
	@Override
	protected Collection<String> getConfigClassNames(ILaunchConfiguration aConfiguration) throws CoreException
	{
		return Collections.singletonList(Config.class.getName());
	}

	/**
	 * Append connection information.
	 */
	@Override
	protected Map<String, String> getAdditionalSystemProperties(ILaunchConfiguration aConfiguration) throws CoreException
	{
		Map<String, String> theMap = super.getAdditionalSystemProperties(aConfiguration);
		
		theMap.put(StaticConfig.PARAM_COLLECTOR_PORT, ""+TODPlugin.getDefault().getSession().getPort());
		theMap.put(StaticConfig.PARAM_LOGGING_WORKINGSET, "[-java.** -javax.**]");
		theMap.put(StaticConfig.PARAM_IDENTIFICATION_WORKINGSET, "[-java.** -javax.**]");
		
		return theMap;
	}
	
	@Override
	protected List<String> getAdditionalClassPathEntries(ILaunchConfiguration aConfiguration) throws CoreException
	{
		List<String> theEntries = super.getAdditionalClassPathEntries(aConfiguration);
		theEntries.add(System.getProperty("logminer.path", TODPlugin.getDefault().getLibraryPath()+"/logminer.jar"));
		
		return theEntries;
	}
	
	@Override
	protected boolean includeReflexInClasspath(ILaunchConfiguration aConfiguration) throws CoreException
	{
		return true;
	}
}
