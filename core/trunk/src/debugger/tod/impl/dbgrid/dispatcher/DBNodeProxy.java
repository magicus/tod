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
package tod.impl.dbgrid.dispatcher;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.SimplePointer;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * A proxy for database nodes. It collects messages in a 
 * buffer and sends them to the actual {@link DatabaseNode}
 * when there are enough, or after a certain time.
 * @author gpothier
 */
public abstract class DBNodeProxy
{
	private final Socket itsSocket;
	private final DataOutputStream itsOutStream;
	private final DataInputStream itsInStream;

	private final int itsNodeId;
	private final GridMaster itsMaster;
	
	private long itsEventsCount = 0;
	private long itsFirstTimestamp = 0;
	private long itsLastTimestamp = 0;

	
	public DBNodeProxy(Socket aSocket, int aNodeId, GridMaster aMaster)
	{
		itsSocket = aSocket;
		itsNodeId = aNodeId;
		itsMaster = aMaster;
		
		try
		{
			itsOutStream = new DataOutputStream(new BufferedOutputStream(aSocket.getOutputStream()));
			itsInStream = new DataInputStream(aSocket.getInputStream());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

	}

	
	public GridMaster getMaster()
	{
		return itsMaster;
	}

	public int getNodeId()
	{
		return itsNodeId;
	}

	protected Socket getSocket()
	{
		return itsSocket;
	}

	protected DataInputStream getInStream()
	{
		return itsInStream;
	}

	protected DataOutputStream getOutStream()
	{
		return itsOutStream;
	}


	/**
	 * Flushes possibly buffered messages, both in this proxy 
	 * and in the node.
	 */
	public abstract void flush();

	/**
	 * Requests the node to clear its database.
	 *
	 */
	public abstract void clear();
	
	/**
	 * Pushes an event so that it will be stored by the node behind this proxy.
	 * @return A simple id of the event (see {@link SimplePointer}).
	 */
	public final long pushEvent(GridEvent aEvent)
	{
		pushEvent0(aEvent);
		long theId = SimplePointer.create(itsEventsCount, getNodeId());
		
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
