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
package tod.impl.dbgrid;

import java.io.File;
import java.lang.reflect.Constructor;

import javax.swing.SwingUtilities;

import reflex.Run;
import reflex.core.LogLevel;
import reflex.lib.pom.POMConfig;
import reflex.lib.pom.POMScheduler;
import reflex.run.common.RLoader;
import reflex.run.common.RunningEnvironment;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Infrastructure class that permits to have the UI classes loaded
 * by Reflex.
 * @author gpothier
 */
//public class ReflexBridge extends POMConfig
//{
//	private static final boolean ENABLE = false;
//	
//	private static ThreadLocal itsLocal = new ThreadLocal();
//	
//	public static void main(String[] args) throws Throwable
//	{
//		Args theArgs = (Args) itsLocal.get();
//		Object theInstance = theArgs.newInstance();
//		itsLocal.set(theInstance);
//	}
//	
//	/**
//	 * Creates a reflexive instance of the given class. Note that
//	 * the class should not have been loaded before this method
//	 * is called (so passing Xxxx.class.getName() to this method
//	 * is not an option).
//	 */
//	public static Object create(String aClassName, Object... aArgs)
//	{
//		return create(aClassName, null, aArgs);
//	}
//	
//	/**
//	 * Creates a reflexive instance of the given class. Note that
//	 * the class should not have been loaded before this method
//	 * is called (so passing Xxxx.class.getName() to this method
//	 * is not an option).
//	 */
//	public static Object create(String aClassName, Class[] aTypes, Object... aArgs)
//	{
//		try
//		{
//			Args theArgs = new Args(aClassName, aTypes, aArgs);
//			
//			if (ENABLE)
//			{
//				// LogLevel.set(LogLevel.VERBOSE);
//				itsLocal.set(theArgs);
//							
//				Run.main(new String[] { 
//						"-lp", ReflexBridge.class.getName(), 
//						"--working-set", "[+tod.impl.dbgrid.GridLogBrowser +tod.impl.dbgrid.GridLogBrowser$QueryResultCache +tod.impl.dbgrid.aggregator.GridEventBrowser -tod.impl.dbgrid.ReflexBridge]",
//						ReflexBridge.class.getName()});
//				
//				return itsLocal.get();
//			}
//			else 
//			{
//				return theArgs.newInstance();
//			}
//		}
//		catch (Throwable e)
//		{
//			throw new RuntimeException(e);
//		}
//	}
//	
//	private static class Args
//	{
//		private String itsClassName;
//		private Class[] itsTypes;
//		private Object[] itsValues;
//		
//		public Args(String aClassName, Class[] aTypes, Object[] aValues)
//		{
//			itsClassName = aClassName;
//			itsTypes = aTypes;
//			if (itsTypes == null)
//			{
//				itsTypes = new Class[itsValues.length];
//				for (int i = 0; i < itsValues.length; i++) itsTypes[i] = itsValues[i].getClass();
//			}
//			itsValues = aValues;
//		}
//		
//		public Object newInstance() throws Exception
//		{
//			Class theClass = ENABLE ?
//					Class.forName(itsClassName, true, RunningEnvironment.get().getLoader())
//					: Class.forName(itsClassName);
//			Constructor theConstructor = theClass.getConstructor(itsTypes);
//			return theConstructor.newInstance(itsValues);
//		}
//	}
//	
//}
