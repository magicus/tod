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
package tod.impl.dbgrid.gridimpl.grpidx;

import java.net.Socket;
import java.rmi.RemoteException;

import tod.agent.DebugFlags;
import tod.core.database.browser.ILocationStore;
import tod.impl.dbgrid.db.EventReorderingBuffer;
import tod.impl.dbgrid.db.HierarchicalIndex;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.EventReorderingBuffer.ReorderingBufferListener;
import tod.impl.dbgrid.db.RoleIndexSet.RoleTuple;
import tod.impl.dbgrid.db.StdIndexSet.StdTuple;
import tod.impl.dbgrid.dispatch.DBNodeProxy;
import tod.impl.dbgrid.dispatch.DispatchNodeProxy;
import tod.impl.dbgrid.dispatch.LeafEventDispatcher;
import tod.impl.dbgrid.dispatch.RIDispatchNode;
import tod.impl.dbgrid.gridimpl.grpidx.GrpIdxDatabaseNode.IndexKind;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;

/**
 * In this implementation of the event dispatcher the events
 * are sent to the nodes in a round robin fashion but the
 * nodes do not index on the attributes of the event. Instead
 * the dispatcher sends index data to the nodes, grouping indexes
 * so that a single iondex is not spread across the nodes.
 * @author gpothier
 */
