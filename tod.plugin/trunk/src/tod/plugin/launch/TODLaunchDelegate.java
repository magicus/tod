/*
TOD plugin - Eclipse pluging for TOD
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.plugin.launch;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

import tod.core.config.TODConfig;
import tod.core.session.ClassCacheCleaner;
import tod.core.session.ISession;
import tod.plugin.TODPlugin;
import tod.plugin.TODSessionManager;
import zz.eclipse.utils.launcher.AbstractCustomLaunchConfigurationDelegate;

public class TODLaunchDelegate extends AbstractCustomLaunchConfigurationDelegate
{
	/**
	 * Force running mode (debug/run).
	 */
	public void launch(ILaunchConfiguration aConfiguration, String aMode, ILaunch aLaunch, IProgressMonitor aMonitor)
	throws CoreException
	{
		if (aMonitor == null) aMonitor = new NullProgressMonitor();
		
		aMonitor.beginTask("Launching with TOD", IProgressMonitor.UNKNOWN);
		
		if (aMonitor.isCanceled()) return;		
		aMonitor.subTask("Creating session");
		
		TODConfig theConfig = TODConfigLaunchTab.readConfig(aConfiguration);
		ISession theSession = TODSessionManager.getInstance().createSession(
				aLaunch,
				getJavaProject(aConfiguration),
				theConfig);
		
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

			if (aMonitor.isCanceled()) return;		
			aMonitor.subTask("Checking cached classes");
			
			ClassCacheCleaner.deleteUpdatedClasses(theSession, theBootpath, theClasspath);
			super.launch(aConfiguration, ILaunchManager.RUN_MODE, aLaunch, aMonitor, true);
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
		
		TODConfig theConfig = TODConfigLaunchTab.readConfig(aConfiguration);
		
		theArguments.add("-Dcollector-host="+theConfig.get(TODConfig.COLLECTOR_HOST));
		theArguments.add("-Dcollector-port="+theConfig.get(TODConfig.COLLECTOR_JAVA_PORT));
		theArguments.add("-Dnative-port="+theConfig.get(TODConfig.COLLECTOR_NATIVE_PORT));
		theArguments.add("-Dtod-host=tod-1");
		
		return theArguments;
	}
	
	@Override
	protected List<String> getPrependedBootClassPathEntries(ILaunchConfiguration aConfiguration) throws CoreException
	{
		List<String> theEntries = super.getPrependedBootClassPathEntries(aConfiguration);
		
//		String theBciPath = System.getProperty(
//				"bci.path",
//				TODPlugin.getDefault().getLibraryPath()+"/remotebci.jar");
		
		String theAgentPath = System.getProperty(
				"agent.path",
				TODPlugin.getDefault().getLibraryPath()+"/tod-agent.jar");
		
//		theEntries.add(theBciPath);
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
