/*
 * Created on Jun 14, 2007
 */
package tod.plugin.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;

import tod.core.config.TODConfig;
import tod.core.session.ISession;
import tod.plugin.GenericSourceRevealer;
import tod.plugin.SourceRevealer;
import tod.plugin.TODPlugin;
import tod.plugin.TODPluginUtils;
import tod.plugin.TODSessionManager;

public class ConnectToDatabaseLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate
{
	public void launch(
			ILaunchConfiguration aConfiguration, 
			String aMode, 
			ILaunch aLaunch, 
			IProgressMonitor aMonitor) throws CoreException
	{
		if (aMonitor == null) aMonitor = new NullProgressMonitor();
		
		TODConfig theConfig = TODConfigLaunchTab.readConfig(aConfiguration);
		
		theConfig.set(TODConfig.COLLECTOR_HOST, getAddress(aConfiguration));
		theConfig.set(TODConfig.SESSION_TYPE, TODConfig.SESSION_REMOTE);
		
		aMonitor.worked(1);
		IJavaProject theJavaProject = getJavaProject(aConfiguration);
		SourceRevealer theRevealer = new GenericSourceRevealer(aLaunch, theJavaProject);
		
		ISession theSession = TODSessionManager.getInstance().getSession(
				aLaunch,
				theRevealer,
				theConfig);

		TODPluginUtils.getTraceNavigatorView(true);
		aMonitor.done();
	}
	
	private static String getAddress(ILaunchConfiguration aConfig)
	{
		try
		{
			return aConfig.getAttribute(ConnectToDatabaseLaunchConstants.DATABASE_ADDRESS, "localhost");
		}
		catch (CoreException e)
		{
			TODPlugin.logError("Cannot read database address", e);
			return null;
		}
	}
	
}
