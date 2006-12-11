/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.core.database.structure;

import tod.core.database.browser.ILocationsRepository;

public class ArrayTypeInfo extends TypeInfo implements IArrayTypeInfo
{
	private ITypeInfo itsElementType;
	private int itsDimensions;
	
	public ArrayTypeInfo(ITypeInfo aElementType, int aDimensions)
	{
		super(-1);
		itsElementType = aElementType;
		itsDimensions = aDimensions;
	}

	public int getDimensions()
	{
		return itsDimensions;
	}

	public ITypeInfo getElementType()
	{
		return itsElementType;
	}

	public int getSize()
	{
		return 1;
	}

	public boolean isArray()
	{
		return true;
	}

	public boolean isPrimitive()
	{
		return false;
	}

	public boolean isVoid()
	{
		return false;
	}
	
	
}
