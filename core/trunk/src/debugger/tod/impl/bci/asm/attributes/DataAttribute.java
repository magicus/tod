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
package tod.impl.bci.asm.attributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

import tod.impl.bci.asm.attributes.SootAttribute.Entry;

/**
 * An attribute that uses a {@link DataInputStream} instead of a {@link ClassReader}.
 * @author gpothier
 */
public abstract class DataAttribute extends Attribute
{

	public DataAttribute(String aType)
	{
		super(aType);
	}

	/**
	 * The read method that should be implemented by subclasses 
	 */
	protected abstract Attribute read(DataInputStream aStream, Label[] aLabels) throws IOException;
	
	@Override
	protected final Attribute read(
			ClassReader cr, 
			int off, 
			int len, 
			char[] buf, 
			int codeOff, 
			Label[] aLabels)
	{
		try
		{
			DataInputStream theStream = new DataInputStream(new ByteArrayInputStream(cr.b, off, len));
			return read(theStream, aLabels);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * The write method that should be implemented by subclasses.
	 */
	protected abstract void write(DataOutputStream aStream, int len, int maxStack, int maxLocals) throws IOException;
	
	@Override
	protected final ByteVector write(
			ClassWriter cw, 
			byte[] code, 
			int len, 
			int maxStack, 
			int maxLocals)
	{
		try
		{
			ByteArrayOutputStream theOut = new ByteArrayOutputStream();
			DataOutputStream theStream = new DataOutputStream(theOut);
			
			write(theStream, len, maxStack, maxLocals);
			
			theStream.flush();
			
			byte[] theArray = theOut.toByteArray();
			ByteVector bv = new ByteVector(theArray.length);
			bv.putByteArray(theArray, 0, theArray.length);
			return bv;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
