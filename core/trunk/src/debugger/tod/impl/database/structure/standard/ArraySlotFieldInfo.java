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

import tod.core.database.structure.IArraySlotFieldInfo;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IStructureDatabase;

public class ArraySlotFieldInfo extends FieldInfo
implements IArraySlotFieldInfo
{
	public ArraySlotFieldInfo(IStructureDatabase aDatabase, IArrayTypeInfo aType, int aIndex)
	{
		super(aDatabase, -aIndex-1, aType, null);
	}
	
	@Override
	public IArrayTypeInfo getType()
	{
		return (IArrayTypeInfo) super.getType();
	}
	
	@Override
	public int getId()
	{
		return -1;
	}
	
	public int getIndex()
	{
		return -super.getId()-1;
	}
	
	@Override
	public String getName()
	{
		return "["+getIndex()+"]";
	}

}
