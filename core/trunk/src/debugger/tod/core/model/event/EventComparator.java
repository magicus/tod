package tod.core.model.event;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tod.core.model.structure.ThreadInfo;

/**
 * Compares events based on their timestamp.
 */
public class EventComparator implements Comparator<ILogEvent>
{
	private static EventComparator INSTANCE = new EventComparator();

	public static EventComparator getInstance()
	{
		return INSTANCE;
	}

	private EventComparator()
	{
	}
	
	public int compare(ILogEvent aEvent1, ILogEvent aEvent2)
	{
		long theDelta;
		if (aEvent1.getThread() == aEvent2.getThread())
		{
			theDelta = aEvent1.getSerial() - aEvent2.getSerial(); 
		}
		else 
		{
			theDelta = aEvent1.getTimestamp() - aEvent2.getTimestamp();
		}
		
		if (theDelta > 0) return 1;
		else if (theDelta < 0) return -1;
		else return 0;
	}
	
	/**
	 * Retrieves the index (or insertion point) of the event
	 * with the specified timestamp
	 */
	public static int indexOf (long aTimestamp, List<ILogEvent> aEvents)
	{
		int theIndex = Collections.binarySearch(
				aEvents, 
				new DummyEvent(aTimestamp), 
				getInstance());
		return theIndex >= 0 ? theIndex : -theIndex-1;
	}

	/**
	 * Retrieves the index (or insertion point) of the specified event
	 */
	public static int indexOf (ILogEvent aEvent, List<ILogEvent> aEvents)
	{
		int theIndex = Collections.binarySearch(
				aEvents, 
				aEvent, 
				getInstance());
		
		theIndex = theIndex >= 0 ? theIndex : -theIndex-1;
		
		int theSize = aEvents.size();
		int theResult = theIndex;
		
		while (theResult < theSize)
		{
			ILogEvent theEvent = aEvents.get (theResult);
			if (theEvent == aEvent) break;
			else if (theEvent.getTimestamp() > aEvent.getTimestamp()) 
			{
				theResult = Math.max (theIndex, theResult-1);
				break;
			}
			
			theResult++;
		}
		return theResult;
	}


	/**
	 * This event only serves to retrieve the index for a timestamp.
	 */
	public static class DummyEvent implements ILogEvent
	{
		private final long itsTimestamp;

		public DummyEvent(long aTimestamp)
		{
			itsTimestamp = aTimestamp;
		}
		
		public ThreadInfo getThread()
		{
			return null;
		}
		
		public long getTimestamp()
		{
			return itsTimestamp;
		}

		public long getSerial()
		{
			return 0;
		}

		public IBehaviorEnterEvent getFather()
		{
			return null;
		}
	}

}