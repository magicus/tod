/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import tod.core.ILogCollector;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.HostInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.impl.common.EventCollector;
import tod.impl.dbgrid.aggregator.QueryAggregator;
import tod.impl.dbgrid.aggregator.RIQueryAggregator;
import tod.impl.dbgrid.dbnode.NodeRejectedException;
import tod.impl.dbgrid.dbnode.RIDatabaseNode;
import tod.impl.dbgrid.dispatcher.DBNodeProxy;
import tod.impl.dbgrid.dispatcher.EventDispatcher;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.remote.RILocationsRepository;
import tod.utils.remote.RemoteLocationsRepository;
import zz.utils.Utils;
import zz.utils.net.Server;

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
	
	private List<RIGridMasterListener> itsListeners = new ArrayList<RIGridMasterListener>();
	
	private List<RIDatabaseNode> itsNodes = new ArrayList<RIDatabaseNode>();
	private List<DBNodeProxy> itsNodeProxies = new ArrayList<DBNodeProxy>();
	
	private EventDispatcher itsDispatcher = new EventDispatcher(this);
	
	/**
	 * This map stores the collector associated with each host.
	 */
	private Map<Integer, GridEventCollector> itsCollectors = new HashMap<Integer, GridEventCollector>();
	
	private ILocationsRepository itsLocationsRepository;
	private RemoteLocationsRepository itsRemoteLocationsRepository;
	private NodeServer itsNodeServer;
	
	private long itsEventsCount;
	private long itsFirstTimestamp;
	private long itsLastTimestamp;
	private int itsThreadCount;
	
	/**
	 * Maximum number of nodes to accept.
	 */
	private int itsMaxNodes;
	
	private Set<String> itsNodeHosts = new HashSet<String>();
	
	public GridMaster(ILocationsRepository aLocationsRepository, int aMaxNodes) throws RemoteException
	{
		itsLocationsRepository = aLocationsRepository;
		itsMaxNodes = aMaxNodes;
		itsRemoteLocationsRepository = new RemoteLocationsRepository(itsLocationsRepository);
		
		itsNodeServer = new NodeServer();
		
		Timer theTimer = new Timer(true);
		theTimer.schedule(new DataUpdater(), 5000, 3000);
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
	
	public synchronized void acceptNode(Socket aSocket)
	{
		try
		{
			DataInputStream theStream = new DataInputStream(aSocket.getInputStream());
			int theId = theStream.readInt();
			
			DBNodeProxy theProxy = new DBNodeProxy(aSocket, theId, this);
			itsNodeProxies.add(theProxy);
			itsDispatcher.addNode(theProxy);
			System.out.println("Registered node (socket): "+theId);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public synchronized int registerNode(RIDatabaseNode aNode, String aHostname) throws RemoteException, NodeRejectedException
	{
		if (itsMaxNodes > 0 && itsNodes.size() >= itsMaxNodes) 
			throw new NodeRejectedException("Maximum number of nodes reached");
		
		if (! itsNodeHosts.add(aHostname)) 
			throw new NodeRejectedException("Refused node from same host");
		
		int theId = itsNodes.size()+1;
		itsNodes.add(aNode);
		System.out.println("Registered node (RMI): "+theId);
		
		return theId;
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
	 * Creates a new {@link EventCollector} that can receive events
	 * generated by a particular host.
	 * @param aHostId Id of the host that will send events to the collector.
	 */
	public ILogCollector createCollector(int aHostId)
	{
		GridEventCollector theCollector = new GridEventCollector(
				this,
				new HostInfo(aHostId),
				itsLocationsRepository,
				itsDispatcher);
		
		itsCollectors.put(aHostId, theCollector);
		
		return theCollector;
	}
	
	/**
	 * Returns the event dispatcher. For testing only.
	 */
	public EventDispatcher getDispatcher()
	{
		return itsDispatcher;
	}
	
	
	/**
	 * Ensures that all buffered data is pushed to the nodes.
	 */
	public void flush()
	{
		itsDispatcher.flush();
	}

	/**
	 * Returns the number of events received by the collectors.
	 * @return
	 */
	private long getCollectorEventsCount()
	{
		long theCount = 0;
		for (GridEventCollector theCollector : itsCollectors.values())
		{
			theCount += theCollector.getEventsCount();
		}
		
		return theCount;
	}
	

	public IThreadInfo getThread(int aHostId, long aJVMThreadId)
	{
		GridEventCollector theCollector = itsCollectors.get(aHostId);
		return theCollector.getThread(aJVMThreadId);
	}

	public List<IThreadInfo> getThreads()
	{
		List<IThreadInfo> theThreads = new ArrayList<IThreadInfo>();
		for (GridEventCollector theCollector : itsCollectors.values())
		{
			for (IThreadInfo theThread : theCollector.getThreads()) 
			{
				if (theThread != null) theThreads.add (theThread);
			}
		}
		
		return theThreads;
	}
	
	public List<IHostInfo> getHosts()
	{
		List<IHostInfo> theHosts = new ArrayList<IHostInfo>();
		for (GridEventCollector theCollector : itsCollectors.values())
		{
			theHosts.add(theCollector.getHost());
		}
		
		return theHosts;
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

	/**
	 * A server that waits for database nodes to connect
	 * @author gpothier
	 */
	private class NodeServer extends Server
	{
		public NodeServer()
		{
			super(DebuggerGridConfig.MASTER_NODE_PORT);
		}

		@Override
		protected void accepted(Socket aSocket)
		{
			acceptNode(aSocket);
		}
	}
	
	
}
