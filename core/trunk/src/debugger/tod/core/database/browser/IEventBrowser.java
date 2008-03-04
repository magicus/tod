/*
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
package tod.core.database.browser;

import tod.core.database.event.ILogEvent;
import zz.utils.IPublicCloneable;


/**
 * Permits to navigate in an event list. It is similar to an
 * iterator, in that it maintains an internal cursor and
 * can be quieried for next and previous elements.
 * The cursor can also be repositioned with 
 * {@link #setTimestamp(long)}
 * @author gpothier
 */
public interface IEventBrowser extends IPublicCloneable
{
	/**
	 * Returns the {@link ILogBrowser} that created this event browser.
	 */
	public ILogBrowser getLogBrowser();
	
	/**
	 * Returns the number of events that can be returned by this
	 * browser.
	 */
	public long getEventCount ();
	
	/**
	 * Returns the number of events that occured between the specified timestamps
	 */
	public long getEventCount(long aT1, long aT2, boolean aForceMergeCounts);

	/**
	 * Returns an array of event counts. The timestamp range
	 * defined by the two timestamp parameters is divided into 
	 * slots of equal duration, and the event count for each slot
	 * is returned in the array.
	 * @param aT1 Beginning of timestamp range
	 * @param aT2 End of timestamp range
	 * @param aSlotsCount Number of slots to consider. 
	 */
	public long[] getEventCounts(long aT1, long aT2, int aSlotsCount, boolean aForceMergeCounts);
	
	/**
	 * Sets the cursor so that a call to {@link #next()}
	 * will return the first event available to
	 * this browser that occured not before the specified event.
	 * In the case the specified event is available to this browser,
	 * {@link #next()} will return this event.
	 * @return Returns true if the specified event if available to 
	 * this browser.
	 */
	public boolean setNextEvent (ILogEvent aEvent);
	
	/**
	 * Sets the cursor so that a call to {@link #previous()}
	 * will return the last event available to
	 * this browser that occured not after the specified event.
	 * In the case the specified event is available to this browser,
	 * {@link #previous()} will return this event.
	 * @return Returns true if the specified event if available to
	 * this browser.
	 */
	public boolean setPreviousEvent (ILogEvent aEvent);
	
	/**
	 * Sets the internal cursor of this browser so that the next
	 * element returned by {@link #next()} is the first event
	 * whose timestamp is superior or equal to the specified 
	 * timestamp.
	 */
	public void setNextTimestamp (long aTimestamp);
	
	/**
	 * Sets the internal cursor of this browser so that the next
	 * element returned by {@link #previous()} is the last event
	 * whose timestamp is inferior or equal to the specified 
	 * timestamp.
	 */
	public void setPreviousTimestamp (long aTimestamp);
	
	/**
	 * Indicates if there is a next event.
	 */
	public boolean hasNext ();
	
	/**
	 * Indicates if there is a previous event.
	 */
	public boolean hasPrevious ();
	
	/**
	 * Returns the next event and updates the cursor.
	 */
	public ILogEvent next();

	/**
	 * Returns the previous event and updates the cursor.
	 */
	public ILogEvent previous();
	
	/**
	 * Creates a new event browser that is the intersection of this
	 * browser's filter and the specified filter.
	 */
	public IEventBrowser createIntersection(IEventFilter aFilter);
	
	/**
	 * Returns the timestamp of the first event that can be returned by this browser.
	 */
	public long getFirstTimestamp();
	
	/**
	 * Returns the timestamp of the last event that can be returned by this browser.
	 */
	public long getLastTimestamp();
	
	/**
	 * Returns a clone of this event browser.
	 * The clone has the same set of events, but an independant position.
	 */
	public IEventBrowser clone();
}
