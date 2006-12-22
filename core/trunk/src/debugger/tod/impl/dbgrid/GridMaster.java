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
package tod.impl.dbgrid;

import static tod.impl.dbgrid.DebuggerGridConfig.DISPATCH_BRANCHING_FACTOR;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import tod.core.bci.IInstrumenter;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILocationStore;
import tod.core.database.structure.HostInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.server.TODServer;
import tod.core.transport.LogReceiver;
import tod.impl.dbgrid.aggregator.QueryAggregator;
import tod.impl.dbgrid.aggregator.RIQueryAggregator;
import tod.impl.dbgrid.dbnode.NodeRejectedException;
import tod.impl.dbgrid.dbnode.RIDatabaseNode;
import tod.impl.dbgrid.dispatcher.AbstractEventDispatcher;
import tod.impl.dbgrid.dispatcher.DBNodeProxy;
import tod.impl.dbgrid.dispatcher.InternalEventDispatcher;
import tod.impl.dbgrid.dispatcher.RIEventDispatcher;
import tod.impl.dbgrid.gridimpl.GridImpl;
import tod.impl.dbgrid.gridimpl.IGridImplementationFactory;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.remote.RILocationsRepository;
import tod.utils.remote.RemoteLocationsRepository;
import zz.utils.Utils;

/**
 * The entry point to the database grid.
 * Manages configuration and discovery of database nodes,
 * acts as a factory for {@link GridEventCollector}s
 * and {@link QueryAggregator}.
 * @author gpothier
 */
public class GridMaster extends UnicastRemoteObject implements RIGridMaster
{
	public static final String RMI_ID = "GridMaster";
	
	private TODConfig itsConfig;
	private final IInstrumenter itsInstrumenter;

	private List<RIGridMasterListener> itsListeners = new ArrayList<RIGridMasterListener>();
	
	private TODServer itsServer;
	
	private List<RIDatabaseNode> itsNodes = new ArrayList<RIDatabaseNode>();
	private List<RIEventDispatcher> itsLeafDispatchers = new ArrayList<RIEventDispatcher>();
	private List<RIEventDispatcher> itsInternalDispatchers = new ArrayList<RIEventDispatcher>();
	
	private List<DBNodeProxy> itsNodeProxies = new ArrayList<DBNodeProxy>();
	
	private AbstractEventDispatcher itsRootDispatcher;
	
	private ILocationStore itsLocationStore;
	private RemoteLocationsRepository itsRemoteLocationsRepository;
	
	private long itsEventsCount;
	private long itsFirstTimestamp;
	private long itsLastTimestamp;
	private int itsThreadCount;
	
	/**
	 * Number of nodes to wait for.
	 */
	private int itsExpectedNodes;
	
	private int itsExpectedLeafDispatchers;
	private int itsExpectedInternalDispatchers;
	
	private Set<String> itsNodeHosts = new HashSet<String>();
	
