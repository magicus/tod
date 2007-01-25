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
package tod.impl.dbgrid.gridimpl;

import java.rmi.RemoteException;

import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.db.RINodeEventIterator;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;

public abstract class AbstractEventDatabase
{
	
	/**
	 * Causes this database to recursively unregister from the monitor.
	 */
	public abstract void unregister();

	/**
	 * Flushes the event buffer. Events should not be added
	 * after this method is called.
	 */
	public abstract int flush();

	/**
	 * Creates an iterator over matching events of this node, starting at the specified timestamp.
	 */
	public abstract BidiIterator<GridEvent> evaluate(EventCondition aCondition, long aTimestamp);

	public abstract RINodeEventIterator getIterator(EventCondition aCondition) throws RemoteException;

	public abstract long[] getEventCounts(
			EventCondition aCondition,
			long aT1, 
			long aT2,
			int aSlotsCount, 
			boolean aForceMergeCounts) throws RemoteException;

	/**
	 * Pushes a single message to this node.
	 * Messages can be events or parent/child
	 * relations.
	 */
	public abstract void push(GridEvent aEvent);

}
