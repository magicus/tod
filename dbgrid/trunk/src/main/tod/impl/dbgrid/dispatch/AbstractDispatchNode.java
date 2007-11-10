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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;

import tod.Util;
import tod.core.config.TODConfig;
import tod.core.transport.LogReceiver;
import tod.impl.database.structure.standard.HostInfo;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.NodeException;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.db.NodeRejectedException;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;
import zz.utils.net.Server.ServerAdress;

/**
 * Base class for all the nodes of the dispatching tree.
 * There are three kinds of nodes:
 * <li>Internal dispatchers, that dispatch events to other 
 * dispatchers.
 * <li>Leaf dispatchers, that dispatch events to database
 * nodes.
 * <li>Database nodes, that index and store events.
 * @author gpothier
 */
public abstract class AbstractDispatchNode extends UnicastRemoteObject 
implements RIDispatchNode
{
	/**
	 * Id of this node in the system
	 */
	private String itsNodeId = "";

	/**
	 * The master to which this node is connected.
	 */
	private RIGridMaster itsMaster;

	/**
	 * This latch permits to wait until this node is connected
	 * to the master.
	 * @see #waitConnectedToMaster()
	 */
	private CountDownLatch itsConnectedLatch = new CountDownLatch(1);

	public AbstractDispatchNode() throws RemoteException
	{
	}

	public TODConfig getConfig()
	{
		try
		{
			return getMaster().getConfig();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the id of this node
	 */
	public String getNodeId()
	{
		return itsNodeId;
	}
	
	public RIGridMaster getMaster()
	{
		return itsMaster;
	}

	/**
	 * Creates a log receiver that is able to communicate directly
	 * with this dispatcher. The grid master requests a log receiver
	 * to its root dispatcher whenever a new client connects.
	 * @param aStartImmediately Whether the receiver should immediately
	 * start its thread. This is for testing only.
	 */
	public abstract LogReceiver createLogReceiver(
			HostInfo aHostInfo, 
			GridMaster aMaster, 
			InputStream aInStream,
			OutputStream aOutStream, 
			boolean aStartImmediately);
	

	public void connectToLocalMaster(GridMaster aMaster, String aId)
	{
		itsMaster = aMaster;
		itsNodeId = aId;
		connectedToMaster();
		itsConnectedLatch.countDown();
	}
	
	/**
	 * Establishes the initial connection between this node and the
	 * {@link GridMaster} through RMI.
	 */
	public void connectToMaster() throws IOException, NotBoundException, AlreadyBoundException
	{
		System.out.println("[AbstractDispatchNode] connectToMaster");
		
		// Setup RMI connection
		Registry theRegistry = LocateRegistry.getRegistry(DebuggerGridConfig.MASTER_HOST, Util.TOD_REGISTRY_PORT);
		itsMaster = (RIGridMaster) theRegistry.lookup(GridMaster.RMI_ID);

		try
		{
			itsNodeId = itsMaster.registerNode(this, InetAddress.getLocalHost().getHostName());
			connectedToMaster();
		}
		catch (NodeRejectedException e)
		{
			System.out.println("Rejected by master: "+e.getMessage());
			System.exit(1);
		}
		
		itsConnectedLatch.countDown();
		
		System.out.println("Master assigned node id "+itsNodeId);
		
		startMonitoringThread();
	}

	/**
	 * This method is called once this node is connected to the
	 * master.
	 */
	protected void connectedToMaster()
	{
	}
	
	/**
	 * Waits until this node is connected to the grid master.
	 */
	protected void waitConnectedToMaster()
	{
		try
		{
			itsConnectedLatch.await();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Connects this node to its parent dispatcher.
	 * Once the connection is established, this method calls
	 * {@link #connectToDispatcher(Socket)} that is responsible
	 * for servicing the connection.
	 */
	public final void connectToDispatcher(ServerAdress aAdress) 
	{
		try
		{
			waitConnectedToMaster();
			
			System.out.println("[AbstractDispatchNode] Connecting to dispatcher at "+aAdress.hostName);
			Socket theSocket = aAdress.connect();
			DataOutputStream theStream = 
				new DataOutputStream(theSocket.getOutputStream());
			
			theStream.writeUTF(getNodeId());
			theStream.flush();
			
			connectToDispatcher(theSocket);
			System.out.println("[AbstractDispatchNode] Connected.");
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Services the connection to the parent dispatcher.
	 */
	protected abstract void connectToDispatcher(Socket aSocket);
	
	
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

	public abstract int flush();
	public abstract void clear();

}