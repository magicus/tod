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
package tod.impl.database.structure.standard;

import org.objectweb.asm.Type;

import tod.core.database.structure.IPrimitiveTypeInfo;

public class PrimitiveTypeInfo extends TypeInfo implements IPrimitiveTypeInfo
{
	private static final long serialVersionUID = 2145422655286109651L;
	
	public static final PrimitiveTypeInfo VOID = new PrimitiveTypeInfo("void", Type.VOID_TYPE, 0, 1);
	public static final PrimitiveTypeInfo BOOLEAN = new PrimitiveTypeInfo("boolean", Type.BOOLEAN_TYPE, 1, 2);
	public static final PrimitiveTypeInfo INT = new PrimitiveTypeInfo("int", Type.INT_TYPE, 1, 3);
	public static final PrimitiveTypeInfo LONG = new PrimitiveTypeInfo("long", Type.LONG_TYPE, 2, 4);
	public static final PrimitiveTypeInfo BYTE = new PrimitiveTypeInfo("byte", Type.BYTE_TYPE, 1, 5);
	public static final PrimitiveTypeInfo SHORT = new PrimitiveTypeInfo("short", Type.SHORT_TYPE, 1, 6);
	public static final PrimitiveTypeInfo CHAR = new PrimitiveTypeInfo("char", Type.CHAR_TYPE, 1, 7);
	public static final PrimitiveTypeInfo DOUBLE = new PrimitiveTypeInfo("double", Type.DOUBLE_TYPE, 2, 8);
	public static final PrimitiveTypeInfo FLOAT = new PrimitiveTypeInfo("float", Type.FLOAT_TYPE, 1, 9);

	public static final PrimitiveTypeInfo[] TYPES = {
		VOID, BOOLEAN, INT, LONG, BYTE, SHORT, CHAR, DOUBLE, FLOAT
	};
	
	private final int itsSize;
	private final String itsJvmName;

	public PrimitiveTypeInfo(String aName, Type aAsmType, int aSize, int aId)
	{
		super(null, aId, aName);
		itsJvmName = aAsmType.getDescriptor();
		itsSize = aSize;
	}

	public String getJvmName()
	{
		return itsJvmName;
	}
	
	public int getSize()
	{
		return itsSize;
	}

	public boolean isArray()
	{
		return false;
	}

	public boolean isPrimitive()
	{
		return true;
	}

	public boolean isVoid()
	{
		return "void".equals(getName());
	}
	
	/**
	 * Returns the type info corresponding to the specified id.
	 * Note that in a structure database, ids 1 to 9 are for primitive types.
	 */
	public static PrimitiveTypeInfo get(int aId)
	{
		return TYPES[aId-1];
	}
}
