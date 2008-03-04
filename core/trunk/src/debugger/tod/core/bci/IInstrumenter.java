/*
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
