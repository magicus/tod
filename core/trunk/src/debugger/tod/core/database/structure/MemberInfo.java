/*
 * Created on Oct 25, 2004
 */
package tod.core.database.structure;

import tod.core.database.browser.ILocationsRepository;

/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a type member (method, constructor, field).
 * @author gpothier
 */
public abstract class MemberInfo extends LocationInfo implements IMemberInfo
{
	private IClassInfo itsType;
	
	public MemberInfo(int aId, IClassInfo aTypeInfo, String aName)
	{
		super(aId, aName);
		itsType = aTypeInfo;
	}
	
	public IClassInfo getType()
	{
		return itsType;
	}	
}
