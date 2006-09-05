/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid.aggregator;

import tod.impl.dbgrid.GridMaster;

public class QueryAggregator implements RIQueryAggregator
{
	private GridMaster itsMaster;

	public QueryAggregator(GridMaster aMaster)
	{
		itsMaster = aMaster;
	}
}
