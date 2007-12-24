/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

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
package tod.core.database.event;

import tod.core.database.browser.IEventBrowser;

/**
 * An event that contains other events.
 * @author gpothier
 */
public interface IParentEvent extends ILogEvent
{
	/**
	 * Returns an {@link IEventBrowser} that permits to retrieve the
	 * children of this event.
	 */
	public IEventBrowser getChildrenBrowser();
	
	/**
	 * Indicates if this node has children apart from the exit event.
	 */
	public boolean hasRealChildren();

	
	/**
	 * Indicates if this event is the direct parent of its children.
	 * A call to an instrumented behavior from another instrumented
	 * behavior results in a direct parent event: children events are
	 * those that occur during the execution of the called behavior.
	 * <br/>
	 * On the other hand, call to a non-instrumented behavior from
	 * an instrumented behavior results in an indirect parent event:
	 * the non-instrumented behavior will not generate any event, but it
	 * might call an instrumented behavior; the events generated by the latter
	 * will appear as children of the parent event, although they occured
	 * deeper in the control flow.
	 */
	public boolean isDirectParent();

	/**
	 * Returns the timestamp of the first event of the set of events
	 * comprised of this container and its contained events.
	 */
	public long getFirstTimestamp();
	
	/**
	 * Returns the timestamp of the last event of the set of events
	 * comprised of this container and its contained events.
	 */
	public long getLastTimestamp();
}
