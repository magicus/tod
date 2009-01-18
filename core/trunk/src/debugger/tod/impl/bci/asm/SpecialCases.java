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
package tod.impl.bci.asm;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry for special instrumentation cases.
 * For instance, in the case of an ArrayList we are only interested in
 * mutating methods.
 * @author gpothier
 */
public class SpecialCases
{
	private static Map<String, InstrumentationSpec> itsSpecs = new HashMap<String, InstrumentationSpec>();
	
	private static void add(String aClassName, InstrumentationSpec aSpec)
	{
		itsSpecs.put(aClassName, aSpec);
	}
	
	private static boolean isArrayCopy(String aOwner, String aName, String aDesc)
	{
		return "java/lang/System".equals(aOwner) && "arraycopy".equals(aName);
	}
	
	private static final InstrumentationSpec WRITES_AND_COPY = new WritesAndCopy();

	
	static
	{
		add("java/util/ArrayList", WRITES_AND_COPY);
		add("java/util/HashMap", WRITES_AND_COPY);
		add("java/util/HashMap$Entry", WRITES_AND_COPY);
	}

	/**
	 * Returns the instrumentation spec for the given class.
	 */
	public static InstrumentationSpec getSpec(String aClassName)
	{
		return itsSpecs.get(aClassName);
	}
	
	/**
	 * Returns an iterable over all the classes that have a special case registered.
	 */
	public static Iterable<String> getAllClasses()
	{
		return itsSpecs.keySet();
	}

	/**
	 * A spec that captures field and array writes, array creations, and calls to {@link System#arraycopy(Object, int, Object, int, int)}
	 * @author gpothier
	 */
	private static class WritesAndCopy extends InstrumentationSpec.None
	{
		@Override
		public boolean traceFieldWrite(String aOwner, String aName, String aDesc)
		{
			return true;
		}
		
		@Override
		public boolean traceArrayWrite()
		{
			return true;
		}
		
		@Override
		public boolean traceNewArray()
		{
			return true;
		}
		
		@Override
		public boolean traceCall(String aOwner, String aName, String aDesc)
		{
			return isArrayCopy(aOwner, aName, aDesc);
		}
	}
}
