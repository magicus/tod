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
package tod.impl.dbgrid.gridimpl.uniform;

import java.io.IOException;
import java.net.Socket;

import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dispatcher.DBNodeProxy;
import tod.impl.dbgrid.messages.GridEvent;
import tod.utils.NativeStream;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

/**
 * A proxy for database nodes. It collects messages in a 
 * buffer and sends them to the actual {@link DatabaseNode}
 * when there are enough, or after a certain time.
 * @author gpothier
 */
public class UniformDBNodeProxy extends DBNodeProxy
{
	
	private final int[] itsEventBuffer = new int[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE];
	private final byte[] itsEventByteBuffer = new byte[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE*4];
	private final BitStruct itsEventBufferStruct = new IntBitStruct(itsEventBuffer);
	
	private long itsSentEventsCount = 0;
	
	/**
	 * Number of currently buffered messages.
	 */
	private int itsBufferedEventsCount = 0;
	
	public UniformDBNodeProxy(Socket aSocket, int aNodeId)
	{
		super(aSocket, aNodeId);
	}

	@Override
	protected void pushEvent0(GridEvent aEvent)
	{
		if (aEvent.getBitCount() > itsEventBufferStruct.getRemainingBits())
		{
			sendEventBuffer();
		}
		
		aEvent.writeTo(itsEventBufferStruct);
		itsBufferedEventsCount++;
	}
	
	protected void sendEventBuffer()
	{
//		System.out.println(String.format(
//				"Sending %d messages to node %d (already sent %d)",
//				itsMessagesCount,
//				itsNodeId,
//				itsSentMessagesCount));
		try
		{
			getOutStream().writeByte(UniformDatabaseNode.CMD_PUSH_EVENTS);
			getOutStream().writeInt(itsBufferedEventsCount);
			
			NativeStream.i2b(itsEventBuffer, itsEventByteBuffer);
			
			getOutStream().write(itsEventByteBuffer);
//			itsOutputStream.flush();
			
			itsSentEventsCount += itsBufferedEventsCount;
			
			itsEventBufferStruct.reset();
			itsBufferedEventsCount = 0;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void flush()
	{
		try
		{
			sendEventBuffer();
			
			getOutStream().writeByte(UniformDatabaseNode.CMD_FLUSH);
			getOutStream().flush();
			
			int theCount = getInStream().readInt();
			System.out.println("DBNodeProxy: database node flushed "+theCount+" events.");
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void clear()
	{
		try
		{
			sendEventBuffer();
			
			getOutStream().writeByte(UniformDatabaseNode.CMD_CLEAR);
			getOutStream().flush();
			
			int theResult = getInStream().readInt();
			assert theResult == 1;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
}
