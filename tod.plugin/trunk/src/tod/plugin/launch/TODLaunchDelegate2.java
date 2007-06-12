/*
 * Created on Jun 7, 2007
 */
package tod.plugin.launch;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import tod.core.config.TODConfig;
import tod.core.session.ConnectionInfo;
import tod.core.session.ISession;
import tod.plugin.TODPlugin;
import tod.plugin.TODSessionManager;
import zz.eclipse.utils.launcher.AbstractCustomLaunchConfigurationDelegate;

/**
 * This is a "delegated" delegate that err... delegates to the original
 * delegate for the launch configuration type, hacking its way with
 * reflex to add appropriate vm arguments.
 * @author gpothier
 */
public class TODLaunchDelegate2 extends AbstractCustomLaunchConfigurationDelegate
{
	private static final String MODE = ILaunchManager.DEBUG_MODE;

	private ISession itsSession;
	
	public void launch(
			ILaunchConfiguration aConfiguration, 
			String aMode, 
			ILaunch aLaunch, 
			IProgressMonitor aMonitor) throws CoreException
	{
		if (aMonitor == null) aMonitor = new NullProgressMonitor();
		aMonitor.beginTask("Launching with TOD", IProgressMonitor.UNKNOWN);
		
		if (aMonitor.isCanceled()) return;
		
		aMonitor.subTask("Obtaining delegate");
		
		ILaunchConfigurationType theType = aConfiguration.getType();
		ILaunchConfigurationDelegate theDelegate = theType.getDelegate(MODE);
		
		aMonitor.subTask("Creating session");
		
		TODConfig theConfig = TODConfigLaunchTab.readConfig(aConfiguration);
		itsSession = TODSessionManager.getInstance().getSession(
				aLaunch,
				getJavaProject(aConfiguration),
				theConfig);
		if (itsSession == null) return;
		
		try
		{
			List<String> theArgs = getAdditionalVMArguments(aConfiguration);
			ReflexLaunchHack.setArgs(theArgs.toArray(new String[theArgs.size()]));
			theDelegate.launch(aConfiguration, MODE, aLaunch, aMonitor);
		}
		catch (CoreException e)
		{
			System.out.println("Exception caught in launch, disconnecting session");
			itsSession.disconnect();
			throw e;
		}
		catch (RuntimeException e)
		{
			System.out.println("Exception caught in launch, disconnecting session");
			itsSession.disconnect();
			throw e;
		}
		finally
		{
			ReflexLaunchHack.setArgs(null);
		}
	}
	
	@Override
	protected List<String> getAdditionalVMArguments(ILaunchConfiguration aConfiguration) throws CoreException
	{
		List<String> theArguments = super.getAdditionalVMArguments(aConfiguration);
		
		String theLibraryPath = TODPlugin.getDefault().getLibraryPath();
		
		String theAgentPath = System.getProperty(
				"bcilib.path",
				theLibraryPath+"/libbci-agent.so");
		
		theArguments.add("-agentpath:"+theAgentPath);
		theArguments.add("-Djava.library.path="+theLibraryPath);
		
		theArguments.add("-noverify");
		
		TODConfig theConfig = TODConfigLaunchTab.readConfig(aConfiguration);
		
		ConnectionInfo theConnectionInfo = itsSession.getConnectionInfo();
		
		theArguments.add("-Dcollector-host="+theConnectionInfo.getHostName());
		theArguments.add("-Dcollector-port="+theConnectionInfo.getLogReceiverPort());
		theArguments.add("-Dnative-port="+theConnectionInfo.getNativePort());
		
		theArguments.add("-Dtod-host="+theConfig.get(TODConfig.CLIENT_HOST_NAME));
		
		return theArguments;
	}
	
	@Override
	protected List<String> getPrependedBootClassPathEntries(ILaunchConfiguration aConfiguration) throws CoreException
	{
		List<String> theEntries = super.getPrependedBootClassPathEntries(aConfiguration);
		
		String theAgentPath = System.getProperty(
				"agent.path",
				TODPlugin.getDefault().getLibraryPath()+"/tod-agent.jar");
		
		theEntries.add(theAgentPath);
		
		return theEntries;
	}
	

}
