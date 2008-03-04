/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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
