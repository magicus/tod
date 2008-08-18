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
package tod.plugin;

import java.io.File;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import tod.Util;
import tod.impl.dbgrid.DBProcessManager;
import zz.eclipse.utils.EclipseUtils;

/**
 * The main plugin class to be used in the desktop.
 */
public class TODPlugin extends AbstractUIPlugin
{
	public static final boolean USE_REFLEX_BRIDGE = false;
	
	// The shared instance.
	private static TODPlugin plugin;
	
	private String itsLibraryPath;

	/**
	 * The constructor.
	 */
	public TODPlugin()
	{
		plugin = this;
		System.setProperty("swing.aatext", "true");
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		String theBase = getLibraryPath();

		String theDevPath = Util.workspacePath;
		if (theDevPath == null)
		{
			DBProcessManager.cp += File.pathSeparator
				+theBase+"/tod-debugger.jar"+File.pathSeparator
				+theBase+"/tod-dbgrid.jar"+File.pathSeparator
				+theBase+"/tod-evdb1.jar"+File.pathSeparator
				+theBase+"/tod-agent.jar"+File.pathSeparator
				+theBase+"/asm-all-3.1.jar"+File.pathSeparator
				+theBase+"/aspectjrt.jar"+File.pathSeparator
				+theBase+"/zz.utils.jar";
		}
		else
		{
			DBProcessManager.cp += File.pathSeparator
				+theDevPath+"/TOD/bin"+File.pathSeparator
				+theDevPath+"/TOD-agent/bin"+File.pathSeparator
				+theDevPath+"/TOD-dbgrid/bin"+File.pathSeparator
				+theDevPath+"/TOD-evdb1/bin"+File.pathSeparator
				+theBase+"/asm-all-3.1.jar"+File.pathSeparator
				+theBase+"/aspectjrt.jar"+File.pathSeparator
				+theDevPath+"/zz.utils/bin";
			
			if (System.getProperty("agent.path") == null) System.setProperty("agent.path", theDevPath+"/TOD-agent/bin");
			if (System.getProperty("bcilib.path") == null) System.setProperty("bcilib.path", theDevPath+"/TOD-agent");

		}
		
		DBProcessManager.lib = theBase;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception
	{
		super.stop(context);
		plugin = null;
	}
	
	private void msgBridgeProblem()
	{
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				String theArgs = System.getProperty("eclipse.vmargs");
				String thePath = getLibraryPath() + "/reflex-bridge.jar"; 
				
				String theMessage = "Eclipse must be launched with special JVM arguments " +
						"for TOD to work. Check that your eclipse.ini file (in the Eclipse " +
						"installation directory) contains the following text:\n" +
						"-javaagent:" + thePath + "\n" +
						"If you launch Eclipse with a custom launch script and the script " +
						"passes -vmargs to the Eclipse launcher, add the above argument " +
						"right after -vmargs.\n" +
						"The current vmargs are: "+theArgs;

				Shell theShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(theShell, "TOD plugin - Reflex Bridge not found", theMessage);
			}
		});
	}
		


	/**
	 * Returns the shared instance.
	 */
	public static TODPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin("tod.plugin", path);
	}
	
	public String getLibraryPath()
	{
		if (itsLibraryPath == null)
			itsLibraryPath = EclipseUtils.getLibraryPath(this);
		
		return itsLibraryPath;
	}
	
	public static void logError(String aMessage, Throwable aThrowable)
	{
		log(Status.ERROR, aMessage, aThrowable);
	}
	
	public static void logWarning(String aMessage, Throwable aThrowable)
	{
		log(Status.WARNING, aMessage, aThrowable);
	}
	
	public static void log(int aSeverity, String aMessage, Throwable aThrowable)
	{
		getDefault().getLog().log(new Status(aSeverity, "tod.plugin", Status.OK, aMessage, aThrowable));
	}
	

}
