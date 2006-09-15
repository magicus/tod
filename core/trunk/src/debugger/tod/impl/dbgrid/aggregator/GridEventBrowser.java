/*
 * Created on Aug 24, 2006
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
		return getEventCount(0, Long.MAX_VALUE);
	}

	public long getEventCount(long aT1, long aT2)
	{
		return getEventCounts(aT1, aT2, 1)[0];
	}

	public long[] getEventCounts(long aT1, long aT2, int aSlotsCount)
	{
		try
		{
			return itsAggregator.getEventCounts(aT1, aT2, aSlotsCount);
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

	public void setCursor(ILogEvent aEvent)
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
