/*
 * Created on Jul 20, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_CFLOW_PAGE_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_EVENT_BUFFER_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_EVENT_PAGE_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_INDEX_PAGE_SIZE;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.Iterator;

import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridMessage;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.ConfigUtils;
import tod.utils.NativeStream;
import zz.utils.SortedRingBuffer;
import zz.utils.Utils;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

public class DatabaseNode
{
	/**
	 * Id of this node in the system
	 */
	private final int itsNodeId;
	
	private final HardPagedFile itsEventsFile;
	private final HardPagedFile itsIndexesFile;
	private final HardPagedFile itsCFlowDataFile;
	
	private final EventList itsEventList;
	private final Indexes itsIndexes;
	
	/**
	 * Timestamp of the last processed event
	 */
	private long itsLastProcessedTimestamp;
	
	private SortedRingBuffer<GridEvent> itsEventBuffer = 
		new SortedRingBuffer<GridEvent>(DB_EVENT_BUFFER_SIZE, new EventTimestampComparator());
	
	private MasterConnection itsMasterConnection;
	
	private boolean itsFlushed = false;
	
	public DatabaseNode(boolean aRegisterToMaster)
	{
		Monitor.getInstance().register(this);
		try
		{
			String thePrefix = ConfigUtils.readString("node-data-dir", ".");
			File theParent = new File(thePrefix);
			itsEventsFile = new HardPagedFile(new File(theParent, "events.bin"), DB_EVENT_PAGE_SIZE);
			itsIndexesFile = new HardPagedFile(new File(theParent, "indexes.bin"), DB_INDEX_PAGE_SIZE);
			itsCFlowDataFile = new HardPagedFile(new File(theParent, "cflow.bin"), DB_CFLOW_PAGE_SIZE);
			
			itsEventList = new EventList(itsEventsFile);
			itsIndexes = new Indexes(itsIndexesFile);
			
			if (aRegisterToMaster) itsNodeId = connectToMaster();
			else itsNodeId = 1;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private int connectToMaster() throws IOException
	{
		String theMasterHost = ConfigUtils.readString("master-host", "localhost");
		Socket theSocket = new Socket(theMasterHost, DebuggerGridConfig.MASTER_NODE_PORT);
		DataInputStream theStream = new DataInputStream(theSocket.getInputStream());
		int theNodeId = theStream.readInt();
		
		itsMasterConnection = new MasterConnection(theSocket);
		
		return theNodeId;
	}

	/**
	 * Returns the id of this node
	 */
	public int getNodeId()
	{
		return itsNodeId;
	}

	public Indexes getIndexes()
	{
		return itsIndexes;
	}
	
	
	/**
	 * Creates an iterator over matching events of this node, starting at the specified timestamp.
	 */
	public Iterator<GridEvent> evaluate(EventCondition aCondition, long aTimestamp)
	{
		return aCondition.createIterator(itsEventList, getIndexes(), aTimestamp);
	}

	/**
	 * Pushes a single message to this node.
	 * Messages can be events or parent/child
	 * relations.
	 */
	public void push(GridMessage aMessage)
	{
		assert ! itsFlushed;
		
		if (aMessage instanceof GridEvent)
		{
			GridEvent theEvent = (GridEvent) aMessage;
			addEvent(theEvent);
		}
		else throw new RuntimeException("Not handled: "+aMessage);
	}
	
	/**
	 * Flushes the event buffer. Events should not be added
	 * after this method is called.
	 */
	public void flush()
	{
		while (! itsEventBuffer.isEmpty()) processEvent(itsEventBuffer.remove());
		itsFlushed = true;
	}
	
	private void addEvent(GridEvent aEvent)
	{
		if (itsEventBuffer.isFull()) processEvent(itsEventBuffer.remove());
		itsEventBuffer.add(aEvent);
	}
	
	private void processEvent(GridEvent aEvent)
	{
		if (aEvent.getTimestamp() < itsLastProcessedTimestamp)
		{
			throw new RuntimeException("Out of order events");
		}
		itsLastProcessedTimestamp = aEvent.getTimestamp();
		
		long theId = itsEventList.add(aEvent);
		aEvent.index(itsIndexes, theId);		
	}
	
	/**
	 * Returns the amount of disk storage used by this node.
	 */
	public long getStorageSpace()
	{
		return itsEventsFile.getStorageSpace() + itsIndexesFile.getStorageSpace();
	}
	
	public long getEventsCount()
	{
		return itsEventList.getEventsCount();
	}

	
	private static class EventTimestampComparator implements Comparator<GridEvent>
	{
		public int compare(GridEvent aEvent1, GridEvent aEvent2)
		{
			long theDelta = aEvent1.getTimestamp() - aEvent2.getTimestamp();
			if (theDelta == 0) return 0;
			else if (theDelta > 0) return 1;
			else return -1;
		}
	}
	
	public static void main(String[] args) throws RemoteException
	{
		new DatabaseNode(true);
	}

	/**
	 * The socket thread that handles the connection with the grid master.
	 * @author gpothier
	 */
	private class MasterConnection extends Thread
	{
		private final int[] itsBuffer = new int[DebuggerGridConfig.MASTER_BUFFER_SIZE];
		private final byte[] itsByteBuffer = new byte[DebuggerGridConfig.MASTER_BUFFER_SIZE*4];
		private final BitStruct itsStruct = new IntBitStruct(itsBuffer);
		private final Socket itsSocket;
		private long itsReceivedMessages = 0;

		public MasterConnection(Socket aSocket)
		{
			itsSocket = aSocket;
			start();
		}
		
		@Override
		public void run()
		{
			try
			{
				DataInputStream theStream = new DataInputStream(itsSocket.getInputStream());
				
				while (itsSocket.isConnected())
				{
					int theCount;
					try
					{
						theCount = theStream.readInt();
					}
					catch (EOFException e)
					{
						break;
					}
					
					int theMagic = theStream.readInt();
					if (theMagic != 0xabcdef) throw new RuntimeException("Bad magic number!");
//					System.out.println(String.format(
//							"Received %d messages (already received %d)",
//							theCount,
//							itsReceivedMessages));
					
					Utils.readFully(theStream, itsByteBuffer);
					NativeStream.b2i(itsByteBuffer, itsBuffer);
					itsStruct.reset();
					
					itsReceivedMessages += theCount;

					for (int i=0;i<theCount;i++)
					{
						GridMessage theMessage = GridMessage.read(itsStruct);
						push(theMessage);
					}
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			
			flush();
		}
	}
}
