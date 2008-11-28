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
package tod.impl.database.structure.standard;

import tod.core.database.structure.IArraySlotFieldInfo;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;

public class ArraySlotFieldInfo 
implements IArraySlotFieldInfo
{
	private transient IStructureDatabase itsDatabase;
	private final int itsIndex;
	private final IArrayTypeInfo itsType;
	
	public ArraySlotFieldInfo(IStructureDatabase aDatabase, IArrayTypeInfo aType, int aIndex)
	{
		itsDatabase = aDatabase;
		itsIndex = aIndex;
		itsType = aType;
	}
	
	public IStructureDatabase getDatabase()
	{
		return itsDatabase;
	}

	public IArrayTypeInfo getDeclaringType()
	{
		return itsType;
	}
	
	public ITypeInfo getType()
	{
		return getDeclaringType().getElementType();
	}
	
	public int getId()
	{
		return -1;
	}
	
	public int getIndex()
	{
		return itsIndex;
	}
	
	public String getName()
	{
		return "["+getIndex()+"]";
	}

	public boolean isStatic()
	{
		return false;
	}

	public String getSourceFile()
	{
		throw new UnsupportedOperationException();
	}
}
