/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;

import java.util.ArrayList;
import java.util.List;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventFilter;

import zz.utils.Utils;

/**
 * A condition that is compound of various other {@link EventCondition}s.
 * @author gpothier
 */
public abstract class CompoundCondition extends EventCondition
implements ICompoundFilter
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
	
	public final void add(IEventFilter aFilter) throws IllegalStateException
	{
		addCondition((EventCondition) aFilter);
	}

	public final List<IEventFilter> getFilters()
	{
		return (List) itsConditions;
	}

	public final void remove(IEventFilter aFilter) throws IllegalStateException
	{
		throw new UnsupportedOperationException();
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
	
	@Override
	public int getClausesCount()
	{
		int theCount = 0;
		for (EventCondition theCondition : getConditions()) 
		{
			theCount += theCondition.getClausesCount();
		}
		
		return theCount;
	}
	
}
