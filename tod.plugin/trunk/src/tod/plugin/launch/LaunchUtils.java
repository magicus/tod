/*
 * Created on Jun 8, 2007
 */
package tod.plugin.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import tod.core.config.TODConfig;
import tod.core.session.ConnectionInfo;
import tod.core.session.ISession;
import tod.plugin.DebuggingSession;
import tod.plugin.SourceRevealer;
import tod.plugin.TODPlugin;
import tod.plugin.TODSessionManager;
import zz.utils.Utils;

public class LaunchUtils 
{
	public static final String MODE = ILaunchManager.DEBUG_MODE;

	private static final ThreadLocal<LaunchInfo> itsInfo = new ThreadLocal<LaunchInfo>();

	public static boolean setup(
			SourceRevealer aSourceRevealer,
			ILaunchConfiguration aConfiguration, 
			ILaunch aLaunch) throws CoreException
	{
		TODConfig theConfig = TODConfigLaunchTab.readConfig(aConfiguration);
		ISession theSession = TODSessionManager.getInstance().getSession(
				aLaunch,
				aSourceRevealer,
				theConfig);
		
		itsInfo.set(new LaunchInfo(theSession, aConfiguration));
		
		return theSession != null;
	}
	
	public static void tearDown()
	{
		itsInfo.set(null);
	}
	
	public static IVMRunner getVMRunner(IVMRunner aDelegate) 
	{
		return new DelegatedRunner(aDelegate, itsInfo.get());
	}
	
	protected static List<String> getAdditionalVMArguments(LaunchInfo aInfo) throws CoreException
	{
        List<String> theArguments = new ArrayList<String>();
        
        // Boot class path
		String theAgentPath = System.getProperty(
				"agent.path",
				TODPlugin.getDefault().getLibraryPath()+"/tod-agent.jar");
        
        theArguments.add ("-Xbootclasspath/p:"+theAgentPath);
		
        
		String theLibraryPath = TODPlugin.getDefault().getLibraryPath();
		
		// Native agent
		String theNativeAgentPath = System.getProperty(
				"bcilib.path",
				theLibraryPath+"/libbci-agent.so");
		
		theArguments.add("-agentpath:"+theNativeAgentPath);
		theArguments.add("-Djava.library.path="+theLibraryPath);
		
		theArguments.add("-noverify");
		
		// Config
		TODConfig theConfig = TODConfigLaunchTab.readConfig(aInfo.config);
		
		ConnectionInfo theConnectionInfo = aInfo.session.getConnectionInfo();
		
		theArguments.add("-Dcollector-host="+theConnectionInfo.getHostName());
		theArguments.add("-Dcollector-port="+theConnectionInfo.getLogReceiverPort());
		theArguments.add("-Dnative-port="+theConnectionInfo.getNativePort());
		
		theArguments.add("-Dtod-host="+theConfig.get(TODConfig.CLIENT_HOST_NAME));
		
		return theArguments;
	}
	
	private static class LaunchInfo
	{
		public final ISession session;
		public final ILaunchConfiguration config;
		
		public LaunchInfo(ISession aSession, ILaunchConfiguration aConfig)
		{
			session = aSession;
			config = aConfig;
		}
	}


	private static class DelegatedRunner implements IVMRunner
	{
		private IVMRunner itsDelegate;
		private LaunchInfo itsInfo;
		
		public DelegatedRunner(IVMRunner aDelegate, LaunchInfo aInfo)
		{
			itsDelegate = aDelegate;
			itsInfo = aInfo;
		}

		public void run(
				VMRunnerConfiguration aConfiguration, 
				ILaunch aLaunch, 
				IProgressMonitor aMonitor)
				throws CoreException
		{
			List<String> theAdditionalArgs = getAdditionalVMArguments(itsInfo);
			List<String> theArgs = new ArrayList<String>();
			Utils.fillCollection(theArgs, theAdditionalArgs);
			Utils.fillCollection(theArgs, aConfiguration.getVMArguments());
			
			String[] theFullArgs = theArgs.toArray(new String[theArgs.size()]);
			aConfiguration.setVMArguments(theFullArgs);
			
			itsDelegate.run(aConfiguration, aLaunch, aMonitor);
		}
	}
}
