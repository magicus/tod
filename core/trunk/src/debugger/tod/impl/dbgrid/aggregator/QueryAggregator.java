/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid.aggregator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.BufferedBidiIterator;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.db.RIEventIterator;
import tod.impl.dbgrid.db.RINodeEventIterator;
import tod.impl.dbgrid.dispatch.RIDatabaseNode;
import tod.impl.dbgrid.merge.DisjunctionIterator;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;
import zz.utils.Future;

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
	
	private void initIterators(final long aTimestamp)
	{
		final List<RIDatabaseNode> theNodes = itsMaster.getNodes();
		final EventIterator[] theIterators = new EventIterator[theNodes.size()];
		
		List<Future<EventIterator>> theFutures = new ArrayList<Future<EventIterator>>();
		
		for (int i=0;i<theNodes.size();i++)
		{
			final int i0 = i;
			theFutures.add(new Future<EventIterator>()
					{
						@Override
						protected EventIterator fetch() throws Throwable
						{
							RIDatabaseNode theNode = theNodes.get(i0);
							RINodeEventIterator theIterator = theNode.getIterator(itsCondition);
							
							theIterator.setNextTimestamp(aTimestamp);
							theIterators[i0] = new EventIterator(theIterator);
							
							return theIterators[i0];
						}
					});
			
		}

		// Ensure all futures have completed
		for (Future<EventIterator> theFuture : theFutures) theFuture.get();
		
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
	private static class EventIterator extends BufferedBidiIterator<GridEvent[], GridEvent>
	{
		private RIEventIterator itsIterator;

		public EventIterator(RIEventIterator aIterator)
		{
			itsIterator = aIterator;
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
		protected GridEvent[] fetchPreviousBuffer()
		{
			try
			{
				return itsIterator.previous(DebuggerGridConfig.QUERY_ITERATOR_BUFFER_SIZE);
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
		public MergeIterator(BidiIterator<GridEvent>[] aIterators)
		{
			super(aIterators);
		}

		@Override
		protected long getKey(GridEvent aItem)
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
