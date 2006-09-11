/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid;

import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tod.core.ILogCollector;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.HostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.impl.common.EventCollector;
import tod.impl.dbgrid.aggregator.QueryAggregator;
import tod.impl.dbgrid.aggregator.RIQueryAggregator;
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
	
	public GridMaster(ILocationsRepository aLocationsRepository) throws RemoteException
	{
		itsLocationsRepository = aLocationsRepository;
		itsRemoteLocationsRepository = new RemoteLocationsRepository(itsLocationsRepository);
		
		itsNodeServer = new NodeServer();
		
		Timer theTimer = new Timer(true);
		theTimer.schedule(new DataUpdater(), 5000);
	}

	public void addListener(RIGridMasterListener aListener) 
	{
		itsListeners.add(aListener);
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
		int theId = itsNodeProxies.size()+1;
		DBNodeProxy theProxy = new DBNodeProxy(aSocket, theId, this);
		itsNodeProxies.add(theProxy);
		itsDispatcher.addNode(theProxy);
		System.out.println("Registered node (socket): "+theId);
	}
	
	public void registerNode(RIDatabaseNode aNode) throws RemoteException
	{
		itsNodes.add(aNode);
		System.out.println("Registered node (RMI): "+aNode.getNodeId());
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
			Utils.fillCollection(theThreads, theCollector.getThreads());
		}
		
		return theThreads;
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
