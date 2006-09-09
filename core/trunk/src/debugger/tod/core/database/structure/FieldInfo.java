/*
 * Created on Oct 25, 2004
 */
package tod.core.database.structure;

import tod.core.database.browser.ILocationsRepository;

/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a field.
 * @author gpothier
 */
public class FieldInfo extends MemberInfo implements IFieldInfo
{

	public FieldInfo(ILocationsRepository aTrace, int aId, IClassInfo aTypeInfo, String aName)
	{
		super(aTrace, aId, aTypeInfo, aName);
		
	}

}
