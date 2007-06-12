/*
TOD - Trace Oriented Debugger.
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
package tod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import reflex.run.agent.ReflexAgent;

/**
 * Companion class of {@link ReflexBridge} that the application should use
 * when it is ready to setup POM/Reflex.
 * @author gpothier
 */
public class ReflexRiver
{
	/**
	 * Config classes to use.
	 */
	private static List<String> itsConfigClasses = new ArrayList<String>();
	
	/**
	 * Working set entries to use. First level is additive.
	 */
	private static List<String> itsWSEntries = new ArrayList<String>();
	
	static
	{
		itsConfigClasses.add("reflex.lib.pom.POMConfig");
		itsWSEntries.add("+tod.impl.dbgrid.GridLogBrowser");
		itsWSEntries.add("+tod.impl.dbgrid.GridEventBrowser");
	}
	
	/**
	 * Adds the specified classes to the reflex configuration.
	 * This method must be called before {@link #setup()}.
	 */
	public static void addConfigClasses(Class... aClasses)
	{
		for (Class theClass : aClasses) itsConfigClasses.add(theClass.getName());
	}
	
	/**
	 * Adds working set entries to the reflex configuration.
	 * First level is additive, ie. the first entry starts with a "+".
	 * This method must be called before {@link #setup()}.
	 */
	public static void addWSEntries(String... aEntries)
	{
		for (String theEntry : aEntries) itsWSEntries.add(theEntry);
	}
	
	/**
	 * Setup ReflexBridge.
	 * @param aConfigClasses Additional Reflex configuration classes
	 * @param aWSEntries Additional working set entries.
	 */
	public static void setup()
	{
		try
		{
			// We disable the plugin finding mechanism (it searches all of Eclipse plugins...)
			System.setProperty("reflex.config.plugins.path", "");
			
			Class theRBClass = Class.forName("tod.ReflexBridge", true, ClassLoader.getSystemClassLoader());
			Method theGIMethod = theRBClass.getMethod("getInstance");
			Object theInstance = theGIMethod.invoke(null);
			Method theSTMethod = theRBClass.getMethod("setTransformer", ClassFileTransformer.class);
			
			StringBuilder theConfigBuilder = new StringBuilder();
			for (String theClass : itsConfigClasses)
			{
				theConfigBuilder.append(',');
				theConfigBuilder.append(theClass);
			}
			
			StringBuilder theWSBuilder = new StringBuilder();
			
			for (String theEntry : itsWSEntries)
			{
				theWSBuilder.append(',');
				theWSBuilder.append(theEntry);
			}
			
			String theArgs = 
				"-lp " + theConfigBuilder.toString().substring(1) +
				" --working-set [" + theWSBuilder.toString().substring(1) + "]";
			
			theSTMethod.invoke(theInstance, ReflexAgent.createTransformer(theArgs));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
