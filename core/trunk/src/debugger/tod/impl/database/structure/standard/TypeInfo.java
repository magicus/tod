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

import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;


/**
 * Description of a type. This is an abstract class;
 * there are concrete subclasses for class, interface,
 * primitive type and array type.
 * @author gpothier
 */
public abstract class TypeInfo extends LocationInfo implements ITypeInfo
{
	public TypeInfo(IStructureDatabase aDatabase, int aId, String aName)
	{
		super(aDatabase, aId, aName);
	}

	public TypeInfo(IStructureDatabase aDatabase, int aId)
	{
		super(aDatabase, aId);
	}
	
	public ClassInfo createUncertainClone()
	{
		ClassInfo theClone = (ClassInfo) super.clone();
		theClone.changeName(getName()+ "?");
		return theClone;
	}

	@Override
	public String toString()
	{
		return "Type ("+getId()+", "+getName()+")";
	}
}
