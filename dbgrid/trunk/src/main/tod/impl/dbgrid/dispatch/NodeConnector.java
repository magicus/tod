/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid.dispatch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;

import tod.Util;
import tod.core.ILogCollector;
import tod.core.config.TODConfig;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.transport.HighLevelEventReader;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.IGridEventFilter;
import tod.impl.dbgrid.NodeException;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.db.DatabaseNode;
import tod.impl.dbgrid.db.NodeRejectedException;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.db.RINodeEventIterator;
import tod.tools.monitoring.MonitoringServer;
import tod.tools.monitoring.RIMonitoringServer;
import tod.tools.monitoring.RIMonitoringServerProvider;
import zz.utils.monitoring.Monitor;
import zz.utils.monitoring.Monitor.MonitorData;
import zz.utils.net.Server.ServerAdress;

/**
 * Handles the connection between a {@link DatabaseNode} and the {@link GridMaster}
 * (on the node's side).
 * @author gpothier
 */
public class NodeConnector extends UnicastRemoteObject 
implements RINodeConnector
{
	private final DatabaseNode itsDatabaseNode;
	
	/**
	 * Id of this node in the system
	 */
	private int itsNodeId = -1;

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

	public NodeConnector(DatabaseNode aDatabaseNode) throws RemoteException
	{
		itsDatabaseNode = aDatabaseNode;
	}

	public RIMonitoringServer getMonitoringServer()
	{
		return MonitoringServer.get();
	}



	public void setConfig(TODConfig aConfig) 
	{
		itsDatabaseNode.setConfig(aConfig);
	}


	public RIGridMaster getMaster()
	{
		return itsMaster;
	}

	/**
	 * Establishes the initial connection between this node and the
	 * {@link GridMaster} through RMI.
	 */
	public void connectToMaster() throws IOException, NotBoundException
	{
		System.out.println("[NodeConnector] connectToMaster");
		
		// Setup RMI connection
		Registry theRegistry = LocateRegistry.getRegistry(DebuggerGridConfig.MASTER_HOST, Util.TOD_REGISTRY_PORT);
		
		// We use a temp config because we cannot yet obtain the config from the master
		itsMaster = (RIGridMaster) theRegistry.lookup(GridMaster.getRMIId(new TODConfig()));

		try
		{
			itsNodeId = getMaster().registerNode(this, InetAddress.getLocalHost().getHostName());
			itsDatabaseNode.connectedToMaster(itsMaster, itsNodeId);
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
	 * Waits until this node is connected to the grid master.
	 */
	public void waitConnectedToMaster()
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
	
	public int getNodeId() 
	{
		return itsNodeId;
	}
	
	public void connectEventStream(ServerAdress aAdress, IHostInfo aHostInfo) 
	{
		try
		{
			waitConnectedToMaster();
			
			System.out.println("[NodeConnector] Connecting event stream to: "+aAdress.hostName);
			Socket theSocket = aAdress.connect();
			DataOutputStream theStream = 
				new DataOutputStream(theSocket.getOutputStream());
			
			DataInputStream theInStream = 
				new DataInputStream(theSocket.getInputStream());
			
			theStream.writeInt(itsNodeId);
			theStream.flush();
			
			new MyReceiver(theInStream, itsDatabaseNode.createLogCollector(aHostInfo)).start();
			
			System.out.println("[AbstractDispatchNode] Connected.");
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public int flush() 
	{
		return itsDatabaseNode.flush();
	}
	
	public void clear() 
	{
		itsDatabaseNode.clear();
	}

	public long[] getEventCounts(
			IGridEventFilter aCondition, 
			long aT1, 
			long aT2, 
			int aSlotsCount,
			boolean aForceMergeCounts) 
	{
		return itsDatabaseNode.getEventCounts(aCondition, aT1, aT2, aSlotsCount, aForceMergeCounts);
	}

	public long getEventsCount() 
	{
		return itsDatabaseNode.getEventsCount();
	}
	
	public long getDroppedEventsCount() 
	{
		return itsDatabaseNode.getDroppedEventsCount();
	}
	
	public long getObjectsStoreSize() 
	{
		return itsDatabaseNode.getObjectsStoreSize();
	}



	public long getFirstTimestamp() 
	{
		return itsDatabaseNode.getFirstTimestamp();
	}

	public long getLastTimestamp() 
	{
		return itsDatabaseNode.getLastTimestamp();
	}

	public RINodeEventIterator getIterator(IGridEventFilter aCondition) throws RemoteException 
	{
		return itsDatabaseNode.getIterator(aCondition);
	}

	public Object getRegisteredObject(long aId) 
	{
		return itsDatabaseNode.getRegisteredObject(aId);
	}
	
	public ITypeInfo getObjectType(long aId) 
	{
		return itsDatabaseNode.getObjectType(aId);
	}

	public RIBufferIterator<StringSearchHit[]> searchStrings(String aText) throws RemoteException
	{
		return itsDatabaseNode.searchStrings(aText);
	}

	public long getEventCountAtBehavior(int aBehaviorId) 
	{
		return itsDatabaseNode.getEventCountAtBehavior(aBehaviorId);
	}
	
	public long getEventCountAtClass(int aClassId) throws RemoteException
	{
		return itsDatabaseNode.getEventCountAtClass(aClassId);
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
							getMaster().pushMonitorData(itsNodeId, theData);
							sleep(10000);
						}
						catch (InterruptedException e)
						{
							getMaster().nodeException(new NodeException(itsNodeId, e));
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
	
	private static class MyReceiver extends Thread
	{
		private final DataInputStream itsInStream;
		private final ILogCollector itsCollector;
		
		public MyReceiver(DataInputStream aInStream, ILogCollector aCollector)
		{
			super("NodeConnector.MyReceiver");
			itsInStream = aInStream;
			itsCollector = aCollector;
		}

		@Override
		public void run()
		{
			while(true)
			{
				try
				{
					HighLevelEventReader.readPacket(itsInStream, itsCollector);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}
}
