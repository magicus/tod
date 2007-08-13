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

import java.util.HashMap;
import java.util.Map;

import tod.core.database.browser.ILocationRegisterer;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;

/**
 * Default implementation of {@link IClassInfo}.
 * @author gpothier
 */
public class ClassInfo extends TypeInfo implements IClassInfo
{
	private ClassInfo itsSupertype;
	private ClassInfo[] itsInterfaces;
	
	private Map<String, IFieldInfo> itsFieldsMap = new HashMap<String, IFieldInfo>();
	private Map<String, IBehaviorInfo> itsBehaviorsMap = new HashMap<String, IBehaviorInfo>();
	
	/**
	 * Whether this class info can be disposed.
	 * At the start of the system,
	 * and when all debugged VMs are disconnected from the database,
	 * every class is marked disposable.
	 * Once operation starts, classes are marked not disposable
	 * as they are used or added to the database. This permits to free the space
	 * used by old versions of classes that are not used anymore, while preserving
	 * various versions when classes are redefined at runtime.
	 */
	private boolean itsDisposable = false;

	public ClassInfo(IStructureDatabase aDatabase, int aId)
	{
		super(aDatabase, aId);
	}

	public ClassInfo(IStructureDatabase aDatabase, int aId, String aName)
	{
		this (aDatabase, aId, aName, null, null);
	}
	
	public ClassInfo(
			IStructureDatabase aDatabase, 
			int aId, 
			String aName, 
			ClassInfo aSupertype,
			ClassInfo[] aInterfaces)
	{
		super(aDatabase, aId, aName);
		
		itsSupertype = aSupertype;
		itsInterfaces = aInterfaces;
	}

	public boolean isDisposable()
	{
		return itsDisposable;
	}

	public void setDisposable(boolean aDisposable)
	{
		itsDisposable = aDisposable;
	}

	/**
	 * Registers the given field info object.
	 */
	public void register(IFieldInfo aFieldInfo)
	{
		itsFieldsMap.put (aFieldInfo.getName(), aFieldInfo);
	}
	
	/**
	 * Registers the given behavior info object.
	 */
	public void register(IBehaviorInfo aBehavior)
	{
		itsBehaviorsMap.put(getKey(aBehavior), aBehavior);
	}
	
	public IFieldInfo getField(String aName)
	{
		return itsFieldsMap.get(aName);
	}
	
	public IBehaviorInfo getBehavior(String aName, ITypeInfo[] aArgumentTypes)
	{
		return itsBehaviorsMap.get(getKey(aName, aArgumentTypes));
	}
	
	public Iterable<IFieldInfo> getFields()
	{
		return itsFieldsMap.values();
	}
	
	public Iterable<IBehaviorInfo> getBehaviors()
	{
		return itsBehaviorsMap.values();
	}
	
	public ClassInfo[] getInterfaces()
	{
		return itsInterfaces;
	}

	public void setInterfaces(ClassInfo[] aInterfaces)
	{
		itsInterfaces = aInterfaces;
	}

	public void setSupertype(ClassInfo aSupertype)
	{
		itsSupertype = aSupertype;
	}

	public ClassInfo getSupertype()
	{
		return itsSupertype;
	}

	public int getSize()
	{
		return 1;
	}

	public boolean isArray()
	{
		return false;
	}

	public boolean isPrimitive()
	{
		return false;
	}

	public boolean isVoid()
	{
		return false;
	}

	private String getKey(IBehaviorInfo aBehavior)
	{
		return getKey(aBehavior.getName(), aBehavior.getArgumentTypes());
	}
	
	private String getKey(String aName, ITypeInfo[] aArgumentTypes)
	{
		StringBuilder theBuilder = new StringBuilder();
		theBuilder.append(aName);
		for (ITypeInfo theType : aArgumentTypes)
		{
			theBuilder.append('|');
			theBuilder.append(theType.getName());
		}
		
		return theBuilder.toString();
	}
	
	@Override
	public String toString()
	{
		return "Class ("+getId()+", "+getName()+")";
	}
	
	
	public ClassInfo createUncertainClone()
	{
		ClassInfo theClone = (ClassInfo) super.clone();
		theClone.changeName(getName()+ "?");
		return theClone;
	}
}
