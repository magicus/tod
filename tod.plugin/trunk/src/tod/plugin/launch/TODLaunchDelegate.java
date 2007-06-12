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

import tod.core.config.TODConfig;
import tod.core.session.ConnectionInfo;
import tod.core.session.ISession;
import tod.plugin.TODPlugin;
import tod.plugin.TODSessionManager;
import zz.eclipse.utils.launcher.AbstractCustomLaunchConfigurationDelegate;

public class TODLaunchDelegate extends AbstractCustomLaunchConfigurationDelegate
{
	private ISession itsSession;
	
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
		itsSession = TODSessionManager.getInstance().getSession(
				aLaunch,
				getJavaProject(aConfiguration),
				theConfig);
		
		if (itsSession == null) return;
		
		try
		{
			if (aMonitor.isCanceled()) return;		
			super.launch(aConfiguration, ILaunchManager.DEBUG_MODE, aLaunch, aMonitor, true);
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
