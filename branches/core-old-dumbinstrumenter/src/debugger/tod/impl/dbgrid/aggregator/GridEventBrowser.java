/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid.aggregator;

import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.impl.dbgrid.queries.EventCondition;

public class GridEventBrowser implements IEventBrowser
{
	private EventCondition itsCondition;

	public int getCursor()
	{
		return 0;
	}

	public ILogEvent getEvent(int aIndex)
	{
		return null;
	}

	public int getEventCount()
	{
		return 0;
	}

	public int getEventCount(long aT1, long aT2)
	{
		return 0;
	}

	public int[] getEventCounts(long aT1, long aT2, int aSlotsCount)
	{
		return null;
	}

	public List<ILogEvent> getEvents(long aT1, long aT2)
	{
		return null;
	}

	public ILogEvent getNext()
	{
		return null;
	}

	public ILogEvent getPrevious()
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

	public void setCursor(int aPosition)
	{
	}

	public void setNextTimestamp(long aTimestamp)
	{
	}

	public void setPreviousTimestamp(long aTimestamp)
	{
	}
}
