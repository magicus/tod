/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid.aggregator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dbnode.RIDatabaseNode;
import tod.impl.dbgrid.dbnode.RIEventIterator;
import tod.impl.dbgrid.dbnode.RINodeEventIterator;
import tod.impl.dbgrid.merge.DisjunctionIterator;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;
import zz.utils.BufferedIterator;

/**
 * Aggregates the partial results of a query obtained from the nodes.
 * @author gpothier
 */
public class QueryAggregator extends UnicastRemoteObject
implements RIQueryAggregator
{
	private final GridMaster itsMaster;
	private final EventCondition itsCondition;
	private MergeIterator itsMergeIterator;

	public QueryAggregator(GridMaster aMaster, EventCondition aCondition) throws RemoteException
	{
		itsMaster = aMaster;
		itsCondition = aCondition;
		initIterators(0);
	}
	
	private void initIterators(long aTimestamp) throws RemoteException
	{
		List<RIDatabaseNode> theNodes = itsMaster.getNodes();
		EventIterator[] theIterators = new EventIterator[theNodes.size()];
		
		for (int i=0;i<theNodes.size();i++)
		{
			RIDatabaseNode theNode = theNodes.get(i);
			
			RINodeEventIterator theIterator = theNode.getIterator(itsCondition);
			
			theIterator.setNextTimestamp(aTimestamp);
			theIterators[i] = new EventIterator(theIterator);
		}
		
		itsMergeIterator = new MergeIterator(theIterators);
	}

	public GridEvent[] next(int aCount)
	{
		List<GridEvent> theList = new ArrayList<GridEvent>(aCount);
		for (int i=0;i<aCount;i++)
		{
			if (itsMergeIterator.hasNext()) theList.add(itsMergeIterator.next());
			else break;
		}
		
		return theList.size() > 0 ?
				theList.toArray(new GridEvent[theList.size()])
				: null;
	}

	public void setNextTimestamp(long aTimestamp) throws RemoteException
	{
		initIterators(aTimestamp);
	}

	public GridEvent[] previous(int aCount)
	{
		throw new UnsupportedOperationException();
	}

	public void setPreviousTimestamp(long aTimestamp)
	{
		throw new UnsupportedOperationException();
	}
	
	public long[] getEventCounts(
			final long aT1,
			final long aT2, 
			final int aSlotsCount,
			final boolean aForceMergeCounts) throws RemoteException
	{
//		System.out.println("Aggregating counts...");
		
		long t0 = System.currentTimeMillis();
		long[] theCounts = new long[aSlotsCount];

		// Sum results from all nodes.
		List<RIDatabaseNode> theNodes = itsMaster.getNodes();
		List<Future<long[]>> theFutures = new ArrayList<Future<long[]>>();
		
		for (RIDatabaseNode theNode : theNodes)
		{
			final RIDatabaseNode theNode0 = theNode;
			theFutures.add (new Future<long[]>()
			{
				@Override
				protected long[] fetch() throws Throwable
				{
					long[] theEventCounts = theNode0.getEventCounts(
							itsCondition,
							aT1,
							aT2, 
							aSlotsCount,
							aForceMergeCounts);
					return theEventCounts;
				}
			});
		}
		
		for (Future<long[]> theFuture : theFutures)
		{
			long[] theNodeCounts = theFuture.get();
			for(int i=0;i<aSlotsCount;i++) theCounts[i] += theNodeCounts[i];
		}
		
		long t1 = System.currentTimeMillis();
		
//		System.out.println("Computed counts in "+(t1-t0)+"ms.");
		
		return theCounts;
	}

	/**
	 * A real iterator that wraps a {@link RIEventIterator}
	 * @author gpothier
	 */
	private static class EventIterator extends BufferedIterator<GridEvent[], GridEvent>
	{
		private RIEventIterator itsIterator;

		public EventIterator(RIEventIterator aIterator)
		{
			itsIterator = aIterator;
			reset();
		}
		
		@Override
		protected GridEvent[] fetchNextBuffer()
		{
			try
			{
				return itsIterator.next(DebuggerGridConfig.QUERY_ITERATOR_BUFFER_SIZE);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		protected int getSize(GridEvent[] aBuffer)
		{
			return aBuffer.length;
		}

		@Override
		protected GridEvent get(GridEvent[] aBuffer, int aIndex)
		{
			return aBuffer[aIndex];
		}
	}
	
	/**
	 * The iterator that merges results from all the nodes
	 * @author gpothier
	 */
	private static class MergeIterator extends DisjunctionIterator<GridEvent>
	{
		public MergeIterator(Iterator<GridEvent>[] aIterators)
		{
			super(aIterators);
		}

		@Override
		protected long getTimestamp(GridEvent aItem)
		{
			return aItem.getTimestamp();
		}

		@Override
		protected boolean sameEvent(GridEvent aItem1, GridEvent aItem2)
		{
			return aItem1.getHost() == aItem2.getHost()
				&& aItem1.getThread() == aItem2.getThread()
				&& aItem1.getTimestamp() == aItem2.getTimestamp();
		}
	}
}
