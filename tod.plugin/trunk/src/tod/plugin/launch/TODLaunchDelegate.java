/*
 * Created on Aug 16, 2005
 */
package tod.plugin.launch;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

import reflex.ide.eclipse.launcher.AbstractCustomLaunchConfigurationDelegate;
import tod.plugin.TODPlugin;
import tod.plugin.TODSessionManager;
import tod.session.ISession;
import zz.utils.Utils;

public class TODLaunchDelegate extends AbstractCustomLaunchConfigurationDelegate
{
	/**
	 * Force running mode (debug/run).
	 */
	public void launch(ILaunchConfiguration aConfiguration, String aMode, ILaunch aLaunch, IProgressMonitor aMonitor)
	throws CoreException
	{
		ISession theSession = TODSessionManager.getInstance().createSession();
		try
		{
			LibraryLocation[] theLibraryLocations = JavaRuntime.getLibraryLocations(getVMInstall(aConfiguration));
			String[] theBootpath = new String[theLibraryLocations.length];
			for (int i = 0; i < theLibraryLocations.length; i++)
			{
				LibraryLocation theLocation = theLibraryLocations[i];
				theBootpath[i] = theLocation.getSystemLibraryPath().toFile().getPath();
			}
			
			String[] theBootClasspath = getBootpath(aConfiguration);
			String[] theClasspath = getClasspath(aConfiguration);
			ClassCacheCleaner.deleteUpdatedClasses(theSession, theBootpath, theClasspath);
			super.launch(aConfiguration, ILaunchManager.DEBUG_MODE, aLaunch, aMonitor);
		}
		catch (CoreException e)
		{
			System.out.println("Exception caught in launch, disconnecting session");
			theSession.disconnect();
			throw e;
		}
		catch (RuntimeException e)
		{
			System.out.println("Exception caught in launch, disconnecting session");
			theSession.disconnect();
			throw e;
		}
	}

	
	@Override
	protected List<String> getAdditionalVMArguments(ILaunchConfiguration aConfiguration) throws CoreException
	{
		List<String> theArguments = super.getAdditionalVMArguments(aConfiguration);
		
		String theAgentPath = System.getProperty(
				"bcilib.path",
				TODPlugin.getDefault().getLibraryPath()+"/libbci-agent.so");
		
		theArguments.add("-agentpath:"+theAgentPath);
		
		theArguments.add("-noverify");
		
		return theArguments;
	}
	
	@Override
	protected List<String> getPrependedBootClassPathEntries(ILaunchConfiguration aConfiguration) throws CoreException
	{
		List<String> theEntries = super.getPrependedBootClassPathEntries(aConfiguration);
		
		String theBciPath = System.getProperty(
				"bci.path",
				TODPlugin.getDefault().getLibraryPath()+"/remotebci.jar");
		
		String theAgentPath = System.getProperty(
				"agent.path",
				TODPlugin.getDefault().getLibraryPath()+"/tod-agent.jar");
		
		theEntries.add(theBciPath);
		theEntries.add(theAgentPath);
		
		return theEntries;
	}
	
}
//public class TODLaunchDelegate extends AbstractReflexLaunchConfigurationDelegate
//{
//	
//	/**
//	 * Force "run" mode.
//	 */
//	public void launch(ILaunchConfiguration aConfiguration, String aMode, ILaunch aLaunch, IProgressMonitor aMonitor)
//	throws CoreException
//	{
//		super.launch(aConfiguration, ILaunchManager.DEBUG_MODE, aLaunch, aMonitor);
//	}
//	
//	@Override
//	protected Collection<String> getConfigClassNames(ILaunchConfiguration aConfiguration) throws CoreException
//	{
//		return Collections.singletonList(Config.class.getName());
//	}
//	
//	/**
//	 * Append connection information.
//	 */
//	@Override
//	protected Map<String, String> getAdditionalSystemProperties(ILaunchConfiguration aConfiguration) throws CoreException
//	{
//		Map<String, String> theMap = super.getAdditionalSystemProperties(aConfiguration);
//		
//		theMap.put(StaticConfig.PARAM_COLLECTOR_PORT, ""+TODPlugin.getDefault().getSession().getPort());
//		theMap.put(StaticConfig.PARAM_LOGGING_WORKINGSET, "[-java.** -javax.**]");
////		theMap.put(StaticConfig.PARAM_IDENTIFICATION_WORKINGSET, "[-java.** -javax.**]");
//		
//		return theMap;
//	}
//	
//	@Override
//	protected List<String> getAdditionalClassPathEntries(ILaunchConfiguration aConfiguration) throws CoreException
//	{
//		List<String> theEntries = super.getAdditionalClassPathEntries(aConfiguration);
//		theEntries.add(System.getProperty("logminer.path", TODPlugin.getDefault().getLibraryPath()+"/logminer.jar"));
//		
//		return theEntries;
//	}
//	
//	@Override
//	protected boolean includeReflexInClasspath(ILaunchConfiguration aConfiguration) throws CoreException
//	{
//		return true;
//	}
//	
//	@Override
//	protected boolean useCustomLoader(ILaunchConfiguration aConfiguration) throws CoreException
//	{
//		return true;
//	}
//}
