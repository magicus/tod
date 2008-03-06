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

import reflex.lib.pom.POMSync;
import reflex.lib.pom.POMSyncClass;
import reflex.lib.pom.impl.POMMetaobject;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;
import tod.impl.database.BufferedBidiIterator;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.IScheduled;
import tod.impl.dbgrid.Scheduler;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;

/**
 * Implementation of {@link IEventBrowser} that serves as the client-side
 * of a {@link QueryAggregator}.
 * @author gpothier
 */
@POMSyncClass(
		scheduler = Scheduler.class, 
		group = Scheduler.class,
		syncAll = false)
public class GridEventBrowser extends BufferedBidiIterator<ILogEvent[], ILogEvent>
implements IGridEventBrowser, IScheduled
{
	private final GridLogBrowser itsBrowser;
	private final EventCondition itsFilter;
	
	private final RIQueryAggregator itsAggregator;
	
	private ILogEvent itsFirstEvent;
	private ILogEvent itsLastEvent;
	
	public GridEventBrowser(GridLogBrowser aBrowser, EventCondition aFilter) throws RemoteException
	{
		itsBrowser = aBrowser;
		itsFilter = aFilter;
		itsAggregator = itsBrowser.getMaster().createAggregator(aFilter);
		assert itsAggregator != null;
		reset();
	}
	
	public GridLogBrowser getLogBrowser()
	{
		return itsBrowser;
	}
	
	public IEventFilter getFilter()
	{
		return itsFilter;
	}
	
	public ILogBrowser getKey()
	{
		return itsBrowser;
	}
	
	POMMetaobject getMetaobject()
	{
		return itsBrowser.getMetaobject();
	}
	
	public void setBounds(ILogEvent aFirstEvent, ILogEvent aLastEvent)
	{
		itsFirstEvent = aFirstEvent;
		itsLastEvent = aLastEvent;
	}
	
	@Override
	@POMSync
	protected ILogEvent[] fetchNextBuffer()
	{
		try
		{
			getMetaobject().callTrap(null, "fetchNextBuffer");
			return fetchNextBuffer0();
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	protected ILogEvent[] fetchNextBuffer0()
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
	@POMSync
	protected ILogEvent[] fetchPreviousBuffer()
	{
		try
		{
			getMetaobject().callTrap(null, "fetchPreviousBuffer");
			return fetchPreviousBuffer0();
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	protected ILogEvent[] fetchPreviousBuffer0()
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

	@POMSync
	public long[] getEventCounts(long aT1, long aT2, int aSlotsCount, boolean aForceMergeCounts)
	{
		try
		{
			getMetaobject().callTrap(null, "getEventCounts");
			return getEventCounts0(aT1, aT2, aSlotsCount, aForceMergeCounts);
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	public long[] getEventCounts0(long aT1, long aT2, int aSlotsCount, boolean aForceMergeCounts)
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

	@POMSync
	public boolean setNextEvent(ILogEvent aEvent)
	{
		try
		{
			getMetaobject().callTrap(null, "setNextEvent");
			return setNextEvent0(aEvent);
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	public boolean setNextEvent0(ILogEvent aEvent)
	{
		try
		{
			boolean theResult = itsAggregator.setNextEvent(
					checkTimestamp(aEvent.getTimestamp()),
					aEvent.getThread().getId());
			reset();
			
			return theResult;
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	@POMSync
	public boolean setPreviousEvent(ILogEvent aEvent)
	{
		try
		{
			getMetaobject().callTrap(null, "setPreviousEvent");
			return setPreviousEvent0(aEvent);
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	public boolean setPreviousEvent0(ILogEvent aEvent)
	{
		try
		{
			long theTimestamp = checkTimestamp(aEvent.getTimestamp());
			IThreadInfo theThread = aEvent.getThread();
			int theThreadId = theThread.getId();
			boolean theResult = itsAggregator.setPreviousEvent(theTimestamp, theThreadId);
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
	
	@POMSync
	public void setNextTimestamp(long aTimestamp)
	{
		try
		{
			getMetaobject().callTrap(null, "setNextTimestamp");
			setNextTimestamp0(aTimestamp);
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	public void setNextTimestamp0(long aTimestamp)
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

	@POMSync
	public void setPreviousTimestamp(long aTimestamp)
	{
		try
		{
			getMetaobject().callTrap(null, "setPreviousTimestamp");
			setPreviousTimestamp0(aTimestamp);
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	public void setPreviousTimestamp0(long aTimestamp)
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
	
	public IEventBrowser clone()
	{
		return itsBrowser.createBrowser(itsFilter);
	}

	public IEventBrowser createIntersection(IEventFilter aFilter)
	{
		IGridEventBrowser theBrowser = itsBrowser.createBrowser(itsBrowser.createIntersectionFilter(
				itsFilter,
				aFilter));
		
		theBrowser.setBounds(itsFirstEvent, itsLastEvent);
		
		return theBrowser;
	}

	public long getFirstTimestamp()
	{
		return itsFirstEvent != null ?
				itsFirstEvent.getTimestamp()
				: getFirstTimestamp(clone());
	}

	public long getLastTimestamp()
	{
		return itsLastEvent != null ?
				itsLastEvent.getTimestamp()
				: getLastTimestamp(clone());
	}
	
	/**
	 * Returns the timestamp of the first event available to the 
	 * given browser, or 0 if there is no event. 
	 */
	private static long getFirstTimestamp(IEventBrowser aBrowser)
	{
		aBrowser.setNextTimestamp(0);
		if (aBrowser.hasNext())
		{
			return aBrowser.next().getTimestamp();
		}
		else return 0;
	}
	
	/**
	 * Returns the timestamp of the last event available to the 
	 * given browser, or 0 if there is no event. 
	 */
	private static long getLastTimestamp(IEventBrowser aBrowser)
	{
		aBrowser.setPreviousTimestamp(Long.MAX_VALUE);
		if (aBrowser.hasPrevious())
		{
			return aBrowser.previous().getTimestamp();
		}
		else return 0;
	}


	
	
}
