/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.core.database.event;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;

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
//		if (aEvent1.getThread() == aEvent2.getThread())
//		{
//			theDelta = aEvent1.getSerial() - aEvent2.getSerial(); 
//		}
//		else 
//		{
			theDelta = aEvent1.getTimestamp() - aEvent2.getTimestamp();
//		}
		
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
		
		public IThreadInfo getThread()
		{
			return null;
		}
		
		public int getDepth()
		{
			return 0;
		}

		public IHostInfo getHost()
		{
			return null;
		}

		public long getTimestamp()
		{
			return itsTimestamp;
		}

		public IBehaviorCallEvent getParent()
		{
			return null;
		}

		public ExternalPointer getParentPointer()
		{
			return null;
		}

		public ExternalPointer getPointer()
		{
			return null;
		}

		public int[] getAdviceCFlow()
		{
			return null;
		}

	}

}