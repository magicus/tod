/*
 * Created on Jun 8, 2007
 */
package tod.plugin.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.pde.ui.launcher.EclipseApplicationLaunchConfiguration;
import org.eclipse.pde.ui.launcher.JUnitLaunchConfigurationDelegate;


/**
 * Launch delegate for PDE unit tests
 * @author gpothier
 */
public class TODLaunchDelegate_PDE_JUnit
extends JUnitLaunchConfigurationDelegate
{
	@Override
	public IVMRunner getVMRunner(ILaunchConfiguration aConfiguration, String aMode) throws CoreException
	{
		return LaunchUtils.getVMRunner(super.getVMRunner(aConfiguration, aMode));
	}
	
	@Override
	public void launch(ILaunchConfiguration aConfiguration, String aMode, ILaunch aLaunch, IProgressMonitor aMonitor)
			throws CoreException
	{
		try
		{
			IProject[] theProjects = getProjectsForProblemSearch(aConfiguration, aMode);
			if (LaunchUtils.setup(theProjects, TODConfigLaunchTab.readConfig(aConfiguration), aLaunch))
			{
				super.launch(aConfiguration, LaunchUtils.getLaunchMode(aConfiguration), aLaunch, aMonitor);
			}
		}
		finally
		{
			LaunchUtils.tearDown();
		}
	}
}
