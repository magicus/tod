/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.ArrayList;
import java.util.List;

import zz.utils.Utils;

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

	@Override
	protected String toString(int aIndent)
	{
		StringBuilder theBuilder = new StringBuilder();
		Utils.indentln(theBuilder, aIndent);
		theBuilder.append(getClass().getSimpleName());
		
		for (EventCondition theCondition : getConditions())
		{
			Utils.indentln(theBuilder, aIndent+2);
			theBuilder.append(theCondition.toString(aIndent+2));
		}
		
		theBuilder.append('\n');
		return theBuilder.toString();
	}
}
