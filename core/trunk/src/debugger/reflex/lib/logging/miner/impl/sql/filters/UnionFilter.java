/*
 * Created on Nov 26, 2004
 */
package reflex.lib.logging.miner.impl.sql.filters;

import java.util.List;

import tod.core.model.trace.IEventFilter;

/**
 * @author gpothier
 */
public class UnionFilter extends SQLCompoundFilter
{

	public UnionFilter()
	{
	}
	
	public UnionFilter(IEventFilter... aFilters)
	{
		super(aFilters);
	}
	
	public UnionFilter(List<IEventFilter> aFilters)
	{
		super(aFilters);
	}
	
	
	
	protected String getOperator()
	{
		return "OR";
	}
}
