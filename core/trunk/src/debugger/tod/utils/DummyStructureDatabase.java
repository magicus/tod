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

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IStructureDatabase.IBehaviorListener;

public class DummyStructureDatabase 
implements IStructureDatabase
{
	private static DummyStructureDatabase INSTANCE = new DummyStructureDatabase();

	public static DummyStructureDatabase getInstance()
	{
		return INSTANCE;
	}

	private DummyStructureDatabase()
	{
	}
	
	public IBehaviorInfo getBehavior(int aId, boolean aFailIfAbsent)
	{
		return null;
	}

	public void addBehaviorListener(IBehaviorListener aListener)
	{
	}

	public IBehaviorInfo[] getBehaviors()
	{
		return null;
	}

	public IClassInfo getNewClass(String aName)
	{
		return null;
	}

	public void removeBehaviorListener(IBehaviorListener aListener)
	{
	}

	public IClassInfo getClass(int aId, boolean aFailIfAbsent)
	{
		return null;
	}

	public IClassInfo getClass(String aName, boolean aFailIfAbsent)
	{
		return null;
	}

	public IClassInfo getClass(String aName, String aChecksum, boolean aFailIfAbsent)
	{
		return null;
	}

	public IClassInfo[] getClasses(String aName)
	{
		return null;
	}

	public IFieldInfo getField(int aId, boolean aFailIfAbsent)
	{
		return null;
	}

	public String getId()
	{
		return null;
	}

	public ITypeInfo getType(String aName, boolean aFailIfAbsent)
	{
		return null;
	}

	public IClassInfo[] getClasses()
	{
		return null;
	}

	public Stats getStats()
	{
		return null;
	}

	public ITypeInfo getNewType(String aName)
	{
		return null;
	}

}
