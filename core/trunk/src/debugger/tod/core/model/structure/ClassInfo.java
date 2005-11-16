/*
 * Created on Nov 15, 2005
 */
package tod.core.model.structure;

import java.util.HashMap;
import java.util.Map;

import tod.core.model.trace.ILocationTrace;

public class ClassInfo extends TypeInfo
{
	private ClassInfo itsSupertype;
	private ClassInfo[] itsInterfaces;
	
	private Map<String, FieldInfo> itsFieldsMap = new HashMap<String, FieldInfo>();
	private Map<String, BehaviorInfo> itsBehaviorsMap = new HashMap<String, BehaviorInfo>();
	

	public ClassInfo(ILocationTrace aTrace, int aId)
	{
		super(aTrace, aId);
	}

	public ClassInfo(ILocationTrace aTrace, int aId, String aName)
	{
		this (aTrace, aId, aName, null, null);
	}
	
	public ClassInfo(
			ILocationTrace aTrace,
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
	public void register(FieldInfo aFieldInfo)
	{
		itsFieldsMap.put (aFieldInfo.getName(), aFieldInfo);
	}
	
	/**
	 * Registers the given behavior info object.
	 */
	public void register(BehaviorInfo aBehaviorInfo)
	{
		itsBehaviorsMap.put(getKey(aBehaviorInfo), aBehaviorInfo);
	}
	
	public FieldInfo getField(String aName)
	{
		return itsFieldsMap.get(aName);
	}
	
	public BehaviorInfo getBehavior(String aName, TypeInfo[] aArgumentTypes)
	{
		return itsBehaviorsMap.get(getKey(aName, aArgumentTypes));
	}
	
	public Iterable<FieldInfo> getFields()
	{
		return itsFieldsMap.values();
	}
	
	public Iterable<BehaviorInfo> getBehaviors()
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

	@Override
	public int getSize()
	{
		return 1;
	}

	@Override
	public boolean isArray()
	{
		return false;
	}

	@Override
	public boolean isPrimitive()
	{
		return false;
	}

	@Override
	public boolean isVoid()
	{
		return false;
	}

	private String getKey(BehaviorInfo aBehavior)
	{
		return getKey(aBehavior.getName(), aBehavior.getArgumentTypes());
	}
	
	private String getKey(String aName, TypeInfo[] aArgumentTypes)
	{
		StringBuilder theBuilder = new StringBuilder();
		theBuilder.append(aName);
		for (TypeInfo theType : aArgumentTypes)
		{
			theBuilder.append('|');
			theBuilder.append(theType.getName());
		}
		
		return theBuilder.toString();
	}
}
