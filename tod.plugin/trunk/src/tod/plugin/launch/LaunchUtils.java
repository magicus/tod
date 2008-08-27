/*
 * Created on Jun 8, 2007
 */
package tod.plugin.launch;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall3;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
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
import tod.plugin.views.main.MainView;
import zz.utils.Utils;

public class LaunchUtils 
{
	public static final String MODE = ILaunchManager.DEBUG_MODE;

	private static final ThreadLocal<LaunchInfo> itsInfo = new ThreadLocal<LaunchInfo>();
	
	public static boolean setup(
			IJavaProject aJavaProject,
			TODConfig aConfig, 
			ILaunch aLaunch) throws CoreException
	{
		return setup(new IProject[] {aJavaProject.getProject()}, aConfig, aLaunch);
	}
	
	public static boolean setup(
			IProject[] aProjects,
			TODConfig aConfig, 
			ILaunch aLaunch) throws CoreException
{
		MainView theView = TODPluginUtils.getMainView(true);
		
		ISession theSession = null;
		try
		{
			theSession = TODSessionManager.getInstance().getSession(
					theView.getGUIManager(),
					aConfig,
					new EclipseProgramLaunch(aLaunch, aProjects));
		}
		catch (Exception e)
		{
			TODPlugin.logError("Could not create session", e);
			handleException(aConfig, e);
		}
		
		itsInfo.set(new LaunchInfo(theSession, aConfig));
		
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
	
	/**
	 * Determines the version of the native agent library to use for the given
	 * launch configuration.
	 * The architecture of the target VM is first determined by running a small
	 * program in the target VM.
	 */
	protected static String getAgentLibName(ILaunchConfiguration aConfiguration) throws CoreException
	{
		// Determine architecture of target VM
		IVMInstall theVMInstall = JavaRuntime.computeVMInstall(aConfiguration);
		String theOs;
		String theArch;
		
		if (theVMInstall instanceof IVMInstall3)
		{
			IVMInstall3 theInstall3 = (IVMInstall3) theVMInstall;
			Map theMap = theInstall3.evaluateSystemProperties(new String[] {"os.name", "os.arch"}, null);
			theOs = (String) theMap.get("os.name");
			theArch = (String) theMap.get("os.arch");
		}
		else
		{
			theOs = System.getProperty("os.name");
			theArch = System.getProperty("os.arch");
		}
		
        String theLibName = null;
        String theAgentName = DeploymentConfig.getNativeAgentName();
		
		if (theOs.startsWith("Windows"))
		{
			theLibName = theAgentName+".dll";
		}
		else if (theOs.startsWith("Mac OS X"))
		{
			theLibName = "lib"+theAgentName+".dylib";
		}
		else if (theOs.startsWith("Linux"))
		{
			if (theArch.equals("x86") || theArch.equals("i386") || theArch.equals("i686"))
			{
				theLibName = "lib"+theAgentName+".so";
			}
			else if (theArch.equals("x86_64") || theArch.equals("amd64"))
			{
				theLibName = "lib"+theAgentName+"_x64.so";
			}
		}
		
		if (theLibName == null)
		{
			throw new RuntimeException("Unsupported architecture: "+theOs+"/"+theArch);
		}

		return theLibName;
	}
	
	protected static List<String> getAdditionalVMArguments(
			ILaunch aLaunch, 
			LaunchInfo aInfo,
			IProgressMonitor aMonitor) throws CoreException
	{
		if (aMonitor == null) aMonitor = new NullProgressMonitor();
		
		// Determine which version of the agent to use.
		if (aMonitor.isCanceled()) return null;
		aMonitor.subTask("Determining architecture of target JVM");
        String theLibName = getAgentLibName(aLaunch.getLaunchConfiguration());
		if (aMonitor.isCanceled()) return null;

		aMonitor.subTask("Setting up extra JVM arguments");
		
		String theLibraryPath = TODPlugin.getDefault().getLibraryPath();

		List<String> theArguments = new ArrayList<String>();
        
        // Boot class path
		String theAgentPath = System.getProperty("agent.path", theLibraryPath+"/tod-agent.jar");
        
        theArguments.add ("-Xbootclasspath/p:"+theAgentPath);
		

		String theNativeAgentPath = System.getProperty("bcilib.path", theLibraryPath)+"/"+theLibName;
		
		theArguments.add("-agentpath:"+theNativeAgentPath);
		theArguments.add("-Djava.library.path="+theLibraryPath);
		
		theArguments.add("-noverify");
		
		// Config
		TODConfig theConfig = aInfo.config;
		
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
		public final TODConfig config;
		
		public LaunchInfo(ISession aSession, TODConfig aConfig)
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
			List<String> theAdditionalArgs = getAdditionalVMArguments(aLaunch, itsInfo, aMonitor);
			if (aMonitor.isCanceled()) return;
			
			List<String> theArgs = new ArrayList<String>();
			Utils.fillCollection(theArgs, theAdditionalArgs);
			Utils.fillCollection(theArgs, aConfiguration.getVMArguments());
			
			String[] theFullArgs = theArgs.toArray(new String[theArgs.size()]);
			aConfiguration.setVMArguments(theFullArgs);
			
			itsDelegate.run(aConfiguration, aLaunch, aMonitor);
		}
	}
	
}
