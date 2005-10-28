/*
 * Created on Oct 25, 2004
 */
package tod.core.model.structure;

import tod.core.ILogCollector;

/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a field.
 * @author gpothier
 */
public class FieldInfo extends MemberInfo
{

	public FieldInfo(int aId, TypeInfo aTypeInfo, String aName)
	{
		super(aId, aTypeInfo, aName);
		
	}

}
