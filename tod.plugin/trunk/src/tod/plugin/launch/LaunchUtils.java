/*
 * Created on Jun 8, 2007
 */
package tod.plugin.launch;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import tod.core.config.DeploymentConfig;
import tod.core.config.TODConfig;
import tod.core.session.ConnectionInfo;
import tod.core.session.ISession;
import tod.core.session.SessionCreationException;
import tod.core.session.TODSessionManager;
import tod.plugin.EclipseProgramLaunch;
import tod.plugin.TODPlugin;
import tod.plugin.TODPluginUtils;
import tod.plugin.views.AbstractNavigatorView;
import zz.utils.Utils;

public class LaunchUtils 
{
	public static final String MODE = ILaunchManager.DEBUG_MODE;

	private static final ThreadLocal<LaunchInfo> itsInfo = new ThreadLocal<LaunchInfo>();

	public static boolean setup(
			IJavaProject aJavaProject,
			ILaunchConfiguration aConfiguration, 
			ILaunch aLaunch) throws CoreException
	{
		return setup(new IProject[] {aJavaProject.getProject()}, aConfiguration, aLaunch);
	}
	
	public static boolean setup(
			IProject[] aProjects,
			ILaunchConfiguration aConfiguration, 
			ILaunch aLaunch) throws CoreException
{
		TODConfig theConfig = TODConfigLaunchTab.readConfig(aConfiguration);
		AbstractNavigatorView theView = TODPluginUtils.getTraceNavigatorView(true);
		
		ISession theSession = null;
		try
		{
			theSession = TODSessionManager.getInstance().getSession(
					theView.getGUIManager(),
					theConfig,
					new EclipseProgramLaunch(aLaunch, aProjects));
		}
		catch (Exception e)
		{
			TODPlugin.logError("Could not create session", e);
			handleException(theConfig, e);
		}
		
		itsInfo.set(new LaunchInfo(theSession, aConfiguration));
		
		return theSession != null;
	}
	
	private static void handleException(TODConfig aConfig, Exception e)
	{
		ConnectException theConnectException = Utils.findAncestorException(ConnectException.class, e);
		if (theConnectException != null) 
		{
			msgConnectionProblem(aConfig);
			return;
		}
		
		SessionCreationException theSessionCreationException = Utils.findAncestorException(SessionCreationException.class, e);
		if (theSessionCreationException != null)
		{
			msgProblem("Cannot create session", e.getMessage());
			return;
		}
		
		throw new RuntimeException(e);
	}
	
	public static void tearDown()
	{
		itsInfo.set(null);
	}
	
	private static void msgConnectionProblem(TODConfig aConfig)
	{
		String theMessage;
		String theSessionType = aConfig.get(TODConfig.SESSION_TYPE);
		
		if (TODConfig.SESSION_REMOTE.equals(theSessionType))
		{
			theMessage = "No debugging session could be created because of a connection " +
			"error. Check that the database host settings are correct, " +
			"and that the database is up and running.";
		}
		else if (TODConfig.SESSION_LOCAL.equals(theSessionType))
		{
			theMessage = "Could not connect to the local database session. " +
					"This could be caused by a timeout error " +
					"if your machine is under load, please retry. " +
					"If the error persists check the Eclipse log.";
		}
		else 
		{
			theMessage = "Undertermined connection problem. " +
					"Check Eclipse log for details.";
		}
		
		msgProblem("Cannot connect", theMessage);
	}
		
	/**
	 * Displays an error message
	 */
	private static void msgProblem(final String aTitle, final String aMessage)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				Shell theShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(theShell, aTitle, aMessage);
			}
		});
	}
	
	/**
	 * Returns the session created by {@link #setup(IJavaProject, ILaunchConfiguration, ILaunch)}
	 * in this thread.
	 */
	public static ISession getSession()
	{
		return itsInfo.get().session;
	}
	
	public static IVMRunner getVMRunner(IVMRunner aDelegate) 
	{
		return new DelegatedRunner(aDelegate, itsInfo.get());
	}
	
	protected static List<String> getAdditionalVMArguments(LaunchInfo aInfo) throws CoreException
	{
		String theLibraryPath = TODPlugin.getDefault().getLibraryPath();

		List<String> theArguments = new ArrayList<String>();
        
        // Boot class path
		String theAgentPath = System.getProperty(
				"agent.path",
				theLibraryPath+"/tod-agent.jar");
        
        theArguments.add ("-Xbootclasspath/p:"+theAgentPath);
		
		// Native agent
        String theLibName = null;
        String theAgentName = DeploymentConfig.getNativeAgentName();
		String theOs = System.getProperty("os.name");
		if (theOs.contains("Windows")) theLibName = theAgentName+".dll";
		else if (theOs.contains("Mac")) theLibName = "lib"+theAgentName+".dylib";
		else theLibName = "lib"+theAgentName+".so";

		String theNativeAgentPath = System.getProperty(
				"bcilib.path",
				theLibraryPath+"/"+theLibName);
		
		theArguments.add("-agentpath:"+theNativeAgentPath);
		theArguments.add("-Djava.library.path="+theLibraryPath);
		
		theArguments.add("-noverify");
		
		// Config
		TODConfig theConfig = TODConfigLaunchTab.readConfig(aInfo.config);
		
		ConnectionInfo theConnectionInfo = aInfo.session.getConnectionInfo();
		
		theArguments.add("-Dcollector-host="+theConnectionInfo.getHostName());
		theArguments.add("-Dcollector-port="+theConnectionInfo.getPort());
/*	    if (TODConfig.SESSION_LOCAL.equals(theConfig.get(TODConfig.SESSION_TYPE)))
			theArguments.add("-Djava.rmi.server.hostname=127.0.0.1");
		else theArguments.add("-Djava.rmi.server.hostname="+theConnectionInfo.getHostName());
	*/
		theArguments.add(TODConfig.CLIENT_NAME.javaOpt(theConfig));
		theArguments.add(TODConfig.AGENT_CACHE_PATH.javaOpt(theConfig));
		theArguments.add(TODConfig.AGENT_VERBOSE.javaOpt(theConfig));
		
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
