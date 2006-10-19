/*
 * Created on Jul 21, 2006
 */
package tod.impl.dbgrid.dispatcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import tod.impl.common.event.Event;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.ExternalPointer;
import tod.impl.dbgrid.GridEventCollector;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridMessage;
import tod.utils.NativeStream;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

/**
 * A proxy for database nodes. It collects messages in a 
 * buffer and sends them to the actual {@link DatabaseNode}
 * when there are enough, or after a certain time.
 * @author gpothier
 */
public class DBNodeProxy
{
	private static final int TRANSMIT_DELAY_MS = 1000;
	
	private final Socket itsSocket;
	private final DataOutputStream itsOutStream;
	private final DataInputStream itsInStream;
	private final int itsNodeId;
	private final GridMaster itsMaster;
	
	private final int[] itsBuffer = new int[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE];
	private final byte[] itsByteBuffer = new byte[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE*4];
	private final BitStruct itsEventsBuffer = new IntBitStruct(itsBuffer);
	
	private long itsSentMessagesCount = 0;
	private long itsEventsCount = 0;
	private long itsFirstTimestamp = 0;
	private long itsLastTimestamp = 0;
	
	/**
	 * Number of currently buffered messages.
	 */
	private int itsMessagesCount = 0;
	
	public DBNodeProxy(Socket aSocket, int aNodeId, GridMaster aMaster)
	{
		itsSocket = aSocket;
		itsNodeId = aNodeId;
		itsMaster = aMaster;
		
		try
		{
			itsOutStream = new DataOutputStream(itsSocket.getOutputStream());
			itsInStream = new DataInputStream(itsSocket.getInputStream());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Pushes an event so that it will be stored by the node behind this proxy.
	 */
	public void pushEvent(GridEvent aEvent)
	{
		pushMessage(aEvent);
		
		itsEventsCount++;
		long theTimestamp = aEvent.getTimestamp();
		
		// The following code is a bit faster than using min & max
		// (Pentium M 2ghz)
		if (itsFirstTimestamp == 0) itsFirstTimestamp = theTimestamp;
		if (itsLastTimestamp < theTimestamp) itsLastTimestamp = theTimestamp;
	}

	private void pushMessage(GridMessage aMessage)
	{
		if (aMessage.getBitCount() > itsEventsBuffer.getRemainingBits())
		{
			sendBuffer();
		}
		
		aMessage.writeTo(itsEventsBuffer);
		itsMessagesCount++;
	}
	
	private void sendBuffer()
	{
//		System.out.println(String.format(
//				"Sending %d messages to node %d (already sent %d)",
//				itsMessagesCount,
//				itsNodeId,
//				itsSentMessagesCount));
		try
		{
			itsOutStream.writeByte(DatabaseNode.CMD_PUSH_EVENTS);
			itsOutStream.writeInt(itsMessagesCount);
			
			NativeStream.i2b(itsBuffer, itsByteBuffer);
			
			itsOutStream.write(itsByteBuffer);
//			itsOutputStream.flush();
			
			itsSentMessagesCount += itsMessagesCount;
			
			itsEventsBuffer.reset();
			itsMessagesCount = 0;
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
			sendBuffer();
			
			itsOutStream.writeByte(DatabaseNode.CMD_FLUSH_EVENTS);
			itsOutStream.flush();
			
			int theCount = itsInStream.readInt();
			System.out.println("DBNodeProxy: database node flushed "+theCount+" events.");
			itsSocket.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
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
