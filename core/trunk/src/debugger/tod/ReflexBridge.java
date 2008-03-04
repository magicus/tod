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
package tod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * A Java agent class that sets up a dummy {@link ClassFileTransformer}
 * that will delegate to the actual Reflex transformer when the application
 * is ready (ie. when the Reflex classes are available).
 * @author gpothier
 */
public class ReflexBridge implements ClassFileTransformer
{
	private static ReflexBridge INSTANCE = new ReflexBridge();

	public static ReflexBridge getInstance()
	{
		return INSTANCE;
	}

	private ReflexBridge()
	{
	}
	
	private ClassFileTransformer itsTransformer;
	
    public byte[] transform(
    		ClassLoader aLoader, 
    		String aClassName, 
    		Class< ? > aClassBeingRedefined, 
    		ProtectionDomain aProtectionDomain, 
    		byte[] aClassfileBuffer) throws IllegalClassFormatException
	{
    	try
		{
			byte[] theResult = null;
			
			if (itsTransformer != null) 
			{
				theResult = itsTransformer.transform(aLoader, aClassName, aClassBeingRedefined, aProtectionDomain, aClassfileBuffer);
			}
			
			return theResult;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
    
    public void setTransformer(ClassFileTransformer aTransformer)
	{
		itsTransformer = aTransformer;
	}

	public static void premain(String agentArgs, Instrumentation inst)
    {
		System.out.println("ReflexBridge loaded.");
        inst.addTransformer(ReflexBridge.getInstance());
    }
}