	private ThreadHostRegisterer itsRegisterer = new ThreadHostRegisterer();

	
	public GridMaster(
			TODConfig aConfig, 
			ILocationStore aLocationStore, 
			IInstrumenter aInstrumenter,
			int aExpectedNodes) throws RemoteException
	{
		itsConfig = aConfig;
		itsInstrumenter = aInstrumenter;
		
		try
		{
			if (aExpectedNodes > 0) itsNodeHosts.add(InetAddress.getLocalHost().getHostName());
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		
		itsLocationStore = aLocationStore;
		itsRemoteLocationsRepository = new RemoteLocationsRepository(itsLocationStore);
		itsExpectedNodes = aExpectedNodes;
		DispatchTreeStructure theStructure = DispatchTreeStructure.compute(itsExpectedNodes);
		itsExpectedInternalDispatchers = theStructure.internalNodes;
		itsExpectedLeafDispatchers = theStructure.leafNodes;
	}
	
	/**
	 * Creates the TOD server, which is in charge of accepting
	 * connections from clients.
	 */
	protected TODServer createServer()
	{
		return new MyTODServer(itsConfig, itsInstrumenter);
	}
	
	/**
	 * Waits until all nodes and dispatchers are properly connected
	 */
	public void waitReady()
	{
		try
		{
			while (itsNodes.size() < itsExpectedNodes
					|| itsLeafDispatchers.size() < itsExpectedLeafDispatchers
					|| itsInternalDispatchers.size() < itsExpectedInternalDispatchers)
			{
				Thread.sleep(1000);
				System.out.println(String.format(
						"Found %d/%d nodes, %d/%d internal dispatchers, %d/%d leaf dispatchers.",
						itsNodes.size(), itsExpectedNodes,
						itsInternalDispatchers.size(), itsExpectedInternalDispatchers,
						itsLeafDispatchers.size(), itsExpectedLeafDispatchers));
			}
			
			// Create root dispatcher (local)
			if (itsExpectedLeafDispatchers == 0)
			{
				assert itsExpectedInternalDispatchers == 0;
				IGridImplementationFactory theFactory = GridImpl.getFactory(itsConfig);
				itsRootDispatcher = theFactory.createLeafDispatcher(false);
				itsLeafDispatchers.add(itsRootDispatcher);
			}
			else
			{
				itsRootDispatcher = new InternalEventDispatcher(false);
			}

			// Queue of nodes that must be connected to a parent.
			LinkedList<RIEventDispatcher> theChildrenQueue =
				new LinkedList<RIEventDispatcher>();
			
			// Connect nodes to leaf dispatchers
			Iterator<RIDatabaseNode> theNodesIterator = itsNodes.iterator();
			for (RIEventDispatcher theDispatcher : itsLeafDispatchers)
			{
				for(int i=0;i<DISPATCH_BRANCHING_FACTOR && theNodesIterator.hasNext();i++)
				{
					RIDatabaseNode theNode = theNodesIterator.next();
					theNode.connectToDispatcher(theDispatcher.getAdress());
				}
				
				// Start by queing all leaf dispatchers
				if (theDispatcher != itsRootDispatcher)
					theChildrenQueue.add(theDispatcher);
			}
			
			// Connect internal dispatchers
			LinkedList<RIEventDispatcher> theParentsQueue =
				new LinkedList<RIEventDispatcher>();
			
			Utils.fillCollection(theParentsQueue, itsInternalDispatchers);
			theParentsQueue.add(itsRootDispatcher);
			
			while (! theParentsQueue.isEmpty())
			{
				RIEventDispatcher theParent = theParentsQueue.removeLast();
				
				for(int i=0;i<DISPATCH_BRANCHING_FACTOR && !theChildrenQueue.isEmpty();i++)
				{
					RIEventDispatcher theChild = theChildrenQueue.removeLast();
					theChild.connectToDispatcher(theParent.getAdress());
				}
				
				theChildrenQueue.addFirst(theParent);
			}
			
			assert theChildrenQueue.removeLast() == itsRootDispatcher;
			assert theChildrenQueue.isEmpty();

			// Setup server
			itsServer = createServer();
			
			Timer theTimer = new Timer(true);
			theTimer.schedule(new DataUpdater(), 5000, 3000);

		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Stops accepting new connections from debuggees.
	 */
	public void disconnect()
	{
		itsServer.disconnect();
	}

	public void addListener(RIGridMasterListener aListener) 
	{
		itsListeners.add(aListener);
		try
		{
			aListener.eventsReceived();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		} 
	}
	
	public void pushMonitorData(int aNodeId, MonitorData aData)
	{
		System.out.println("Received monitor data from node #"+aNodeId+"\n"+Monitor.format(aData, false));
		fireMonitorData(aNodeId, aData);
	}

	/**
	 * Fires the {@link RIGridMasterListener#eventsReceived()} message
	 * to all listeners.
	 */
	protected void fireEventsReceived() 
	{
		try
		{
			for (RIGridMasterListener theListener : itsListeners)
			{
				theListener.eventsReceived();
			}
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Fires the {@link RIGridMasterListener#eventsReceived()} message
	 * to all listeners.
	 */
	protected void fireMonitorData(int aNodeId, MonitorData aData) 
	{
		try
		{
			for (RIGridMasterListener theListener : itsListeners)
			{
				theListener.monitorData(aNodeId, aData);
			}
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Fires the {@link RIGridMasterListener#eventsReceived()} message
	 * to all listeners.
	 */
	public void fireException(Throwable aThrowable) 
	{
		System.err.println("Exception catched in master, will be forwarded to clients.");
		aThrowable.printStackTrace();
		
		try
		{
			for (RIGridMasterListener theListener : itsListeners)
			{
				theListener.exception(aThrowable);
			}
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public synchronized int registerNode(RIDatabaseNode aNode, String aHostname) throws NodeRejectedException
	{
		if (itsExpectedNodes > 0 && itsNodes.size() >= itsExpectedNodes) 
			throw new NodeRejectedException("Maximum number of nodes reached");
		
		if (! itsNodeHosts.add(aHostname)) 
			throw new NodeRejectedException("Refused node from same host");
		
		int theId = itsNodes.size()+1;
		itsNodes.add(aNode);
		System.out.println("Registered node (RMI): "+theId+" from "+aHostname);
		
		return theId;
	}
	
	public void registerDispatcher(
			RIEventDispatcher aDispatcher, 
			String aHostname, 
			boolean aLeaf) throws NodeRejectedException
	{
		if (aLeaf)
		{
			if (itsLeafDispatchers.size() >= itsExpectedLeafDispatchers) 
				throw new NodeRejectedException("Maximum number of leaf dispatchers reached");
			
			int theId = itsLeafDispatchers.size()+1;
			itsLeafDispatchers.add(aDispatcher);
			System.out.println("Registered leaf dispatcher (RMI): "+theId+" from "+aHostname);
		}
		else
		{
			if (itsInternalDispatchers.size() >= itsExpectedInternalDispatchers) 
				throw new NodeRejectedException("Maximum number of internal dispatchers reached");
			
			int theId = itsInternalDispatchers.size()+1;
			itsInternalDispatchers.add(aDispatcher);
			System.out.println("Registered internal dispatcher (RMI): "+theId+" from "+aHostname);
		}
	}
	
	public synchronized void nodeException(NodeException aException) 
	{
		itsRootDispatcher.nodeException(aException);
	}
	
	/**
	 * Returns the currently registered nodes.
	 */
	public List<RIDatabaseNode> getNodes()
	{
		return itsNodes;
	}

	/**
	 * Returns the number of registered nodes.
	 */
	public int getNodeCount()
	{
		return itsNodeProxies.size();
	}
	
	/**
	 * Returns the event dispatcher. For testing only.
	 */
	public AbstractEventDispatcher getDispatcher()
	{
		return itsRootDispatcher;
	}
	
	public void clear() 
	{
		itsRootDispatcher.clear();
	}
	
	/**
	 * Ensures that all buffered data is pushed to the nodes.
	 */
	public void flush()
	{
		itsRootDispatcher.flush();
	}
	
	/**
	 * Registers a thread. Should be used by the {@link LogReceiver}
	 * created by the root dispatcher
	 * @see AbstractEventDispatcher#createLogReceiver(GridMaster, tod.core.LocationRegistrer, Socket) 
	 */
	public void registerThread(IThreadInfo aThreadInfo)
	{
		itsRegisterer.registerThread(aThreadInfo);
	}

	/**
	 * Registers a host. Should be used by the {@link LogReceiver}
	 * created by the root dispatcher
	 * @see AbstractEventDispatcher#createLogReceiver(GridMaster, tod.core.LocationRegistrer, Socket) 
	 */
	public void registerHost(IHostInfo aHostInfo)
	{
		itsRegisterer.registerHost(aHostInfo);
	}
	
	public IThreadInfo getThread(int aHostId, long aJVMThreadId)
	{
		return itsRegisterer.getThread(aHostId, aJVMThreadId);
	}

	public List<IThreadInfo> getThreads()
	{
		return itsRegisterer.getThreads();
	}
	
	public List<IHostInfo> getHosts()
	{
		return itsRegisterer.getHosts();
	}

	public RIQueryAggregator createAggregator(EventCondition aCondition) throws RemoteException
	{
		return new QueryAggregator(this, aCondition);
	}
	
	public long getEventsCount()
	{
		return itsEventsCount;
	}

	public long getFirstTimestamp()
	{
		return itsFirstTimestamp;
	}

	public long getLastTimestamp()
	{
		return itsLastTimestamp;
	}

	
	public RILocationsRepository getLocationsRepository() 
	{
		System.out.println("GridMaster.getLocationsRepository()");
		return itsRemoteLocationsRepository;
	}


	/**
	 * A timer task that periodically updates aggregate data,
	 * and notifies listeners if data has changed since last update. 
	 * @author gpothier
	 */
	private class DataUpdater extends TimerTask
	{
		
		@Override
		public void run()
		{
			long theEventsCount = 0;
			long theFirstTimestamp = Long.MAX_VALUE;
			long theLastTimestamp = 0;
			int theThreadsCount = 0;
			
			for (DBNodeProxy theProxy : itsNodeProxies)
			{
				theEventsCount += theProxy.getEventsCount();
				theFirstTimestamp = Math.min(theFirstTimestamp, theProxy.getFirstTimestamp());
				theLastTimestamp = Math.max(theLastTimestamp, theProxy.getLastTimestamp());
			}
			
			if (theEventsCount != itsEventsCount
					|| theFirstTimestamp != itsFirstTimestamp
					|| theLastTimestamp != itsLastTimestamp
					|| theThreadsCount != itsThreadCount)
			{
				itsEventsCount = theEventsCount;
				itsFirstTimestamp = theFirstTimestamp;
				itsLastTimestamp = theLastTimestamp;
				itsThreadCount = theThreadsCount;
				
				fireEventsReceived();
			}
		}
	}

	private class MyTODServer extends TODServer
	{
		private int itsHostId = 1;
		
		public MyTODServer(TODConfig aConfig, IInstrumenter aInstrumenter)
		{
			super(aConfig, aInstrumenter);
		}

		@Override
		protected LogReceiver createReceiver(Socket aSocket)
		{
			HostInfo theHostInfo = new HostInfo(itsHostId++);
			registerHost(theHostInfo);
			
			try
			{
				return itsRootDispatcher.createLogReceiver(
						theHostInfo,
						GridMaster.this,
						getLocationRegistrer(),
						aSocket.getInputStream(),
						aSocket.getOutputStream(),
						true);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Utility class that registers threads and hosts detected by
	 * the root dispatcher.
	 * @author gpothier
	 */
	private static class ThreadHostRegisterer
	{
		private Map<Integer, Map<Long, IThreadInfo>> itsThreadsMap =
			new HashMap<Integer, Map<Long,IThreadInfo>>();
		
		private List<IHostInfo> itsHosts = new ArrayList<IHostInfo>();
		
		public void registerThread(IThreadInfo aThreadInfo)
		{
			int theHostId = aThreadInfo.getHost().getId();
			long theJVMId = aThreadInfo.getJVMId();
			Map<Long, IThreadInfo> theHostMap = itsThreadsMap.get(theHostId);
			if (theHostMap == null)
			{
				theHostMap = new HashMap<Long, IThreadInfo>();
				itsThreadsMap.put(theHostId, theHostMap);
			}
			
			theHostMap.put(theJVMId, aThreadInfo);
		}
		
		public IThreadInfo getThread(int aHostId, long aJVMThreadId)
		{
			Map<Long, IThreadInfo> theHostMap = itsThreadsMap.get(aHostId);
			if (theHostMap == null) return null;
			return theHostMap.get(aJVMThreadId);
		}
		
		public List<IThreadInfo> getThreads()
		{
			List<IThreadInfo> theThreads = new ArrayList<IThreadInfo>();
			for (Map<Long, IThreadInfo> theHostMap : itsThreadsMap.values())
			{
				Utils.fillCollection(theThreads, theHostMap.values());
			}
			return theThreads;
		}
		
		public void registerHost(IHostInfo aHostInfo)
		{
			itsHosts.add(aHostInfo);
		}
		
		public List<IHostInfo> getHosts()
		{
			return itsHosts;
		}
	}
	
}
