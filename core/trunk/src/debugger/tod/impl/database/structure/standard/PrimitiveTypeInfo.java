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
package tod.impl.database.structure.standard;

import tod.core.database.structure.IPrimitiveTypeInfo;

public class PrimitiveTypeInfo extends TypeInfo implements IPrimitiveTypeInfo
{
	public static final PrimitiveTypeInfo VOID = new PrimitiveTypeInfo("void", 0, 1);
	public static final PrimitiveTypeInfo BOOLEAN = new PrimitiveTypeInfo("boolean", 1, 2);
	public static final PrimitiveTypeInfo INT = new PrimitiveTypeInfo("int", 1, 3);
	public static final PrimitiveTypeInfo LONG = new PrimitiveTypeInfo("long", 2, 4);
	public static final PrimitiveTypeInfo BYTE = new PrimitiveTypeInfo("byte", 1, 5);
	public static final PrimitiveTypeInfo SHORT = new PrimitiveTypeInfo("short", 1, 6);
	public static final PrimitiveTypeInfo CHAR = new PrimitiveTypeInfo("char", 1, 7);
	public static final PrimitiveTypeInfo DOUBLE = new PrimitiveTypeInfo("double", 2, 8);
	public static final PrimitiveTypeInfo FLOAT = new PrimitiveTypeInfo("float", 1, 9);

	public static final PrimitiveTypeInfo[] TYPES = {
		VOID, BOOLEAN, INT, LONG, BYTE, SHORT, CHAR, DOUBLE, FLOAT
	};
	
	private final int itsSize;

	public PrimitiveTypeInfo(String aName, int aSize, int aId)
	{
		super(null, aId, aName);
		itsSize = aSize;
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
