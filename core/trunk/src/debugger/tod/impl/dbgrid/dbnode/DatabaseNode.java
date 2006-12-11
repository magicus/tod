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
package tod.impl.dbgrid.dbnode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.NodeException;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.messages.GridMessage;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.NativeStream;
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
	 * This command causes the database node to clear its db
	 * arts: none
	 * return:
	 * 	1 (constant): int
	 */
	public static final byte CMD_CLEAR = 19;
	
	/**
	 * Id of this node in the system
	 */
	private int itsNodeId;
	
	private RIGridMaster itsMaster;
	private MasterConnection itsMasterConnection;
	
	private EventDatabase itsCurrentDatabase;
	
	public DatabaseNode(boolean aRegisterToMaster) throws RemoteException
	{
		clear();
		try
		{
			if (aRegisterToMaster) connectToMaster();
			else itsNodeId = 1;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void clear() 
	{
		if (itsCurrentDatabase != null)
		{
			itsCurrentDatabase.unregister();
		}
		
		String thePrefix = DebuggerGridConfig.NODE_DATA_DIR;
		File theParent = new File(thePrefix);
		System.out.println("Using data directory: "+theParent);
		
		File theFile = new File(theParent, "events.bin");
		theFile.delete();
		itsCurrentDatabase = new EventDatabase(theFile);
	}
	
	private void connectToMaster() throws IOException, NotBoundException
	{
		// Setup RMI connection
		Registry theRegistry = LocateRegistry.getRegistry(DebuggerGridConfig.MASTER_HOST);
		itsMaster = (RIGridMaster) theRegistry.lookup(GridMaster.RMI_ID);

		try
		{
			itsNodeId = itsMaster.registerNode(this, InetAddress.getLocalHost().getHostName());
		}
		catch (NodeRejectedException e)
		{
			System.out.println("Rejected by master: "+e.getMessage());
			System.exit(1);
		}
		
		System.out.println("Master assigned node id "+itsNodeId);
		
		startMonitoringThread();
		
		// Setup socket connection
		String theMasterHost = DebuggerGridConfig.MASTER_HOST;
		System.out.println("Connecting to "+theMasterHost);
		Socket theSocket = new Socket(theMasterHost, DebuggerGridConfig.MASTER_NODE_PORT);
		DataOutputStream theStream = new DataOutputStream(theSocket.getOutputStream());
		theStream.writeInt(itsNodeId);
		theStream.flush();
		
		itsMasterConnection = new MasterConnection(theSocket);
	}
	
	private void startMonitoringThread()
	{
		Thread thePrinterThread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					while (true)
					{
						try
						{
							MonitorData theData = Monitor.getInstance().collectData();
							itsMaster.pushMonitorData(getNodeId(), theData);
							sleep(10000);
						}
						catch (InterruptedException e)
						{
							itsMaster.nodeException(new NodeException(getNodeId(), e));
						}
					}
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}
		};
		thePrinterThread.setDaemon(true);
		thePrinterThread.setPriority(Thread.MAX_PRIORITY);
		thePrinterThread.start();
	}

	/**
	 * Returns the id of this node
	 */
	public int getNodeId()
	{
		return itsNodeId;
	}

	public long[] getEventCounts(
			EventCondition aCondition, 
			long aT1, 
			long aT2,
			int aSlotsCount,
			boolean aForceMergeCounts) throws RemoteException
	{
		return itsCurrentDatabase.getEventCounts(
				aCondition, 
				aT1, 
				aT2, 
				aSlotsCount,
				aForceMergeCounts);
	}

	public RINodeEventIterator getIterator(EventCondition aCondition) throws RemoteException
	{
		return itsCurrentDatabase.getIterator(aCondition);
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
						int theCount = itsCurrentDatabase.flush();
						theOutStream.writeInt(theCount);
						theOutStream.flush();
						break;
						
					case CMD_CLEAR:
						clear();
						theOutStream.writeInt(1);
						theOutStream.flush();
						break;
						
					default:
						throw new RuntimeException("Not handled: "+theCommand);
							
					}
				}
				
				System.exit(0);
			}
			catch (Throwable e)
			{
				try
				{
					itsMaster.nodeException(new NodeException(getNodeId(), e));
				}
				catch (RemoteException e1)
				{
					throw new RuntimeException(e1);
				}
				e.printStackTrace();
				System.exit(1);
			}
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
				itsCurrentDatabase.push(theMessage);
			}
		}
	}
	
	public static void main(String[] args) throws RemoteException
	{
		new DatabaseNode(true);
	}


}
