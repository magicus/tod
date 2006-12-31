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
package tod.utils;

import tod.core.BehaviorKind;
import tod.core.ILocationRegistrer;
import tod.core.database.browser.ILocationStore;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.TypeInfo;

public class DummyLocationRegistrer 
implements ILocationStore
{
	public void registerBehavior(BehaviorKind aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName, String aSignature)
	{
	}

	public void registerBehaviorAttributes(int aBehaviourId, LineNumberInfo[] aLineNumberTable, LocalVariableInfo[] aLocalVariableTable)
	{
	}

	public void registerField(int aFieldId, int aTypeId, String aFieldName)
	{
	}

	public void registerFile(int aFileId, String aFileName)
	{
	}

	public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
	{
	}

	public ITypeInfo[] getArgumentTypes(String aSignature)
	{
		return null;
	}
	
	public TypeInfo getReturnType(String aSignature)
	{
		return null;
	}

	public IBehaviorInfo getBehavior(int aBehaviorId)
	{
		return null;
	}

	public IBehaviorInfo getBehavior(ITypeInfo aType, String aName, String aSignature, boolean aSearchAncestors)
	{
		return null;
	}

	public Iterable<IBehaviorInfo> getBehaviours()
	{
		return null;
	}

	public Iterable<ITypeInfo> getTypes()
	{
		return null;
	}

	public IFieldInfo getField(int aFieldId)
	{
		return null;
	}

	public IFieldInfo getField(ITypeInfo aType, String aName, boolean aSearchAncestors)
	{
		return null;
	}

	public Iterable<IFieldInfo> getFields()
	{
		return null;
	}

	public Iterable<String> getFiles()
	{
		return null;
	}

	public Stats getStats()
	{
		return null;
	}

	public ITypeInfo getType(int aId)
	{
		return null;
	}

	public ITypeInfo getType(String aName)
	{
		return null;
	}
	
	public Iterable<ILocationInfo> getLocations()
	{
		return null;
	}

}
