/*
 * Created on Dec 13, 2008
 */
package tod.plugin.wst.launch;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstall3;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jst.server.tomcat.core.internal.TomcatLaunchConfigurationDelegate;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;

import tod.plugin.launch.LaunchUtils;
import tod.plugin.launch.TODConfigLaunchTab;

public class TODLaunchDelegate_Tomcat extends TomcatLaunchConfigurationDelegate
{
	@Override
	public IVMRunner getVMRunner(ILaunchConfiguration aConfiguration, String aMode) throws CoreException
	{
		return LaunchUtils.getVMRunner(super.getVMRunner(aConfiguration, aMode));
	}
	
	@Override
	public IVMInstall verifyVMInstall(ILaunchConfiguration aConfiguration) throws CoreException
	{
		return new MyVMInstall(super.verifyVMInstall(aConfiguration));
	}
	
	@Override
	public void launch(ILaunchConfiguration aConfiguration, String aMode, ILaunch aLaunch, IProgressMonitor aMonitor)
			throws CoreException
	{
		try
		{
			IServer theServer = ServerUtil.getServer(aConfiguration);
			WSTServerLaunch theLaunch = new WSTServerLaunch(aLaunch, theServer);
			if (LaunchUtils.setup(TODConfigLaunchTab.readConfig(aConfiguration), theLaunch))
			{
				super.launch(aConfiguration, LaunchUtils.getLaunchMode(aConfiguration), aLaunch, aMonitor);
			}
		}
		finally
		{
			LaunchUtils.tearDown();
		}
	}
	
	private class MyVMInstall implements IVMInstall, IVMInstall2, IVMInstall3
	{
		private IVMInstall itsDelegate;
		private IVMInstall2 itsDelegate2;
		private IVMInstall3 itsDelegate3;
		
		public MyVMInstall(IVMInstall aDelegate)
		{
			itsDelegate = aDelegate;
			itsDelegate2 = (IVMInstall2) aDelegate;
			itsDelegate3 = (IVMInstall3) aDelegate;
		}		

		public String getJavaVersion()
		{
			return itsDelegate2.getJavaVersion();
		}

		public String getVMArgs()
		{
			return itsDelegate2.getVMArgs();
		}

		public void setVMArgs(String aVmArgs)
		{
			itsDelegate2.setVMArgs(aVmArgs);
		}

		public String getId()
		{
			return itsDelegate.getId();
		}

		public File getInstallLocation()
		{
			return itsDelegate.getInstallLocation();
		}

		public URL getJavadocLocation()
		{
			return itsDelegate.getJavadocLocation();
		}

		public LibraryLocation[] getLibraryLocations()
		{
			return itsDelegate.getLibraryLocations();
		}

		public String getName()
		{
			return itsDelegate.getName();
		}

		public String[] getVMArguments()
		{
			return itsDelegate.getVMArguments();
		}

		public IVMInstallType getVMInstallType()
		{
			return itsDelegate.getVMInstallType();
		}

		public IVMRunner getVMRunner(String aMode)
		{
			return LaunchUtils.getVMRunner(itsDelegate.getVMRunner(aMode));
		}

		public void setInstallLocation(File aInstallLocation)
		{
			itsDelegate.setInstallLocation(aInstallLocation);
		}

		public void setJavadocLocation(URL aUrl)
		{
			itsDelegate.setJavadocLocation(aUrl);
		}

		public void setLibraryLocations(LibraryLocation[] aLocations)
		{
			itsDelegate.setLibraryLocations(aLocations);
		}

		public void setName(String aName)
		{
			itsDelegate.setName(aName);
		}

		public void setVMArguments(String[] aVmArgs)
		{
			itsDelegate.setVMArguments(aVmArgs);
		}

		public Map evaluateSystemProperties(String[] aProperties, IProgressMonitor aMonitor) throws CoreException
		{
			return itsDelegate3.evaluateSystemProperties(aProperties, aMonitor);
		}
		
	}

}
