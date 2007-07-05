/*
 * Created on Jun 8, 2007
 */
package tod.plugin.ajdt.launch;

import org.eclipse.ajdt.internal.launching.AJApplicationLaunchConfigurationDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import tod.plugin.GenericSourceRevealer;
import tod.plugin.SourceRevealer;
import tod.plugin.launch.LaunchUtils;


/**
 * Launch delegate for launch type: org.eclipse.jdt.launching.localJavaApplication
 * @author gpothier
 */
public class TODLaunchDelegate_AJDT extends AJApplicationLaunchConfigurationDelegate
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
			IJavaProject theJavaProject = getJavaProject(aConfiguration);
			SourceRevealer theRevealer = new GenericSourceRevealer(aLaunch, theJavaProject);
			if (LaunchUtils.setup(theRevealer, aConfiguration, aLaunch))
			{
				super.launch(aConfiguration, LaunchUtils.MODE, aLaunch, aMonitor);
			}
		}
		finally
		{
			LaunchUtils.tearDown();
		}
	}
}
