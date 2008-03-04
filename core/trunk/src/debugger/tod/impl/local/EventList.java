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
package tod.impl.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tod.core.database.event.EventComparator;
import tod.impl.common.event.Event;

/**
 * A list of events, ordered by timestamps.
 * @author gpothier
 */
public class EventList
{
	private List<Event> itsEvents = new ArrayList<Event>();
	private long itsFirstTimestamp = Long.MAX_VALUE;
	private long itsLastTimestamp = 0;
	
	public void clear()
	{
		itsEvents.clear();
	}
	
	/**
	 * Adds the specified event to this list.
	 * @return The index at which the event is inserted
	 */
	public int add (Event aEvent)
	{
		int theIndex;
		
		long theTimestamp = aEvent.getTimestamp();
		
		if (size() == 0
			|| getLast().getTimestamp() <= theTimestamp)
		{
			theIndex = size();
		}
		else
		{
			theIndex = Collections.binarySearch(
					itsEvents, 
					aEvent, 
					EventComparator.getInstance());
			
			if (theIndex < 0) theIndex = -theIndex-1;
		}
		
		itsEvents.add (theIndex, aEvent);
		
		if (theTimestamp > 0)
		{
			itsFirstTimestamp = Math.min(itsFirstTimestamp, theTimestamp);
			itsLastTimestamp = Math.max(itsLastTimestamp, theTimestamp);
		}
		
		return theIndex;
	}
	
	public Event getLast()
	{
		return itsEvents.get (itsEvents.size()-1);
	}
	
	public int size()
	{
		return itsEvents.size();
	}

	public Event get (int aIndex)
	{
		return itsEvents.get(aIndex);
	}

	public long getFirstTimestamp()
	{
		return itsFirstTimestamp;
	}

	public long getLastTimestamp()
	{
		return itsLastTimestamp;
	}
	
}
