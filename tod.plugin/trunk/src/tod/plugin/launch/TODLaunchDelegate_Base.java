/*
 * Created on Jun 8, 2007
 */
package tod.plugin.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import tod.core.config.TODConfig;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.session.ConnectionInfo;
import tod.core.session.ISession;
import tod.plugin.DebuggingSession;
import tod.plugin.SourceRevealerUtils;
import tod.plugin.TODPlugin;
import tod.plugin.TODSessionManager;
import zz.utils.Utils;

public class TODLaunchDelegate_Base 
{
	public static final String MODE = ILaunchManager.DEBUG_MODE;

	private static final ThreadLocal<LaunchInfo> itsInfo = new ThreadLocal<LaunchInfo>();

	public static boolean setup(
			SourceRevealer aGotoSourceDelegate,
			ILaunchConfiguration aConfiguration, 
			ILaunch aLaunch) throws CoreException
	{
		TODConfig theConfig = TODConfigLaunchTab.readConfig(aConfiguration);
		ISession theSession = TODSessionManager.getInstance().getSession(
				aLaunch,
				aGotoSourceDelegate,
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
	
	/**
	 * An interface for source code lookups.
	 * @author gpothier
	 */
	public static abstract class SourceRevealer
	{
		private ILaunch itsLaunch;
		
		public SourceRevealer(ILaunch aLaunch)
		{
			itsLaunch = aLaunch;
		}

		protected ILaunch getLaunch()
		{
			return itsLaunch;
		}

		public final void gotoSource (ILogEvent aEvent)
		{
			if (aEvent instanceof ICallerSideEvent)
			{
				ICallerSideEvent theEvent = (ICallerSideEvent) aEvent;
				gotoSource(theEvent);
			}
			else if (aEvent instanceof IBehaviorCallEvent)
			{
				IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
				gotoSource(theEvent.getExecutedBehavior());
			}
		}
		
		protected final void gotoSource (ICallerSideEvent aEvent)
		{
			IBehaviorCallEvent theParent = aEvent.getParent();
		    if (theParent == null) return;
		    
		    int theBytecodeIndex = aEvent.getOperationBytecodeIndex();
		    IBehaviorInfo theBehavior = theParent.getExecutedBehavior();
		    if (theBehavior == null) return;
		    
		    int theLineNumber = theBehavior.getLineNumber(theBytecodeIndex);
		    ITypeInfo theType = theBehavior.getType();
		    
		    String theTypeName = theType.getName();
		    gotoSource(theTypeName, theLineNumber);
		}
		
		protected abstract void gotoSource(String aTypeName, int aLineNumber);
		protected abstract void gotoSource (IBehaviorInfo aBehavior);

	}
	
	public static class JDTSourceRevealer extends SourceRevealer
	{
		private IJavaProject itsJavaProject;
		
		public JDTSourceRevealer(ILaunch aLaunch, IJavaProject aJavaProject)
		{
			super(aLaunch);
			itsJavaProject = aJavaProject;
		}

		protected IJavaProject getJavaProject()
		{
			return itsJavaProject;
		}

		@Override
		public void gotoSource (String aTypeName, int aLineNumber)
		{
		    SourceRevealerUtils.reveal(getLaunch(), aTypeName, aLineNumber);
		}
		
		@Override
		public void gotoSource (IBehaviorInfo aBehavior)
		{
			SourceRevealerUtils.reveal(
					getJavaProject(), 
					aBehavior.getType().getName(), 
					aBehavior.getName());
		}
	}
	
	public static class PDESourceRevealer extends SourceRevealer
	{
		private IProject[] itsProjects;
		private IJavaProject itsJavaProject;

		public PDESourceRevealer(ILaunch aLaunch, IProject[] aProjects)
		{
			super(aLaunch);
			itsProjects = aProjects;
			
			for (IProject theProject : itsProjects)
			{
				IJavaProject theJProject = JavaCore.create(theProject);
				if (theJProject != null && theJProject.exists()) 
					itsJavaProject = theJProject;
			}
		}

		@Override
		protected void gotoSource(String aTypeName, int aLineNumber)
		{
			if (itsJavaProject != null)
			{
				SourceRevealerUtils.reveal(itsJavaProject, aTypeName, aLineNumber);
			}
		}
		
		@Override
		protected void gotoSource(IBehaviorInfo aBehavior)
		{
		}

	}
}
