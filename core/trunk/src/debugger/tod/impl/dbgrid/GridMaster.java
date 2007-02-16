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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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

import tod.agent.AgentConfig;
import tod.agent.DebugFlags;
import tod.core.bci.IInstrumenter;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILocationStore;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.HostInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.server.TODServer;
import tod.core.transport.LogReceiver;
import tod.impl.dbgrid.aggregator.QueryAggregator;
import tod.impl.dbgrid.aggregator.RIQueryAggregator;
import tod.impl.dbgrid.aggregator.StringHitsAggregator;
import tod.impl.dbgrid.db.NodeRejectedException;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.dispatch.AbstractEventDispatcher;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.dispatch.InternalEventDispatcher;
import tod.impl.dbgrid.dispatch.LeafEventDispatcher;
import tod.impl.dbgrid.dispatch.RIDatabaseNode;
import tod.impl.dbgrid.dispatch.RIDispatchNode;
import tod.impl.dbgrid.dispatch.RIEventDispatcher;
import tod.impl.dbgrid.dispatch.RIInternalDispatcher;
import tod.impl.dbgrid.dispatch.RILeafDispatcher;
import tod.impl.dbgrid.dispatch.RILeafDispatcher.StringSearchHit;
import tod.impl.dbgrid.gridimpl.GridImpl;
import tod.impl.dbgrid.gridimpl.IGridImplementationFactory;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.TODUtils;
import tod.utils.pipe.PipedInputStream2;
import tod.utils.pipe.PipedOutputStream2;
import tod.utils.remote.RILocationsRepository;
import tod.utils.remote.RemoteLocationsRepository;
import zz.utils.ITask;
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

	private List<ListenerData> itsListeners = new ArrayList<ListenerData>();
	
	private TODServer itsServer;
	
	private List<RIDatabaseNode> itsNodes = new ArrayList<RIDatabaseNode>();
	private List<RILeafDispatcher> itsLeafDispatchers = new ArrayList<RILeafDispatcher>();
	private List<RIEventDispatcher> itsInternalDispatchers = new ArrayList<RIEventDispatcher>();
	
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
	
	/**
	 * Pre-registered database nodes
	 * (see {@link #getRoleForNode(String)}).
	 */
	private int itsPreNodes;
	private int itsPreLeafDispatchers;
	private int itsPreInternalDispatchers;
	
	private Set<String> itsNodeHosts = new HashSet<String>();
	
	/**
	 * We maintain a separate set of host names for pre-registration.
	 * This can probably be improved...
	 */
	private Set<String> itsPreNodeHosts = new HashSet<String>();
	
	private ThreadHostRegisterer itsRegisterer = new ThreadHostRegisterer();

	/**
	 * Maps node ids to {@link PrintWriter} objects that write to a log file
	 */
	private Map<String, PrintWriter> itsMonitorLogs = new HashMap<String, PrintWriter>();
	
	/**
	 * A log browser for {@link #exec(ITask)}.
	 */
	private ILogBrowser itsLocalLogBrowser;
	
	/**
	 * Creates a master with a single database node.
	 */
	public GridMaster(
			TODConfig aConfig, 
			ILocationStore aLocationStore, 
			IInstrumenter aInstrumenter,
			DatabaseNode aDatabaseNode,
			boolean aStartServer) throws RemoteException
	{
		itsConfig = aConfig;
		itsInstrumenter = aInstrumenter;
		itsLocationStore = aLocationStore;
		itsRemoteLocationsRepository = new RemoteLocationsRepository(itsLocationStore);

		itsLocalLogBrowser = new GridLogBrowser(this);
		
		try
		{
			IGridImplementationFactory theFactory = GridImpl.getFactory(itsConfig);
			LeafEventDispatcher theDispatcher = theFactory.createLeafDispatcher(false, itsLocationStore);
			itsRootDispatcher = theDispatcher;
			itsLeafDispatchers.add(theDispatcher);
		
			PipedInputStream2 theDispatcherIn = new PipedInputStream2();
			PipedInputStream2 theNodeIn = new PipedInputStream2();
			PipedOutputStream2 theDispatcherOut = new PipedOutputStream2(theNodeIn);
			PipedOutputStream2 theNodeOut = new PipedOutputStream2(theDispatcherIn);
			
			theDispatcher.acceptChild(
					"db-0", 
					aDatabaseNode, 
					theDispatcherIn, 
					theDispatcherOut);
			
			aDatabaseNode.connectToLocalDispatcher(theNodeIn, theNodeOut);
			
			aDatabaseNode.connectToLocalMaster(this, "db-0");
			theDispatcher.connectToLocalMaster(this, "leaf-0");
			
			itsNodes.add(aDatabaseNode);
			
			// Setup server
			if (aStartServer) itsServer = createServer();

			ready();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Initializes a grid master. After calling the constructor, the
	 * {@link #waitReady()} method should be called to wait for the nodes
	 * to connect.
	 */
	public GridMaster(
			TODConfig aConfig, 
			ILocationStore aLocationStore, 
			IInstrumenter aInstrumenter,
			int aExpectedNodes) throws RemoteException
	{
		itsConfig = aConfig;
		itsInstrumenter = aInstrumenter;
		itsLocationStore = aLocationStore;
		itsRemoteLocationsRepository = new RemoteLocationsRepository(itsLocationStore);
		
		itsLocalLogBrowser = new GridLogBrowser(this);

		try
		{
			if (aExpectedNodes > 0 && DebuggerGridConfig.CHECK_SAME_HOST) 
			{
				itsNodeHosts.add(InetAddress.getLocalHost().getHostName());
				itsPreNodeHosts.add(InetAddress.getLocalHost().getHostName());
			}
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		
		

		if (DebugFlags.DISPATCH_FAKE_1)
		{
			itsExpectedNodes = 1;
			itsExpectedLeafDispatchers = 1;
			itsExpectedInternalDispatchers = 0;
		}
		else if (aExpectedNodes == 0)
		{
			
		}
		else
		{
			itsExpectedNodes = aExpectedNodes;
			DispatchTreeStructure theStructure = DispatchTreeStructure.compute(itsExpectedNodes);
			itsExpectedInternalDispatchers = theStructure.internalNodes;
			itsExpectedLeafDispatchers = theStructure.leafNodes;			
		}
	}
	
	public TODConfig getConfig() 
	{
		return itsConfig;
	}
	
	public void setConfig(TODConfig aConfig) 
	{
		itsConfig = aConfig;
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
	 * Waits until all nodes and dispatchers are properly connected.
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
				LeafEventDispatcher theDispatcher = theFactory.createLeafDispatcher(false, itsLocationStore);
				theDispatcher.connectToLocalMaster(this, "root");
				itsRootDispatcher = theDispatcher;
				itsLeafDispatchers.add(theDispatcher);
			}
			else
			{
				InternalEventDispatcher theDispatcher = new InternalEventDispatcher();
				theDispatcher.forwardLocations(itsLocationStore.getLocations());
				itsRootDispatcher = theDispatcher;
			}

			int theBranchingFactor = DebugFlags.DISPATCH_FAKE_1 ? 
					1 
					: DebuggerGridConfig.DISPATCH_BRANCHING_FACTOR;
			
			// Queue of nodes that must be connected to a parent.
			LinkedList<RIEventDispatcher> theChildrenQueue =
				new LinkedList<RIEventDispatcher>();
			
			// Connect nodes to leaf dispatchers
			Iterator<RIDatabaseNode> theNodesIterator = itsNodes.iterator();
			for (RIEventDispatcher theDispatcher : itsLeafDispatchers)
			{
				for(int i=0;i<theBranchingFactor && theNodesIterator.hasNext();i++)
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
				
				for(int i=0;i<theBranchingFactor && !theChildrenQueue.isEmpty();i++)
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
			
			ready();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Called when the dispatching tree (dispatchers and db nodes) is set up.
	 */
	protected void ready()
	{
		Timer theTimer = new Timer(true);
		theTimer.schedule(new DataUpdater(), 5000, 3000);
		
		System.out.println("[GridMaster] Ready!");
	}
	
	/**
	 * Stops accepting new connections from debuggees.
	 */
	public void stop()
	{
		itsServer.stop();
	}

	public void disconnect()
	{
		itsServer.disconnect();
	}
	
	public void addListener(RIGridMasterListener aListener) 
	{
		ListenerData theListenerData = new ListenerData(aListener);
		itsListeners.add(theListenerData);
		theListenerData.fireEventsReceived();
	}
	
	public void pushMonitorData(String aNodeId, MonitorData aData)
	{
//		System.out.println("Received monitor data from node #"+aNodeId+"\n"+Monitor.format(aData, false));
		
		PrintWriter theWriter = itsMonitorLogs.get(aNodeId);
		if (theWriter == null)
		{
			try
			{
				theWriter = new PrintWriter(new FileWriter("log-"+aNodeId+".txt"));
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			itsMonitorLogs.put(aNodeId, theWriter);
		}
		
		theWriter.println();
		theWriter.println(Monitor.format(aData, false));
		theWriter.flush();
		
		fireMonitorData(aNodeId, aData);
	}

	/**
	 * Fires the {@link RIGridMasterListener#eventsReceived()} message
	 * to all listeners.
	 */
	protected void fireEventsReceived() 
	{
		for (Iterator<ListenerData> theIterator = itsListeners.iterator(); theIterator.hasNext();)
		{
			ListenerData theListenerData = theIterator.next();
			if (! theListenerData.fireEventsReceived()) 
			{
				System.out.println("Removing stale listener");
				theIterator.remove();
			}
		}
	}
	
	/**
	 * Fires the {@link RIGridMasterListener#eventsReceived()} message
	 * to all listeners.
	 */
	protected void fireMonitorData(String aNodeId, MonitorData aData) 
	{
		for (Iterator<ListenerData> theIterator = itsListeners.iterator(); theIterator.hasNext();)
		{
			ListenerData theListenerData = theIterator.next();
			if (! theListenerData.fireMonitorData(aNodeId, aData)) 
			{
				System.out.println("Removing stale listener");
				theIterator.remove();
			}
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
		
		for (Iterator<ListenerData> theIterator = itsListeners.iterator(); theIterator.hasNext();)
		{
			ListenerData theListenerData = theIterator.next();
			if (! theListenerData.fireException(aThrowable)) 
			{
				System.out.println("Removing stale listener");
				theIterator.remove();
			}
		}
	}
	
	public synchronized NodeRole getRoleForNode(String aHostName) 
	{
		NodeRole theRole;
		
		if (itsPreNodes < itsExpectedNodes 
				&& (! DebuggerGridConfig.CHECK_SAME_HOST || itsPreNodeHosts.add(aHostName)))
		{
			itsPreNodes++;
			theRole = NodeRole.DATABASE;
		}
		else if (itsPreLeafDispatchers < itsExpectedLeafDispatchers)
		{
			itsPreLeafDispatchers++;
			theRole = NodeRole.LEAF_DISPATCHER; 
		}
		else if (itsPreInternalDispatchers < itsExpectedInternalDispatchers)
		{
			itsPreInternalDispatchers++;
			theRole = NodeRole.INTERNAL_DISPATCHER;
		}
		else theRole = null;
		
		System.out.println("Assigned role "+theRole+" to "+aHostName);
		return theRole;
	}

	public synchronized String registerNode(RIDispatchNode aNode, String aHostname) throws NodeRejectedException
	{
		String theId;
		
		if (aNode instanceof RIDatabaseNode)
		{
			RIDatabaseNode theDatabaseNode = (RIDatabaseNode) aNode;
			theId = registerDatabaseNode(theDatabaseNode, aHostname);
		}
		else if (aNode instanceof RILeafDispatcher)
		{
			RILeafDispatcher theLeafDispatcher = (RILeafDispatcher) aNode;
			theId = registerLeafDispatcher(theLeafDispatcher, aHostname);
		}
		else if (aNode instanceof RIInternalDispatcher)
		{
			RIInternalDispatcher theInternalDispatcher = (RIInternalDispatcher) aNode;
			theId = registerInternalDispatcher(theInternalDispatcher, aHostname);
		}
		else throw new RuntimeException("Not handled: "+aNode);
		
		// Register the node in the RMI registry.
		try
		{
			Registry theRegistry = LocateRegistry.getRegistry(DebuggerGridConfig.MASTER_HOST);
			theRegistry.bind(theId, aNode);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
		return theId;
	}
	
	private String registerDatabaseNode(RIDatabaseNode aNode, String aHostname) throws NodeRejectedException
	{
		if (itsExpectedNodes > 0 && itsNodes.size() >= itsExpectedNodes) 
			throw new NodeRejectedException("Maximum number of nodes reached");
		
		if (DebuggerGridConfig.CHECK_SAME_HOST && ! itsNodeHosts.add(aHostname)) 
			throw new NodeRejectedException("Refused node from same host");
		
		int theId = itsNodes.size()+1;
		itsNodes.add(aNode);
		System.out.println("Registered node (RMI): "+theId+" from "+aHostname);
		
		return "db-"+theId;
	}
	
	private String registerLeafDispatcher(
			RILeafDispatcher aDispatcher, 
			String aHostname) throws NodeRejectedException
	{
		if (itsLeafDispatchers.size() >= itsExpectedLeafDispatchers) 
			throw new NodeRejectedException("Maximum number of leaf dispatchers reached");
		
		int theId = itsLeafDispatchers.size()+1;
		itsLeafDispatchers.add(aDispatcher);
		System.out.println("Registered leaf dispatcher (RMI): "+theId+" from "+aHostname);
		
		return "leaf-"+theId;
	}
	
	private String registerInternalDispatcher(
			RIEventDispatcher aDispatcher, 
			String aHostname) throws NodeRejectedException
	{
		if (itsInternalDispatchers.size() >= itsExpectedInternalDispatchers) 
			throw new NodeRejectedException("Maximum number of internal dispatchers reached");
		
		int theId = itsInternalDispatchers.size()+1;
		itsInternalDispatchers.add(aDispatcher);
		System.out.println("Registered internal dispatcher (RMI): "+theId+" from "+aHostname);
		
		return "internal-"+theId;
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
	 * Returns the currently registered leaf dispatchers.
	 */
	public List<RILeafDispatcher> getLeafDispatchers()
	{
		return itsLeafDispatchers;
	}

	/**
	 * Returns the number of registered nodes.
	 */
	public int getNodeCount()
	{
		return itsNodes.size();
	}
	
	/**
	 * Returns the event dispatcher. For testing only.
	 */
	public AbstractEventDispatcher _getDispatcher()
	{
		return itsRootDispatcher;
	}
	
	public void clear() 
	{
		itsRootDispatcher.clear();
		itsRegisterer.clear();
		updateStats();
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
		if (itsEventsCount == 0) updateStats();
		return itsEventsCount;
	}

	public long getFirstTimestamp()
	{
		if (itsFirstTimestamp == 0) updateStats();
		return itsFirstTimestamp;
	}

	public long getLastTimestamp()
	{
		if (itsLastTimestamp == 0) updateStats();
		return itsLastTimestamp;
	}
	
	public Object getRegisteredObject(final long aId)
	{
		List<Object> theResults = Utils.fork(itsLeafDispatchers, new ITask<RILeafDispatcher, Object>()
				{
					public Object run(RILeafDispatcher aInput)
					{
						try
						{
							return aInput.getRegisteredObject(aId);
						}
						catch (RemoteException e)
						{
							throw new RuntimeException(e);
						}
					}
				});
		
		Object theObject = null;
		for (Object theResult : theResults)
		{
			if (theResult == null) continue;
			if (theObject != null) throw new RuntimeException("Object present in various nodes!");
			theObject = theResult;
		}
		
		return theObject;
	}
	
	public RIBufferIterator<StringSearchHit[]> searchStrings(String aSearchText) throws RemoteException
	{
		return new StringHitsAggregator(this, aSearchText);
	}
	
	protected void updateStats()
	{
		itsEventsCount = 0;
		itsFirstTimestamp = Long.MAX_VALUE;
		itsLastTimestamp = 0;
		
		try
		{
			for (RIDatabaseNode theNode : itsNodes)
			{
				itsEventsCount += theNode.getEventsCount();
				itsFirstTimestamp = Math.min(itsFirstTimestamp, theNode.getFirstTimestamp());
				itsLastTimestamp = Math.max(itsLastTimestamp, theNode.getLastTimestamp());
			}
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}

	}

	public ILocationStore getLocationStore()
	{
		return itsLocationStore;
	}
	
	public RILocationsRepository getLocationsRepository() 
	{
		System.out.println("GridMaster.getLocationsRepository()");
		return itsRemoteLocationsRepository;
	}

	public <O> O exec(ITask<ILogBrowser, O> aTask)
	{
		return aTask.run(itsLocalLogBrowser);
	}

	/**
	 * A timer task that periodically updates aggregate data,
	 * and notifies listeners if data has changed since last update. 
	 * @author gpothier
	 */
	private class DataUpdater extends TimerTask
	{
		private long itsPreviousEventsCount;
		private long itsPreviousFirstTimestamp;
		private long itsPreviousLastTimestamp;
		private int itsPreviousThreadCount;
		
		@Override
		public void run()
		{
			updateStats();
			
			if (itsPreviousEventsCount != itsEventsCount
					|| itsPreviousFirstTimestamp != itsFirstTimestamp
					|| itsPreviousLastTimestamp != itsLastTimestamp
					|| itsPreviousThreadCount != itsThreadCount)
			{
				itsPreviousEventsCount = itsEventsCount;
				itsPreviousFirstTimestamp = itsFirstTimestamp;
				itsPreviousLastTimestamp = itsLastTimestamp;
				itsPreviousThreadCount = itsThreadCount;
				
				fireEventsReceived();
			}
		}
	}

	private class MyTODServer extends TODServer
	{
		private int itsCurrentHostId = 1;
		
		public MyTODServer(TODConfig aConfig, IInstrumenter aInstrumenter)
		{
			super(aConfig, aInstrumenter, itsLocationStore);
		}

		@Override
		protected LogReceiver createReceiver(Socket aSocket)
		{
			HostInfo theHostInfo = new HostInfo(itsCurrentHostId++);
			registerHost(theHostInfo);
			
			try
			{
				LogReceiver theReceiver = itsRootDispatcher.createLogReceiver(
						theHostInfo,
						GridMaster.this,
						new BufferedInputStream(
								aSocket.getInputStream(), 
								AgentConfig.COLLECTOR_BUFFER_SIZE*2),
						new BufferedOutputStream(
								aSocket.getOutputStream(), 
								AgentConfig.COLLECTOR_BUFFER_SIZE*2),
						true);
				
				return theReceiver;
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		@Override
		protected void disconnected()
		{
			super.disconnected();
			GridMaster.this.flush();
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
			IHostInfo theHost = aThreadInfo.getHost();
			int theHostId = theHost != null ? theHost.getId() : 0;
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
		
		public void clear()
		{
			itsHosts.clear();
			itsThreadsMap.clear();
		}
	}

	public static void main(String[] args) throws Exception
	{
		LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		Registry theRegistry = LocateRegistry.getRegistry("localhost");
		TODUtils.setupMaster(theRegistry, args);
		System.out.println("Master ready.");
	}
	
	/**
	 * Data associated to listeners. Permits to identify and remove stale listeners. 
	 * @author gpothier
	 */
	private static class ListenerData
	{
		private RIGridMasterListener itsListener;
		private long itsFirstFailureTime;
		
		public ListenerData(RIGridMasterListener aListener)
		{
			itsListener = aListener;
		}
		
		public boolean fireEventsReceived()
		{
			try
			{
				itsListener.eventsReceived();
				return fire(false);
			}
			catch (RemoteException e)
			{
				return fire(true);
			}
		}
		
		public boolean fireException(Throwable aThrowable)
		{
			try
			{
				itsListener.exception(aThrowable);
				return fire(false);
			}
			catch (RemoteException e)
			{
				return fire(true);
			}
		}
		
		public boolean fireMonitorData(String aNodeId, MonitorData aData)
		{
			try
			{
				itsListener.monitorData(aNodeId, aData);
				return fire(false);
			}
			catch (RemoteException e)
			{
				return fire(true);
			}
		}
		
		/**
		 * Returns false if the listener is no longer valid.
		 */
		private boolean fire(boolean aFailed)
		{
			if (aFailed)
			{
				long theTime = System.currentTimeMillis();
				if (itsFirstFailureTime == 0)
				{
					itsFirstFailureTime = theTime;
					return true;
				}
				else
				{
					long theDelta = theTime - itsFirstFailureTime;
					System.out.println("Listener stale for "+theDelta+"ms");
					return theDelta < 10000;
				}
			}
			else
			{
				itsFirstFailureTime = 0;
				return true;
			}
		}
	}
}
