/*
 * Created on Jun 8, 2007
 */
package tod.plugin.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.pde.ui.launcher.EclipseApplicationLaunchConfiguration;

import tod.plugin.GenericSourceRevealer;
import tod.plugin.SourceRevealer;


/**
 * Launch delegate for configuration type: org.eclipse.pde.ui.RuntimeWorkbench
 * @author gpothier
 */
public class TODLaunchDelegate_PDE_EclipseApplication
extends EclipseApplicationLaunchConfiguration
{
	@Override
	public IVMRunner getVMRunner(ILaunchConfiguration aConfiguration, String aMode) throws CoreException
	{
		return TODLaunchDelegate_Base.getVMRunner(super.getVMRunner(aConfiguration, aMode));
	}
	
	@Override
	public void launch(ILaunchConfiguration aConfiguration, String aMode, ILaunch aLaunch, IProgressMonitor aMonitor)
			throws CoreException
	{
		try
		{
			IProject[] theProjects = getProjectsForProblemSearch(aConfiguration, aMode);
			SourceRevealer theRevealer = new GenericSourceRevealer(aLaunch, theProjects);
			if (TODLaunchDelegate_Base.setup(theRevealer, aConfiguration, aLaunch))
			{
				super.launch(aConfiguration, TODLaunchDelegate_Base.MODE, aLaunch, aMonitor);
			}
		}
		finally
		{
			TODLaunchDelegate_Base.tearDown();
		}
	}
}
