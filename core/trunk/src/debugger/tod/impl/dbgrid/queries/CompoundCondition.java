/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.ArrayList;
import java.util.List;

/**
 * A condition that is compound of various other {@link EventCondition}s.
 * @author gpothier
 */
public abstract class CompoundCondition extends EventCondition
{
	private List<EventCondition> itsConditions = new ArrayList<EventCondition>();

	protected List<EventCondition> getConditions()
	{
		return itsConditions;
	}
	
	public void addCondition(EventCondition aCondition)
	{
		itsConditions.add(aCondition);
	}

}
