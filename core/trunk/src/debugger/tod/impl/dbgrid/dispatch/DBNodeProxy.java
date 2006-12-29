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
package tod.impl.dbgrid.dispatch;

import java.net.Socket;

import tod.impl.dbgrid.SimplePointer;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * A proxy for database nodes. It collects messages in a 
 * buffer and sends them to the actual {@link DatabaseNode}
 * when there are enough, or after a certain time.
 * @author gpothier
 */
public abstract class DBNodeProxy extends DispatchNodeProxy
{
	private long itsEventsCount = 0;
	private long itsFirstTimestamp = 0;
	private long itsLastTimestamp = 0;

	private int itsNodeIndex;
	
	public DBNodeProxy(RIDispatchNode aConnectable, Socket aSocket, String aNodeId)
	{
		super (aConnectable, aSocket, aNodeId);
		itsNodeIndex = Integer.parseInt(aNodeId.substring(3));
	}
	
	/**
	 * Pushes an event so that it will be stored by the node behind this proxy.
	 * @return A simple id of the event (see {@link SimplePointer}).
	 */
	public final long pushEvent(GridEvent aEvent)
	{
		pushEvent0(aEvent);
		long theId = SimplePointer.create(itsEventsCount, itsNodeIndex);
		
		itsEventsCount++;
		long theTimestamp = aEvent.getTimestamp();
		
		// The following code is a bit faster than using min & max
		// (Pentium M 2ghz)
		if (itsFirstTimestamp == 0) itsFirstTimestamp = theTimestamp;
		if (itsLastTimestamp < theTimestamp) itsLastTimestamp = theTimestamp;
		
		return theId;
	}

	protected abstract void pushEvent0(GridEvent aEvent);

	
	/**
	 * Returns the number of events stored by this node
	 */
	public long getEventsCount() 
	{
		return itsEventsCount;
	}
	
	/**
	 * Returns the timestamp of the first event recorded in this node.
	 */
	public long getFirstTimestamp()
	{
		return itsFirstTimestamp;
	}
	
	/**
	 * Returns the timestamp of the last event recorded in this node.
	 */
	public long getLastTimestamp()
	{
		return itsLastTimestamp;
	}


}
