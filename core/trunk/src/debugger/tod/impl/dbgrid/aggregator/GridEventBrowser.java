/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;
import zz.utils.BufferedIterator;

/**
 * Implementation of {@link IEventBrowser} that serves as the client-side
 * of a {@link QueryAggregator}.
 * @author gpothier
 */
public class GridEventBrowser extends BufferedIterator<ILogEvent[], ILogEvent>
implements IEventBrowser
{
	private final GridLogBrowser itsBrowser;
	private RIQueryAggregator itsAggregator;
	
	public GridEventBrowser(GridLogBrowser aBrowser, EventCondition aCondition) throws RemoteException
	{
		itsBrowser = aBrowser;
		itsAggregator = itsBrowser.getMaster().createAggregator(aCondition);
		reset();
	}
	
	
	@Override
	protected ILogEvent[] fetchNextBuffer()
	{
		try
		{
			GridEvent[] theGridEvents = itsAggregator.next(DebuggerGridConfig.QUERY_ITERATOR_BUFFER_SIZE);
			if (theGridEvents == null) return null;
			
			ILogEvent[] theLogEvents = new ILogEvent[theGridEvents.length];
			for(int i=0;i<theGridEvents.length;i++) theLogEvents[i] = convert(theGridEvents[i]);
			return theLogEvents;
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}


	@Override
	protected ILogEvent get(ILogEvent[] aBuffer, int aIndex)
	{
		return aBuffer[aIndex];
	}


	@Override
	protected int getSize(ILogEvent[] aBuffer)
	{
		return aBuffer.length;
	}


	/**
	 * Converts a {@link GridEvent} into an {@link ILogEvent}.
	 */
	private ILogEvent convert(GridEvent aEvent)
	{
		return aEvent.toLogEvent(itsBrowser);
	}
	
	public long getEventCount()
	{
		return getEventCount(0, Long.MAX_VALUE, false);
	}

	public long getEventCount(long aT1, long aT2, boolean aForceMergeCounts)
	{
		return getEventCounts(aT1, aT2, 1, aForceMergeCounts)[0];
	}

	public long[] getEventCounts(long aT1, long aT2, int aSlotsCount, boolean aForceMergeCounts)
	{
		try
		{
			return itsAggregator.getEventCounts(aT1, aT2, aSlotsCount, aForceMergeCounts);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public List<ILogEvent> getEvents(long aT1, long aT2)
	{
		throw new UnsupportedOperationException();
	}

	public ILogEvent previous()
	{
		throw new UnsupportedOperationException();
	}

	public boolean hasPrevious()
	{
		throw new UnsupportedOperationException();
	}

	
	
	public void setNextEvent(ILogEvent aEvent)
	{
		throw new UnsupportedOperationException();
	}


	public void setPreviousEvent(ILogEvent aEvent)
	{
		throw new UnsupportedOperationException();
	}

	public void setNextTimestamp(long aTimestamp)
	{
		try
		{
			itsAggregator.setNextTimestamp(aTimestamp);
			reset();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void setPreviousTimestamp(long aTimestamp)
	{
		throw new UnsupportedOperationException();
	}
}
