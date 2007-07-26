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
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tod.Util;
import tod.agent.AgentConfig;
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
import tod.impl.dbgrid.dispatch.RIDatabaseNode;
import tod.impl.dbgrid.dispatch.RIDispatchNode;
import tod.impl.dbgrid.dispatch.RIDatabaseNode.StringSearchHit;
import tod.impl.dbgrid.dispatch.tree.DispatchTreeStructure;
import tod.impl.dbgrid.dispatch.tree.LocalDispatchTreeStructure;
import tod.impl.dbgrid.dispatch.tree.DispatchTreeStructure.NodeRole;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.TODUtils;
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
	public static final String READY_STRING = "[GridMaster] Ready.";
	
	private TODConfig itsConfig;
	private final IInstrumenter itsInstrumenter;

	private List<ListenerData> itsListeners = new ArrayList<ListenerData>();
	
	private final boolean itsStartServer;
	private TODServer itsServer;
	
	/**
	 * Set by {@link #keepAlive()}
	 */
	private long itsLastKeepAlive = System.currentTimeMillis();
	
	
	private ILocationStore itsLocationStore;
	private RemoteLocationsRepository itsRemoteLocationsRepository;
	
	private long itsEventsCount;
	private long itsFirstTimestamp;
	private long itsLastTimestamp;
	private int itsThreadCount;
	
	private final DispatchTreeStructure itsDispatchTreeStructure;
	
	
	private List<IThreadInfo> itsThreads = new ArrayList<IThreadInfo>();
	private List<IHostInfo> itsHosts = new ArrayList<IHostInfo>();

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
		itsDispatchTreeStructure = new LocalDispatchTreeStructure(this, aDatabaseNode);
		itsRemoteLocationsRepository = new RemoteLocationsRepository(itsLocationStore);

		itsLocalLogBrowser = new GridLogBrowser(this);
		
		itsStartServer = aStartServer;	
		
		createTimeoutThread();
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
			DispatchTreeStructure aTreeStructure) throws RemoteException
	{
		itsConfig = aConfig;
		itsInstrumenter = aInstrumenter;
		itsLocationStore = aLocationStore;
		itsDispatchTreeStructure = aTreeStructure;
		itsDispatchTreeStructure.setMaster(this);
		itsRemoteLocationsRepository = new RemoteLocationsRepository(itsLocationStore);
		
		itsLocalLogBrowser = new GridLogBrowser(this);
		
		itsStartServer = true;

		createTimeoutThread();
	}
	
	private void createTimeoutThread()
	{
		Integer theTimeout = getConfig().get(TODConfig.MASTER_TIMEOUT);
		if (theTimeout != null && theTimeout > 0)
		{
			new TimeoutThread(theTimeout*1000).start();
		}
	}
	
	public TODConfig getConfig() 
	{
		return itsConfig;
	}
	
	public void setConfig(TODConfig aConfig) 
	{
		itsConfig = aConfig;
		
		itsServer.setConfig(aConfig);
		itsInstrumenter.setGlobalWorkingSet(aConfig.get(TODConfig.SCOPE_GLOBAL_FILTER));
		itsInstrumenter.setTraceWorkingSet(aConfig.get(TODConfig.SCOPE_TRACE_FILTER));
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
		itsDispatchTreeStructure.waitReady();
		if (itsStartServer) itsServer = createServer();
		ready();
	}
	
	/**
	 * Called when the dispatching tree (dispatchers and db nodes) is set up.
	 */
	protected void ready()
	{
		Timer theTimer = new Timer(true);
		theTimer.schedule(new DataUpdater(), 5000, 3000);
		
		System.out.println(READY_STRING);
	}
	
	public void keepAlive()
	{
		itsLastKeepAlive = System.currentTimeMillis();
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
		System.out.println("[GridMaster] addListener...");
		ListenerData theListenerData = new ListenerData(aListener);
		itsListeners.add(theListenerData);
//		theListenerData.fireEventsReceived();
		System.out.println("[GridMaster] addListener done.");
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
		NodeRole theRole = itsDispatchTreeStructure.getRoleForNode(aHostName);
		System.out.println("Assigned role "+theRole+" to "+aHostName);

		return theRole;
	}

	public synchronized String registerNode(RIDispatchNode aNode, String aHostname) throws NodeRejectedException
	{
		String theId = itsDispatchTreeStructure.registerNode(aNode, aHostname);
		
		// Register the node in the RMI registry.
		try
		{
			Registry theRegistry = LocateRegistry.getRegistry(DebuggerGridConfig.MASTER_HOST, Util.TOD_REGISTRY_PORT);
			theRegistry.bind(theId, aNode);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return theId;
	}
	
	
	public synchronized void nodeException(NodeException aException) 
	{
		System.err.println(String.format(
				"Received exception %s from node %s",
				aException.getMessage(),
				aException.getNodeId()));
		
		getRootDispatcher().nodeException(aException);
	}
	
	/**
	 * Returns the currently registered nodes.
	 */
	public List<RIDatabaseNode> getNodes()
	{
		return itsDispatchTreeStructure.getDatabaseNodes();
	}
	
	/**
	 * Returns the number of registered nodes.
	 */
	public int getNodeCount()
	{
		return getNodes().size();
	}
	
	/**
	 * Returns the root dispatcher.
	 */
	protected AbstractEventDispatcher getRootDispatcher()
	{
		return itsDispatchTreeStructure.getRootDispatcher();
	}
	
	/**
	 * Returns the event dispatcher. For testing only.
	 */
	public AbstractEventDispatcher _getDispatcher()
	{
		return getRootDispatcher();
	}
	
	public void clear() 
	{
		getRootDispatcher().clear();
		itsThreads.clear();
		itsHosts.clear();
		updateStats();
	}
	
	/**
	 * Ensures that all buffered data is pushed to the nodes.
	 */
	public void flush()
	{
		getRootDispatcher().flush();
	}
	
	/**
	 * Registers a thread. Should be used by the {@link LogReceiver}
	 * created by the root dispatcher
	 * @see AbstractEventDispatcher#createLogReceiver(GridMaster, tod.core.LocationRegistrer, Socket) 
	 */
	public void registerThread(IThreadInfo aThreadInfo)
	{
		itsThreads.add(aThreadInfo);
		((HostInfo) aThreadInfo.getHost()).addThread(aThreadInfo);
	}

	/**
	 * Registers a host. Should be used by the {@link LogReceiver}
	 * created by the root dispatcher
	 * @see AbstractEventDispatcher#createLogReceiver(GridMaster, tod.core.LocationRegistrer, Socket) 
	 */
	public void registerHost(IHostInfo aHostInfo)
	{
		itsHosts.add(aHostInfo);
	}
	
	public List<IThreadInfo> getThreads()
	{
		System.out.println("[GridMaster] getThreads - will return "+itsThreads.size()+" threads.");
		return itsThreads;
	}
	
	public List<IHostInfo> getHosts()
	{
		return itsHosts;
	}

	public RIQueryAggregator createAggregator(EventCondition aCondition) throws RemoteException
	{
		System.out.println("[GridMaster] Creating aggregator for conditions: "+aCondition);
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
		List<Object> theResults = Utils.fork(getNodes(), new ITask<RIDatabaseNode, Object>()
				{
					public Object run(RIDatabaseNode aInput)
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
			for (RIDatabaseNode theNode : getNodes())
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
				LogReceiver theReceiver = getRootDispatcher().createLogReceiver(
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
	
	
	public static void main(String[] args) throws Exception
	{
		Registry theRegistry = Util.getRegistry();
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
	
	/**
	 * This thread is in charge of exiting the database when no client is connected
	 * for a long time.
	 * @author gpothier
	 */
	private class TimeoutThread extends Thread
	{
		private long itsTimeout;
		
		public TimeoutThread(long aTimeout)
		{
			itsTimeout = aTimeout;
		}

		@Override
		public void run()
		{
			System.out.println("[GridMaster] Timeout thread started.");
			try
			{
				while(true)
				{
					long theDelta = System.currentTimeMillis() - itsLastKeepAlive;
					if (theDelta > itsTimeout)
					{
						System.out.println("[GridMaster] Timeout, exiting");
						System.exit(0);
					}
					
					Thread.sleep(5000);
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

}
