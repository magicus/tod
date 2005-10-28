/*
 * Created on Oct 25, 2004
 */
package tod.core.model.structure;

import java.util.HashMap;
import java.util.Map;

import tod.core.ILogCollector;

/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a type (class or interface).
 * @author gpothier
 */
public class TypeInfo extends LocationInfo
{
	private TypeInfo itsSupertype;
	private TypeInfo[] itsInterfaces;
	
	private Map<String, FieldInfo> itsFieldsMap = new HashMap<String, FieldInfo>();
	private Map<String, BehaviorInfo> itsBehaviorsMap = new HashMap<String, BehaviorInfo>();
	

	public TypeInfo(int aId)
	{
		super(aId);
	}

	public TypeInfo(int aId, String aName, TypeInfo aSupertype, TypeInfo[] aInterfaces)
	{
		super(aId, aName);
		
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
		itsBehaviorsMap.put(aBehaviorInfo.getName(), aBehaviorInfo);
	}
	
	public FieldInfo getField(String aName)
	{
		return itsFieldsMap.get(aName);
	}
	
	public BehaviorInfo getBehavior(String aName)
	{
		return itsBehaviorsMap.get(aName);
	}
	
	public Iterable<FieldInfo> getFields()
	{
		return itsFieldsMap.values();
	}
	
	public Iterable<BehaviorInfo> getBehaviors()
	{
		return itsBehaviorsMap.values();
	}
	
	public TypeInfo[] getInterfaces()
	{
		return itsInterfaces;
	}

	public void setInterfaces(TypeInfo[] aInterfaces)
	{
		itsInterfaces = aInterfaces;
	}

	public void setSupertype(TypeInfo aSupertype)
	{
		itsSupertype = aSupertype;
	}

	public TypeInfo getSupertype()
	{
		return itsSupertype;
	}
}