public class GrpIdxEventDispatcher extends LeafEventDispatcher
implements ReorderingBufferListener
{
	/**
	 * This flag is set to true as soon as the first event is dispatched.
	 * Database nodes cannot be added after dispatching has started.
	 */
	private boolean itsStartedDispatching = false;

	/**
	 * Round-robin node index for sending events. 
	 */
	private int itsCurrentNode = 0;
	
	private long itsDroppedEvents = 0;
	private long itsUnorderedEvents = 0;
	private long itsProcessedEvents = 0;
	private long itsLastAddedTimestamp;
	private long itsLastProcessedTimestamp;	
	private EventReorderingBuffer itsReorderingBuffer = new EventReorderingBuffer(this);

	
	private DispatcherIndexes itsIndexes = new DispatcherIndexes();
	
	public GrpIdxEventDispatcher(boolean aConnectToMaster, ILocationStore aLocationStore) throws RemoteException
	{
		super(aConnectToMaster, aLocationStore);
		Monitor.getInstance().register(this);
	}
	
	@Override
	protected void addChild(DispatchNodeProxy aProxy)
	{
		if (itsStartedDispatching) 
			throw new IllegalStateException("Dispatching already started, cannot add new node.");
		
		super.addChild(aProxy);
	}
	
	@Override
	protected DispatchNodeProxy createProxy(
			RIDispatchNode aConnectable, 
			Socket aSocket, 
			String aId)
	{
		return new GrpIdxDBNodeProxy(aConnectable, aSocket, aId);
	}

	@Override
	protected GrpIdxDBNodeProxy getNode(int aIndex)
	{
		return (GrpIdxDBNodeProxy) super.getNode(aIndex);
	}
	
	public synchronized int flush()
	{
		int theCount = 0;
		System.out.println("GrpIdxEventDispatcher: flushing...");
		while (! itsReorderingBuffer.isEmpty())
		{
			processEvent(itsReorderingBuffer.pop());
			theCount++;
		}
		System.out.println("GrpIdxEventDispatcher: flushed "+theCount+" events...");
		theCount += super.flush();
		
		return theCount;
	}

	@Override
	protected void dispatchEvent0(GridEvent aEvent)
	{
		aEvent = (GridEvent) aEvent.clone();
		
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastAddedTimestamp) itsUnorderedEvents++;
		else itsLastAddedTimestamp = theTimestamp;
		
		if (DebugFlags.DISABLE_REORDER)
		{
			processEvent(aEvent);
		}
		else
		{
			while (itsReorderingBuffer.isFull()) processEvent(itsReorderingBuffer.pop());
			itsReorderingBuffer.push(aEvent);
		}
	}
	
	private void processEvent(GridEvent aEvent)
	{
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastProcessedTimestamp)
		{
			eventDropped();
			return;
		}
		
		itsLastProcessedTimestamp = theTimestamp;
		
		itsProcessedEvents++;
		
		itsStartedDispatching = true;
		
		// Send event data to the next node in the round-robin scheme
		GrpIdxDBNodeProxy theProxy = getNode(itsCurrentNode);
		long theId = theProxy.pushEvent(aEvent);
		itsCurrentNode = (itsCurrentNode+1) % getChildrenCount();
		
		// Send index data to adequate nodes.
		aEvent.index(itsIndexes, theId);
	}
	

	@Probe(key = "Out of order events", aggr = AggregationType.SUM)
	public long getUnorderedEvents()
	{
		return itsUnorderedEvents;
	}

	@Probe(key = "DROPPED EVENTS", aggr = AggregationType.SUM)
	public long getDroppedEvents()
	{
		return itsDroppedEvents;
	}
	
	public void eventDropped()
	{
		itsDroppedEvents++;
	}

	/**
	 * A "fake" index structure that permits to dispatch indexing requests.
	 * @author gpothier
	 */
	private class DispatcherIndexes extends Indexes
	{
		@Override
		public HierarchicalIndex<StdTuple> getArrayIndexIndex(int aIndex)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public HierarchicalIndex<RoleTuple> getBehaviorIndex(int aIndex)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public HierarchicalIndex<StdTuple> getDepthIndex(int aIndex)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public HierarchicalIndex<StdTuple> getFieldIndex(int aIndex)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public HierarchicalIndex<StdTuple> getHostIndex(int aIndex)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public HierarchicalIndex<StdTuple> getLocationIndex(int aIndex)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public HierarchicalIndex<RoleTuple> getObjectIndex(int aIndex)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public HierarchicalIndex<StdTuple> getThreadIndex(int aIndex)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public HierarchicalIndex<StdTuple> getTypeIndex(int aIndex)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public HierarchicalIndex<StdTuple> getVariableIndex(int aIndex)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void indexArrayIndex(int aIndex, StdTuple aTuple)
		{
			GrpIdxDBNodeProxy theNode = getNode(aIndex % getChildrenCount());
			theNode.pushIndexData(IndexKind.INDEX, aIndex, aTuple);
		}

		@Override
		public void indexBehavior(int aIndex, RoleTuple aTuple)
		{
			GrpIdxDBNodeProxy theNode = getNode(aIndex % getChildrenCount());
			theNode.pushIndexData(IndexKind.BEHAVIOR, aIndex, aTuple);
		}

		@Override
		public void indexDepth(int aIndex, StdTuple aTuple)
		{
			GrpIdxDBNodeProxy theNode = getNode(aIndex % getChildrenCount());
			theNode.pushIndexData(IndexKind.DEPTH, aIndex, aTuple);
		}

		@Override
		public void indexField(int aIndex, StdTuple aTuple)
		{
			GrpIdxDBNodeProxy theNode = getNode(aIndex % getChildrenCount());
			theNode.pushIndexData(IndexKind.FIELD, aIndex, aTuple);
		}

		@Override
		public void indexHost(int aIndex, StdTuple aTuple)
		{
			GrpIdxDBNodeProxy theNode = getNode(aIndex % getChildrenCount());
			theNode.pushIndexData(IndexKind.HOST, aIndex, aTuple);
		}

		@Override
		public void indexLocation(int aIndex, StdTuple aTuple)
		{
			GrpIdxDBNodeProxy theNode = getNode(aIndex % getChildrenCount());
			theNode.pushIndexData(IndexKind.LOCATION, aIndex, aTuple);
		}

		@Override
		public void indexObject(int aIndex, RoleTuple aTuple)
		{
			GrpIdxDBNodeProxy theNode = getNode(aIndex % getChildrenCount());
			theNode.pushIndexData(IndexKind.OBJECT, aIndex, aTuple);
		}

		@Override
		public void indexThread(int aIndex, StdTuple aTuple)
		{
			GrpIdxDBNodeProxy theNode = getNode(aIndex % getChildrenCount());
			theNode.pushIndexData(IndexKind.THREAD, aIndex, aTuple);
		}

		@Override
		public void indexType(int aIndex, StdTuple aTuple)
		{
			GrpIdxDBNodeProxy theNode = getNode(aIndex % getChildrenCount());
			theNode.pushIndexData(IndexKind.TYPE, aIndex, aTuple);
		}

		@Override
		public void indexVariable(int aIndex, StdTuple aTuple)
		{
			GrpIdxDBNodeProxy theNode = getNode(aIndex % getChildrenCount());
			theNode.pushIndexData(IndexKind.VARIABLE, aIndex, aTuple);
		}
	}

}
