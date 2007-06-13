/*
 * Created on Jun 8, 2007
 */
package tod.plugin.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IVMRunner;

import tod.plugin.GenericSourceRevealer;
import tod.plugin.SourceRevealer;


/**
 * Launch delegate for config type: org.eclipse.jdt.junit.launchconfig
 * @author gpothier
 */
public class TODLaunchDelegate_JDT_JUnit extends JUnitLaunchConfigurationDelegate
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
			IJavaProject theJavaProject = getJavaProject(aConfiguration);
			SourceRevealer theRevealer = new GenericSourceRevealer(aLaunch, theJavaProject);
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
