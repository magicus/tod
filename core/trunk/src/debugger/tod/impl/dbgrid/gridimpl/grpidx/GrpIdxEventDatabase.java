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

import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_SIZE;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import tod.agent.DebugFlags;
import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RINodeEventIterator;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.dbnode.file.IndexTuple;
import tod.impl.dbgrid.gridimpl.AbstractEventDatabase;
import tod.impl.dbgrid.gridimpl.grpidx.GrpIdxDatabaseNode.IndexKind;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.queries.EventCondition;

/**
 * This class manages an event database for a debugging session.
 * An event database consists in an event list and a number
 * of indexes.
 * In the grouped index schemes, the database does not automatically
 * index incoming events bu instead waits for indexing requests.
 * @author gpothier
 */
public class GrpIdxEventDatabase extends AbstractEventDatabase
{
	private final HardPagedFile itsFile;
	
	private final EventList itsEventList;
	private final Indexes itsIndexes;
	
	private long itsAddedEventsCount = 0;
	
	/**
	 * Creates a new database using the specified file.
	 */
	public GrpIdxEventDatabase(int aNodeId, File aFile) 
	{
		Monitor.getInstance().register(this);
		try
		{
			itsFile = new HardPagedFile(aFile, DB_PAGE_SIZE);
			
			itsEventList = new EventList(aNodeId, itsFile);
			itsIndexes = new Indexes(itsFile);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Causes this database to recursively unregister from the monitor.
	 */
	public void unregister()
	{
		Monitor.getInstance().unregister(this);
		itsFile.unregister();
		itsEventList.unregister();
		itsIndexes.unregister();
	}

	@Override
	public int flush()
	{
		return 0;
	}
	
	public Indexes getIndexes()
	{
		return itsIndexes;
	}

	@Override
	public BidiIterator<GridEvent> evaluate(EventCondition aCondition, long aTimestamp)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public RINodeEventIterator getIterator(EventCondition aCondition) throws RemoteException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long[] getEventCounts(
			EventCondition aCondition,
			long aT1, 
			long aT2,
			int aSlotsCount, 
			boolean aForceMergeCounts) throws RemoteException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Pushes a single message to this node.
	 * Messages can be events or parent/child
	 * relations.
	 */
	@Override
	public void push(GridEvent aEvent)
	{
		if (DebugFlags.SKIP_EVENTS) return;
		itsAddedEventsCount++;
		itsEventList.add(aEvent);
	}
	
	/**
	 * Appends an index tuple to the specified index.
	 */
	public void index(IndexKind aIndexKind, int aIndex, IndexTuple aTuple)
	{
		aIndexKind.index(getIndexes(), aIndex, aTuple);
	}
	
	/**
	 * Returns the amount of disk storage used by this node.
	 */
	public long getStorageSpace()
	{
		return itsFile.getStorageSpace();
	}
	
	public long getEventsCount()
	{
		return itsEventList.getEventsCount();
	}
}
