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

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.event.ILogEvent;
import tod.impl.dbgrid.BufferedBidiIterator;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;

/**
 * Implementation of {@link IEventBrowser} that serves as the client-side
 * of a {@link QueryAggregator}.
 * @author gpothier
 */
public class GridEventBrowser extends BufferedBidiIterator<ILogEvent[], ILogEvent>
implements IEventBrowser
{
	private final GridLogBrowser itsBrowser;
	private final EventCondition itsFilter;
	
	private RIQueryAggregator itsAggregator;
	
	private ILogEvent itsFirstEvent;
	private ILogEvent itsLastEvent;
	
	public GridEventBrowser(GridLogBrowser aBrowser, EventCondition aFilter) throws RemoteException
	{
		itsBrowser = aBrowser;
		itsFilter = aFilter;
		itsAggregator = itsBrowser.getMaster().createAggregator(aFilter);
		reset();
	}
	
	/**
	 * Sets temporal bounds to this browser: no events before the first
	 * event or past the last event will be returned.
	 */
	public void setBounds(ILogEvent aFirstEvent, ILogEvent aLastEvent)
	{
		itsFirstEvent = aFirstEvent;
		itsLastEvent = aLastEvent;
	}
	
	public GridLogBrowser getLogBrowser()
	{
		return itsBrowser;
	}
	
	@Override
	protected ILogEvent[] fetchNextBuffer()
	{
		try
		{
			GridEvent[] theGridEvents = itsAggregator.next(DebuggerGridConfig.QUERY_ITERATOR_BUFFER_SIZE);
			if (theGridEvents == null) return null;
			else return convert(theGridEvents);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected ILogEvent[] fetchPreviousBuffer()
	{
		try
		{
			GridEvent[] theGridEvents = itsAggregator.previous(DebuggerGridConfig.QUERY_ITERATOR_BUFFER_SIZE);
			if (theGridEvents == null) return null;
			else return convert(theGridEvents);
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
	
	@Override
	public boolean hasNext()
	{
		if (super.hasNext())
		{
			if (itsLastEvent == null) return true;
			
			long theFirstT = itsFirstEvent != null ? itsFirstEvent.getTimestamp() : 0;
			long theLastT = itsLastEvent.getTimestamp();
			
			ILogEvent theNext = peekNext();
			long theNextT = theNext.getTimestamp();
			
			if (theNextT < theFirstT) return false;
			else if (theNextT > theLastT) return false;
			else if (theNextT < theLastT) return true;
			else if (theNext.equals(itsLastEvent)) return true;
			else 
			{
				// TODO: There is a border case we don't handle 
				// (all available events have the same timestamp)
				ILogEvent thePrevious = peekPrevious();
				return ! itsLastEvent.equals(thePrevious);
			}
		}
		else return false;
	}
	
	@Override
	public boolean hasPrevious()
	{
		if (super.hasPrevious())
		{
			if (itsFirstEvent == null) return true;
			
			long theFirstT = itsFirstEvent.getTimestamp();
			long theLastT = itsLastEvent != null ? itsLastEvent.getTimestamp() : Long.MAX_VALUE;
			
			ILogEvent thePrevious = peekPrevious();
			long thePreviousT = thePrevious.getTimestamp();
			
			if (thePreviousT > theLastT) return false;
			else if (thePreviousT < theFirstT) return false;
			else if (thePreviousT > theFirstT) return true;
			else if (thePrevious.equals(itsFirstEvent)) return true;
			else 
			{
				// TODO: There is a border case we don't handle 
				// (all available events have the same timestamp)
				ILogEvent theNext = peekNext();
				return ! itsFirstEvent.equals(theNext);
			}
		}
		else return false;
	}
	
	/**
	 * Converts a {@link GridEvent} into an {@link ILogEvent}.
	 */
	private ILogEvent convert(GridEvent aEvent)
	{
		assert itsFilter._match(aEvent);
		return aEvent.toLogEvent(itsBrowser);
	}
	
	private ILogEvent[] convert(GridEvent[] aEvents)
	{
		ILogEvent[] theLogEvents = new ILogEvent[aEvents.length];
		for(int i=0;i<aEvents.length;i++) theLogEvents[i] = convert(aEvents[i]);
		return theLogEvents;
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
			long[] theCounts = itsAggregator.getEventCounts(
								aT1, 
								aT2, 
								aSlotsCount, 
								aForceMergeCounts);
			
			// TODO: take into account first & last events.
			return theCounts;
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public boolean setNextEvent(ILogEvent aEvent)
	{
		try
		{
			boolean theResult = itsAggregator.setNextEvent(
					checkTimestamp(aEvent.getTimestamp()),
					aEvent.getHost().getId(),
					aEvent.getThread().getId());
			reset();
			
			return theResult;
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public boolean setPreviousEvent(ILogEvent aEvent)
	{
		try
		{
			boolean theResult = itsAggregator.setPreviousEvent(
					checkTimestamp(aEvent.getTimestamp()),
					aEvent.getHost().getId(),
					aEvent.getThread().getId());
			reset();
			
			return theResult;
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Corrects the given timestamp if necessary so that
	 * it fits between first and last events.
	 */
	private long checkTimestamp(long aTimestamp)
	{
		if (itsFirstEvent != null)
		{
			long theTimestamp = itsFirstEvent.getTimestamp();
			aTimestamp = Math.max(aTimestamp, theTimestamp);
		}
		if (itsLastEvent != null)
		{
			long theTimestamp = itsLastEvent.getTimestamp();
			aTimestamp = Math.min(aTimestamp, theTimestamp);
		}
		return aTimestamp;
	}
	
	public void setNextTimestamp(long aTimestamp)
	{
		try
		{
			itsAggregator.setNextTimestamp(checkTimestamp(aTimestamp));
			reset();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void setPreviousTimestamp(long aTimestamp)
	{
		try
		{
			itsAggregator.setPreviousTimestamp(checkTimestamp(aTimestamp));
			reset();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public IEventBrowser createIntersection(IEventFilter aFilter)
	{
		GridEventBrowser theBrowser = itsBrowser.createBrowser(itsBrowser.createIntersectionFilter(
				itsFilter,
				aFilter));
		
		theBrowser.setBounds(itsFirstEvent, itsLastEvent);
		
		return theBrowser;
	}
	
	
}
