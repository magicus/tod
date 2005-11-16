/*
 * Created on Oct 25, 2004
 */
package tod.core.model.structure;

import tod.core.ILogCollector;
import tod.core.model.trace.ILocationTrace;

/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a field.
 * @author gpothier
 */
public class FieldInfo extends MemberInfo
{

	public FieldInfo(ILocationTrace aTrace, int aId, ClassInfo aTypeInfo, String aName)
	{
		super(aTrace, aId, aTypeInfo, aName);
		
	}

}
