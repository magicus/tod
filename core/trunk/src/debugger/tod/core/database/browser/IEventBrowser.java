/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.core.database.browser;

import java.util.List;

import tod.core.database.event.ILogEvent;


/**
 * Permits to navigate in an event list. It is similar to an
 * iterator, in that it maintains an internal cursor and
 * can be quieried for next and previous elements.
 * The cursor can also be repositioned with 
 * {@link #setTimestamp(long)}
 * @author gpothier
 */
public interface IEventBrowser
{
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
	 * Returns a list of all the events that occured between the specified timestamps
	 */
	public List<ILogEvent> getEvents(long aT1, long aT2);
	
//	/**
//	 * Returns the event at  the specified index.
//	 */
//	public ILogEvent getEvent(int aIndex);
	
//	/**
//	 * Returns the current cursor position.
//	 */
//	public int getCursor ();
	
//	/**
//	 * Sets the current cursor position.
//	 */
//	public void setCursor (int aPosition);
	
	/**
	 * Sets the cursor so that a call to {@link #next()}
	 * will return the first event available to
	 * this browser that occured not before the specified event.
	 * In the case the specified event is available to this browser,
	 * {@link #next()} will return this event.
	 */
	public void setNextEvent (ILogEvent aEvent);
	
	/**
	 * Sets the cursor so that a call to {@link #previous()}
	 * will return the last event available to
	 * this browser that occured not after the specified event.
	 * In the case the specified event is available to this browser,
	 * {@link #previous()} will return this event.
	 */
	public void setPreviousEvent (ILogEvent aEvent);
	
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
}
