/*
 * Created on Oct 25, 2004
 */
package tod.core.model.structure;

import tod.core.ILogCollector;

/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a type member (method, constructor, field).
 * @author gpothier
 */
public abstract class MemberInfo extends LocationInfo
{
	private TypeInfo itsTypeInfo;
	
	public MemberInfo(int aId, TypeInfo aTypeInfo, String aName)
	{
		super(aId, aName);
		itsTypeInfo = aTypeInfo;
	}
	
	public TypeInfo getType()
	{
		return itsTypeInfo;
	}	
}
