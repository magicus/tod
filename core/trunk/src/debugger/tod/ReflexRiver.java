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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import reflex.run.agent.ReflexAgent;

/**
 * Companion class of {@link ReflexBridge} that the application should use
 * when it is ready to setup POM/Reflex.
 * @author gpothier
 */
public class ReflexRiver
{
	public static void setup()
	{
		try
		{
			Class theRBClass = Class.forName("tod.ReflexBridge", true, ClassLoader.getSystemClassLoader());
			Method theGIMethod = theRBClass.getMethod("getInstance");
			Object theInstance = theGIMethod.invoke(null);
			Method theSTMethod = theRBClass.getMethod("setTransformer", ClassFileTransformer.class);
			String theArgs = "-lp reflex.lib.pom.POMConfig --working-set [+tod.impl.dbgrid.GridLogBrowser,+tod.impl.dbgrid.GridEventBrowser]";
//		String theArgs = "-lp reflex.lib.pom.POMConfig --working-set [+tod.impl.dbgrid.Toto]";
			theSTMethod.invoke(theInstance, ReflexAgent.createTransformer(theArgs));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
