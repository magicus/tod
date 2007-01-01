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
package tod.core.database.structure;

import java.util.HashMap;
import java.util.Map;

import tod.core.ILocationRegisterer;
import tod.core.database.browser.ILocationsRepository;

public class ClassInfo extends TypeInfo implements IClassInfo
{
	private ClassInfo itsSupertype;
	private ClassInfo[] itsInterfaces;
	
	private Map<String, IFieldInfo> itsFieldsMap = new HashMap<String, IFieldInfo>();
	private Map<String, IBehaviorInfo> itsBehaviorsMap = new HashMap<String, IBehaviorInfo>();
	

	public ClassInfo(int aId)
	{
		super(aId);
	}

	public ClassInfo(int aId, String aName)
	{
		this (aId, aName, null, null);
	}
	
	public ClassInfo(
			int aId, 
			String aName, 
			ClassInfo aSupertype,
			ClassInfo[] aInterfaces)
	{
		super(aId, aName);
		
		itsSupertype = aSupertype;
		itsInterfaces = aInterfaces;
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
	
	public void register(ILocationRegisterer aRegistrer)
	{
		int theInterfaceCount = getInterfaces() != null ?
				getInterfaces().length
				: 0;
				
		int[] theInterfaceIds = new int[theInterfaceCount];
				    
		for(int i=0;i<theInterfaceCount;i++)
		{
			theInterfaceIds[i] = getInterfaces()[i].getId();
		}
		
		aRegistrer.registerType(
				getId(), 
				getName(), 
				getSupertype() != null ? getSupertype().getId() : 0, 
				theInterfaceIds);
	}
}
