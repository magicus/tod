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
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;

import javassist.ClassPool;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import reflex.Run;
import reflex.core.model.RAnnotationImpl;
import reflex.run.agent.ReflexAgent;
import reflex.run.common.RunningEnvironment;
import tod.impl.dbgrid.LocalGridSession;
import zz.eclipse.utils.EclipseUtils;

/**
 * The main plugin class to be used in the desktop.
 */
public class TODPlugin extends AbstractUIPlugin
{
	// The shared instance.
	private static TODPlugin plugin;
	
	private String itsLibraryPath;

	/**
	 * The constructor.
	 */
	public TODPlugin()
	{
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		Class theRBClass = Class.forName("tod.plugin.ReflexBridge", true, ClassLoader.getSystemClassLoader());
		Method theGIMethod = theRBClass.getMethod("getInstance");
		Object theInstance = theGIMethod.invoke(null);
		Method theSTMethod = theRBClass.getMethod("setTransformer", ClassFileTransformer.class);
		String theArgs = "-lp reflex.lib.pom.POMConfig --working-set [+tod.impl.dbgrid.GridLogBrowser,+tod.impl.dbgrid.GridEventBrowser]";
		theSTMethod.invoke(theInstance, ReflexAgent.createTransformer(theArgs));
		
		String theBase = getLibraryPath();
		ClassPool thePool = RunningEnvironment.get().getClassPool();
		thePool.appendClassPath(theBase+"/reflex-core.jar");
		thePool.appendClassPath(theBase+"/pom.jar");
		thePool.appendClassPath(theBase+"/zz.utils.jar");
		thePool.appendClassPath(theBase+"/tod-debugger.jar");

		LocalGridSession.cp = 
			theBase+"/tod-debugger.jar"+File.pathSeparator
			+theBase+"/tod-agent.jar"+File.pathSeparator
			+theBase+"/asm-2.1.jar"+File.pathSeparator
			+theBase+"/asm-commons-2.1.jar"+File.pathSeparator
			+theBase+"/zz.utils.jar";
		
		LocalGridSession.lib = theBase;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception
	{
		super.stop(context);
		plugin = null;
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
