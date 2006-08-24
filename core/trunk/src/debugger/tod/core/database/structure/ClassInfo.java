/*
 * Created on Nov 15, 2005
 */
package tod.core.database.structure;

import java.util.HashMap;
import java.util.Map;

import tod.core.database.browser.ILocationsRepository;

public class ClassInfo extends TypeInfo implements IClassInfo
{
	private ClassInfo itsSupertype;
	private ClassInfo[] itsInterfaces;
	
	private Map<String, IFieldInfo> itsFieldsMap = new HashMap<String, IFieldInfo>();
	private Map<String, IBehaviorInfo> itsBehaviorsMap = new HashMap<String, IBehaviorInfo>();
	

	public ClassInfo(ILocationsRepository aTrace, int aId)
	{
		super(aTrace, aId);
	}

	public ClassInfo(ILocationsRepository aTrace, int aId, String aName)
	{
		this (aTrace, aId, aName, null, null);
	}
	
	public ClassInfo(
			ILocationsRepository aTrace,
			int aId, 
			String aName, 
			ClassInfo aSupertype,
			ClassInfo[] aInterfaces)
	{
		super(aTrace, aId, aName);
		
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
}
