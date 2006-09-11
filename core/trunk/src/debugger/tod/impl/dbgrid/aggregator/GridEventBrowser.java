/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid.aggregator;

import java.rmi.RemoteException;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.queries.EventCondition;

public class GridEventBrowser implements IEventBrowser
{
	private RIQueryAggregator itsAggregator;

	public GridEventBrowser(RIGridMaster aMaster, EventCondition aCondition) throws RemoteException
	{
		itsAggregator = aMaster.createAggregator(aCondition);
	}

	public int getEventCount()
	{
		throw new UnsupportedOperationException();
	}

	public int getEventCount(long aT1, long aT2)
	{
		throw new UnsupportedOperationException();
	}

	public int[] getEventCounts(long aT1, long aT2, int aSlotsCount)
	{
		throw new UnsupportedOperationException();
	}

	public List<ILogEvent> getEvents(long aT1, long aT2)
	{
		throw new UnsupportedOperationException();
	}

	public ILogEvent next()
	{
		return null;
	}

	public ILogEvent previous()
	{
		return null;
	}

	public boolean hasNext()
	{
		return false;
	}

	public boolean hasPrevious()
	{
		return false;
	}

	public void setCursor(ILogEvent aEvent)
	{
	}

	public void setNextTimestamp(long aTimestamp)
	{
	}

	public void setPreviousTimestamp(long aTimestamp)
	{
	}
}
