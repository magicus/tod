/*
 * Created on Nov 26, 2004
 */
package reflex.lib.logging.miner.impl.sql.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.trace.ICompoundFilter;
import tod.core.model.trace.IEventFilter;

/**
 * Base for SQL compound filters
 * @author gpothier
 */
public abstract class SQLCompoundFilter extends SQLFilter implements ICompoundFilter
{
	private List<IEventFilter> itsFilters;
	
	public SQLCompoundFilter()
	{
		this (new ArrayList<IEventFilter>());
	}
	
	public SQLCompoundFilter(List<IEventFilter> aFilters)
	{
		itsFilters = aFilters;
	}
	
	public SQLCompoundFilter(IEventFilter... aFilters)
	{
		itsFilters = new ArrayList<IEventFilter>(Arrays.asList(aFilters));
	}
	
	public List<IEventFilter> getFilters()
	{
		return itsFilters;
	}
	
	public void add (IEventFilter aFilter)
	{
		itsFilters.add(aFilter);
	}
	
	public void remove (IEventFilter aFilter)
	{
		itsFilters.remove(aFilter);
	}
	
	/**
	 * Returns the SQL operator used to combine the various filters;
	 */
	protected abstract String getOperator();
	
	public String getSQLCondition()
	{
		StringBuffer theBuffer = new StringBuffer ("(");
		
		String theOperator = getOperator();
		
		boolean theFirst = true;
		for (IEventFilter theFilter : getFilters()) 
		{
			SQLFilter theSQLFilter = (SQLFilter) theFilter;
			
			if (! theFirst) theBuffer.append(theOperator);
			else theFirst = false;
			
			theBuffer.append(theSQLFilter.getSQLCondition());
		}
		
		theBuffer.append(')');
		return theBuffer.toString();
	}

}
