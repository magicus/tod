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

import java.util.HashSet;
import java.util.Set;

import zz.utils.Utils;

/**
 * Instrumentation specification for a given class.
 * It is used to treat some cases 
 * @author gpothier
 */
public abstract class InstrumentationSpec
{
	public static InstrumentationSpec ALL = new All(null);
	
	private final String itsClassName;
	
	public InstrumentationSpec(String aClassName)
	{
		itsClassName = aClassName;
	}
	
	public abstract boolean shouldInstrument();
	public abstract boolean shouldInstrument(String aBehaviorName, String aSignature);
	
	
	
	public static class All extends InstrumentationSpec
	{
		public All(String aClassName)
		{
			super(aClassName);
		}

		@Override
		public boolean shouldInstrument()
		{
			return true;
		}

		@Override
		public boolean shouldInstrument(String arg0, String arg1)
		{
			return true;
		}
	}
	
	/**
	 * This spec filters behaviors by name and signature.
	 * @author gpothier
	 */
	public static class BehaviorFilter extends InstrumentationSpec
	{
		private Set<String> itsInstrumentedBehaviors = new HashSet<String>();
		
		public BehaviorFilter(String aClassName, String... aBehaviorNames)
		{
			super(aClassName);
			Utils.fillCollection(itsInstrumentedBehaviors, aBehaviorNames);
		}
		
		@Override
		public boolean shouldInstrument()
		{
			return true;
		}
		
		@Override
		public boolean shouldInstrument(String aBehaviorName, String aSignature)
		{
			// TODO: handle signatures.
			return itsInstrumentedBehaviors.contains(aBehaviorName);
		}
		
	}
}
