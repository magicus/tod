/*
 * Created on Jul 20, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_EVENT_BUFFER_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_EVENT_PAGE_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_INDEX_PAGE_SIZE;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Comparator;
import java.util.Iterator;

import tod.core.config.GeneralConfig;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridMessage;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.NativeStream;
import zz.utils.SortedRingBuffer;
import zz.utils.Utils;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

public class DatabaseNode extends UnicastRemoteObject
implements RIDatabaseNode
{
	/**
	 * This command pushes a list of events to the node.
	 * args:
	 *  count: int
	 *  events
	 * return: none
	 */
	public static final byte CMD_PUSH_EVENTS = 17;
	
	/**
	 * This command flushes all buffered events.
	 * args: none
	 * return:
	 *  number of flushed events: int
	 */
	public static final byte CMD_FLUSH_EVENTS = 18;
	
	/**
	 * Id of this node in the system
	 */
	private int itsNodeId;
	
	private RIGridMaster itsMaster;
	
	private final HardPagedFile itsEventsFile;
	private final HardPagedFile itsIndexesFile;
	
	private final EventList itsEventList;
	private final Indexes itsIndexes;
	
	/**
	 * Timestamp of the last processed event
	 */
	private long itsLastProcessedTimestamp;	
	private long itsProcessedEventsCount = 0;
	
	private long itsLastAddedTimestamp;
	private long itsAddedEventsCount = 0;
	private GridEvent itsLastAddedEvent;
	
	private SortedRingBuffer<GridEvent> itsEventBuffer = 
		new SortedRingBuffer<GridEvent>(DB_EVENT_BUFFER_SIZE, new EventTimestampComparator());
	
	private MasterConnection itsMasterConnection;
	
	private boolean itsFlushed = false;
	
	public DatabaseNode(boolean aRegisterToMaster) throws RemoteException
	{
		Monitor.getInstance().register(this);
		try
		{
			String thePrefix = GeneralConfig.NODE_DATA_DIR;
			File theParent = new File(thePrefix);
			System.out.println("Using data directory: "+theParent);
			itsEventsFile = new HardPagedFile(new File(theParent, "events.bin"), DB_EVENT_PAGE_SIZE);
			itsIndexesFile = new HardPagedFile(new File(theParent, "indexes.bin"), DB_INDEX_PAGE_SIZE);
			
			itsEventList = new EventList(itsEventsFile);
			itsIndexes = new Indexes(itsIndexesFile);
			
			if (aRegisterToMaster) connectToMaster();
			else itsNodeId = 1;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void connectToMaster() throws IOException, NotBoundException
	{
		// Setup socket connection
		String theMasterHost = GeneralConfig.MASTER_HOST;
		System.out.println("Connecting to "+theMasterHost);
		Socket theSocket = new Socket(theMasterHost, DebuggerGridConfig.MASTER_NODE_PORT);
		DataInputStream theStream = new DataInputStream(theSocket.getInputStream());
		itsNodeId = theStream.readInt();
		System.out.println("Master assigned node id "+itsNodeId);
		
		itsMasterConnection = new MasterConnection(theSocket);
		
		// Setup RMI connection
		Registry theRegistry = LocateRegistry.getRegistry(GeneralConfig.MASTER_HOST);
		itsMaster = (RIGridMaster) theRegistry.lookup(GridMaster.RMI_ID);
		itsMaster.registerNode(this);
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

	public RINodeEventIterator getIterator(EventCondition aCondition) throws RemoteException
	{
		return new NodeEventIterator(this, aCondition);
	}

	public long[] getEventCounts(EventCondition aCondition, long aT1, long aT2, int aSlotsCount) throws RemoteException
	{
		return aCondition.getEventCounts(getIndexes(), aT1, aT2, aSlotsCount);
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
	public int flush()
	{
		int theCount = itsEventBuffer.getSize();
		System.out.println("DatabaseNode: flushing "+theCount+" events...");
		while (! itsEventBuffer.isEmpty()) processEvent(itsEventBuffer.remove());
		itsFlushed = true;
		System.out.println("DatabaseNode: flushed");
		return theCount;
	}
	
	private void addEvent(GridEvent aEvent)
	{
//		System.out.println("AddEvent ts: "+aEvent.getTimestamp());
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastAddedTimestamp)
		{
			System.out.println(String.format(
					"Out of order event: %s(%02d)/%s(%02d) (#%d)",
					theTimestamp,
					aEvent.getThread(),
					itsLastAddedTimestamp,
					itsLastAddedEvent.getThread(),
					itsAddedEventsCount));
		}
		
		itsLastAddedTimestamp = theTimestamp;
		itsLastAddedEvent = aEvent;
		itsAddedEventsCount++;
		
		if (itsEventBuffer.isFull()) processEvent(itsEventBuffer.remove());
		itsEventBuffer.add(aEvent);
	}
	
	private void processEvent(GridEvent aEvent)
	{
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastProcessedTimestamp)
		{
			throw new RuntimeException(
					"Out of order event: "+theTimestamp+"/"+itsLastProcessedTimestamp
					+" (#"+itsProcessedEventsCount+")"
					+" (buffer size: "+itsEventBuffer.getCapacity()+")");
		}
		
		itsLastProcessedTimestamp = theTimestamp;
		itsProcessedEventsCount++;
		
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
		private final int[] itsBuffer = new int[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE];
		private final byte[] itsByteBuffer = new byte[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE*4];
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
				DataInputStream theInStream = new DataInputStream(itsSocket.getInputStream());
				DataOutputStream theOutStream = new DataOutputStream(itsSocket.getOutputStream());
				
				while (itsSocket.isConnected())
				{
					byte theCommand;
					try
					{
						theCommand = theInStream.readByte();
					}
					catch (EOFException e)
					{
						break;
					}
					
					switch (theCommand)
					{
					case CMD_PUSH_EVENTS:
						pushEvents(theInStream);
						break;
						
					case CMD_FLUSH_EVENTS:
						int theCount = flush();
						theOutStream.writeInt(theCount);
						theOutStream.flush();
						return;
						
					default:
						throw new RuntimeException("Not handled: "+theCommand);
							
					}
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			
			if (! itsFlushed) flush();
		}
		
		private void pushEvents(DataInputStream aStream) throws IOException
		{
			int theCount = aStream.readInt();
			
//			System.out.println(String.format(
//			"Received %d messages (already received %d)",
//			theCount,
//			itsReceivedMessages));
			
			Utils.readFully(aStream, itsByteBuffer);
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
}
