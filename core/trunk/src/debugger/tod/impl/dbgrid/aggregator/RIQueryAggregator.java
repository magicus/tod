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
package tod.impl.dbgrid.aggregator;

import java.rmi.RemoteException;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.impl.dbgrid.db.RIEventIterator;

/**
 * Remote interface for a {@link QueryAggregator}.
 * @author gpothier
 */
public interface RIQueryAggregator extends RIEventIterator
{
	/**
	 * Semantics matches {@link IEventBrowser#setNextEvent(ILogEvent)}
	 */
	public boolean setNextEvent (long aTimestamp, int aHostId, int aThreadId) throws RemoteException;
	
	/**
	 * Semantics matches {@link IEventBrowser#setPreviousEvent(ILogEvent)}
	 */
	public boolean setPreviousEvent (long aTimestamp, int aHostId, int aThreadId) throws RemoteException;

	
	/**
	 * Semantic matches {@link IEventBrowser#getEventCounts(long, long, int)}
	 */
	public long[] getEventCounts(
			long aT1, 
			long aT2, 
			int aSlotsCount,
			boolean aForceMergeCounts) throws RemoteException;

}
