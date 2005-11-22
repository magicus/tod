/*
 * Created on Oct 25, 2004
 */
package tod.core.model.structure;

import tod.core.ILogCollector;
import tod.core.model.trace.ILocationTrace;

/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a type member (method, constructor, field).
 * @author gpothier
 */
public abstract class MemberInfo extends LocationInfo implements IMemberInfo
{
	private IClassInfo itsType;
	
	public MemberInfo(ILocationTrace aTrace, int aId, IClassInfo aTypeInfo, String aName)
	{
		super(aTrace, aId, aName);
		itsType = aTypeInfo;
	}
	
	public IClassInfo getType()
	{
		return itsType;
	}	
}
