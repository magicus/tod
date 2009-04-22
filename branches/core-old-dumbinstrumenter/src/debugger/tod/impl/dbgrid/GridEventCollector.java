/*
 * Created on Jul 20, 2006
 */
package tod.impl.dbgrid;

import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.IHostInfo;
import tod.impl.common.EventCollector;
import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.Event;
import tod.impl.dbgrid.dispatcher.EventDispatcher;

/**
 * Event collector for the grid database backend. It handles events from a single
 * hosts, preprocesses them and sends them to the {@link EventDispatcher}.
 * @author gpothier
 */
public class GridEventCollector extends EventCollector
{
	private final GridMaster itsMaster;
	
	private EventDispatcher itsDispatcher;
	
	/**
	 * A counter used to generate sequential thread numbers.
	 * This permits to reduce the number of bits used to represent thread ids,
	 * as all 64 bits of original thread ids might be used.
	 * Thread numbers are unique for one host but might
	 * overlap for different hosts.
	 */
	private int itsLastThreadNumber = 1;

	
	public GridEventCollector(
			GridMaster aMaster,
			IHostInfo aHost,
			ILocationsRepository aLocationsRepository,
			EventDispatcher aDispatcher)
	{
		super(aHost, aLocationsRepository);
		itsMaster = aMaster;
		itsDispatcher = aDispatcher;
	}

	@Override
	protected synchronized DefaultThreadInfo createThreadInfo(
			long aId, 
			BehaviorCallEvent aRootEvent)
	{
		int theThreadId = itsLastThreadNumber++;
		return new GridThreadInfo(getHost(), aId, aRootEvent, theThreadId);
	}
	
	@Override
	protected void processEvent(DefaultThreadInfo aThread, Event aEvent)
	{
		GridThreadInfo theThread = (GridThreadInfo) aThread;
		theThread.adjustTimestamp(aEvent);
		
		itsDispatcher.dispatchEvent(aEvent);
	}
	
	/**
	 * Returns the thread number of the given event.
	 */
	public static int getThreadNumber(Event aEvent)
	{
		return ((GridThreadInfo) aEvent.getThread()).getThreadNumber();
	}
	
	@Override
	public GridThreadInfo getThread(long aId)
	{
		return (GridThreadInfo) super.getThread(aId);
	}
	
	public static class GridThreadInfo extends DefaultThreadInfo
	{
		private final int itsThreadNumber;
		private long itsLastTimestamp = -1;
		
		public GridThreadInfo(
				IHostInfo aHost,
				long aId, 
				BehaviorCallEvent aRootEvent,
				int aThreadNumber)
		{
			super(aHost, aId, aRootEvent);
			itsThreadNumber = aThreadNumber;
		}

		/**
		 * Transforms the timestamp of the provided event so that no two events
		 * of the same thread share the same timestamp.
		 */
		public void adjustTimestamp(Event aEvent)
		{
			long theTimestamp = (aEvent.getTimestamp() << DebuggerGridConfig.TIMESTAMP_ADJUST_SHIFT) & ~DebuggerGridConfig.TIMESTAMP_ADJUST_MASK;
			if ((itsLastTimestamp & ~DebuggerGridConfig.TIMESTAMP_ADJUST_MASK) == theTimestamp)
			{
				// Original timestamps overlap.
				if ((itsLastTimestamp & DebuggerGridConfig.TIMESTAMP_ADJUST_MASK) == DebuggerGridConfig.TIMESTAMP_ADJUST_MASK)
				{
					throw new RuntimeException("Timestamp adjust overflow");
				}
				
				theTimestamp = itsLastTimestamp+1;
			}
			
			aEvent.setTimestamp(theTimestamp);
			itsLastTimestamp = theTimestamp;
		}

		public int getThreadNumber()
		{
			return itsThreadNumber;
		}
	}
	
}
