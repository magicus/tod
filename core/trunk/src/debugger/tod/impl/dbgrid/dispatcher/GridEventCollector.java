/*
 * Created on Jul 20, 2006
 */
package tod.impl.dbgrid.dispatcher;

import tod.core.model.structure.IHostInfo;
import tod.impl.common.EventCollector;
import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.Event;
import tod.impl.dbgrid.DebuggerGridConfig;

/**
 * Event collector for the grid database backend. It handles events from a single
 * hosts, preprocesses them and sends them to the {@link EventDispatcher}.
 * @author gpothier
 */
public class GridEventCollector extends EventCollector
{
	/**
	 * A counter used to generate sequential thread numbers.
	 * This permits to reduce the number of bits used to represent thread ids,
	 * as all 64 bits of original thread ids might be used.
	 */
	private static int itsLastThreadNumber = 0;
	
	private EventDispatcher itsDispatcher;
	
	public GridEventCollector(IHostInfo aHost, EventDispatcher aDispatcher)
	{
		super(aHost);
		itsDispatcher = aDispatcher;
	}

	@Override
	protected synchronized DefaultThreadInfo createThreadInfo(long aId, BehaviorCallEvent aRootEvent)
	{
		return new MyThreadInfo(aId, aRootEvent, itsLastThreadNumber++);
	}
	
	@Override
	protected void processEvent(DefaultThreadInfo aThread, Event aEvent)
	{
		MyThreadInfo theThread = (MyThreadInfo) aThread;
		theThread.adjustTimestamp(aEvent);
		
		itsDispatcher.dispatchEvent(aEvent);
	}
	
	/**
	 * Returns the thread number of the given event.
	 */
	public static int getThreadNumber(Event aEvent)
	{
		return ((MyThreadInfo) aEvent.getThread()).getThreadNumber();
	}
	
	private static class MyThreadInfo extends DefaultThreadInfo
	{
		private final int itsThreadNumber;
		private long itsLastTimestamp = -1;
		
		public MyThreadInfo(long aId, BehaviorCallEvent aRootEvent, int aThreadNumber)
		{
			super(aId, aRootEvent);
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
