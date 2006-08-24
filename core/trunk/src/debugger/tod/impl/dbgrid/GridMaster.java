/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import tod.impl.dbgrid.aggregator.QueryAggregator;
import tod.impl.dbgrid.aggregator.RIQueryAggregator;
import tod.impl.dbgrid.dbnode.RIDatabaseNode;
import tod.impl.dbgrid.dispatcher.GridEventCollector;

/**
 * The entry point to the database grid.
 * Manages configuration and discovery of database nodes,
 * acts as a factory for {@link GridEventCollector}s
 * and {@link QueryAggregator}.
 * @author gpothier
 */
public class GridMaster implements RIGridMaster
{
	private List<RIDatabaseNode> itsNodes = new ArrayList<RIDatabaseNode>();
	private QueryAggregator itsAggregator = new QueryAggregator(this);

	public void registerNode(RIDatabaseNode aNode)
	{
		itsNodes.add(aNode);
	}
	
	public int createCollector(int aHostId) throws RemoteException
	{
	}

	public RIQueryAggregator getAggregator()
	{
		return itsAggregator;
	}

	
}
