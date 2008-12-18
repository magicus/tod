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
package tod.tools.jinterp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Represents a Java class in the interpreter.
 * @author gpothier
 */
public class JClass
{
	private final JInterpreter itsInterpreter;
	private final ClassNode itsNode;
	private final Map<String, MethodNode> itsMethods = new HashMap<String, MethodNode>();
	
	private JClass(JInterpreter aInterpreter, ClassNode aNode)
	{
		itsInterpreter = aInterpreter;
		itsNode = aNode;
		for (Iterator theIterator = itsNode.methods.iterator(); theIterator.hasNext();)
		{
			MethodNode theMethod = (MethodNode) theIterator.next();
			itsMethods.put(getMethodKey(theMethod.name, theMethod.signature), theMethod);
		}
	}

	public static JClass create(JInterpreter aInterpreter, byte[] aBytecode)
	{
		ClassReader theReader = new ClassReader(aBytecode);
		ClassNode theNode = new ClassNode();
		theReader.accept(theNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		return new JClass(aInterpreter, theNode);
	}
	
	public static String getMethodKey(String aName, String aSignature)
	{
		return aName+"|"+aSignature;
	}
}
