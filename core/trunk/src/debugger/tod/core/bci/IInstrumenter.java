/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.core.bci;

import java.util.List;

import tod.agent.TracedMethods;
import tod.core.config.TODConfig;

public interface IInstrumenter
{
    /**
     * Instruments the given class.
     * @param aClassName JVM internal class name (eg. "java/lang/Object")
     * @param aBytecode Original bytecode of the class
     * @return New bytecode, or null if no instrumentation is performed.
     */
	public InstrumentedClass instrumentClass (String aClassName, byte[] aBytecode);
	
	/**
	 * Changes the current trace working set.
	 * @see TODConfig#SCOPE_TRACE_FILTER
	 */
	public void setTraceWorkingSet(String aWorkingSet);

	/**
	 * Sets the current global working set.
	 * @see TODConfig#SCOPE_GLOBAL_FILTER
	 */
	public void setGlobalWorkingSet(String aWorkingSet);
	
	/**
	 * Aggregates the results of class instrumentation
	 * @author gpothier
	 */
	public static class InstrumentedClass
	{
		/**
		 * Instrumented bytecode
		 */
		public final byte[] bytecode;
		
		/**
		 * List of ids of the methods that were instrumented.
		 * @see TracedMethods
		 */
		public final List<Integer> tracedMethods;

		public InstrumentedClass(byte[] aBytecode, List<Integer> aTracedMethods)
		{
			bytecode = aBytecode;
			tracedMethods = aTracedMethods;
		}
	}
}
