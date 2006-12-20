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
package tod.impl.dbgrid.dbnode;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import tod.core.config.TODConfig;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.NodeException;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.gridimpl.GridImpl;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;

public abstract class DatabaseNode extends UnicastRemoteObject
implements RIDatabaseNode
{
	
	/**
	 * This command flushes all buffered events and indexes.
	 * args: none
	 * return:
	 *  number of flushed events: int
	 */
	public static final byte CMD_FLUSH = 10;
	
	/**
	 * This command causes the database node to clear its db
	 * arts: none
	 * return:
	 * 	1 (constant): int
	 */
	public static final byte CMD_CLEAR = 11;
	

	
	/**
	 * Id of this node in the system
	 */
	private int itsNodeId;
	
	private RIGridMaster itsMaster;
	private MasterConnection itsMasterConnection;
	
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
	
	/**
	 * Initializes or reinitializes the database.
	 */
	public abstract void clear();
	
	/**
	 * Flushes all buffered data.
	 * @return The number of flushed elements.
	 */
	public abstract int flush();
	
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

	/**
	 * Processes a command sent by the master.
	 * @param aCommand Command id
	 * @throws IOException 
	 */
	protected void processCommand(
			byte aCommand, 
			DataInputStream aInStream, 
			DataOutputStream aOutStream) throws IOException
	{
		switch (aCommand)
		{
		case CMD_FLUSH:
			int theCount = flush();
			aOutStream.writeInt(theCount);
			aOutStream.flush();
			break;
			
		case CMD_CLEAR:
			clear();
			aOutStream.writeInt(1);
			aOutStream.flush();
			break;
			
		default:
			throw new RuntimeException("Not handled: "+aCommand);
				
		}

	}
	
	/**
	 * The socket thread that handles the connection with the grid master.
	 * @author gpothier
	 */
	private class MasterConnection extends Thread
	{
		private final Socket itsSocket;

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
				DataInputStream theInStream = new DataInputStream(new BufferedInputStream(itsSocket.getInputStream()));
				DataOutputStream theOutStream = new DataOutputStream(itsSocket.getOutputStream());
				
				while (itsSocket.isConnected())
				{
					try
					{
						byte theCommand = theInStream.readByte();
						processCommand(theCommand, theInStream, theOutStream);
					}
					catch (EOFException e)
					{
						break;
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
		
	}
	
	public static void main(String[] args)
	{
		GridImpl.getFactory(new TODConfig()).createNode(true);
	}


}
