/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tod.core.ILogCollector;
import tod.core.bci.IInstrumenter;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.HostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.impl.dbgrid.GridEventCollector.GridThreadInfo;
import tod.impl.dbgrid.aggregator.QueryAggregator;
import tod.impl.dbgrid.aggregator.RIQueryAggregator;
import tod.impl.dbgrid.dbnode.RIDatabaseNode;
import tod.impl.dbgrid.dispatcher.EventDispatcher;
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
	
	private List<RIGridMasterListener> itsListeners = new ArrayList<RIGridMasterListener>();
	
	private List<RIDatabaseNode> itsNodes = new ArrayList<RIDatabaseNode>();
	private QueryAggregator itsAggregator = new QueryAggregator(this);
	private EventDispatcher itsDispatcher = new EventDispatcher(this);
	
	/**
	 * This map stores the collector associated with each host.
	 */
	private Map<Integer, GridEventCollector> itsCollectors = new HashMap<Integer, GridEventCollector>();
	
	private ILocationsRepository itsLocationsRepository;
	
	private long itsEventsCount;
	private long itsFirstTimestamp;
	private long itsLastTimestamp;
	private int itsThreadCount;
	
	/**
	 * A counter used to generate sequential thread numbers.
	 * This permits to reduce the number of bits used to represent thread ids,
	 * as all 64 bits of original thread ids might be used.
	 */
	private static int itsLastThreadNumber = 0;

	public GridMaster(ILocationsRepository aLocationsRepository) throws RemoteException
	{
		itsLocationsRepository = aLocationsRepository;
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
	
	public int registerNode(RIDatabaseNode aNode)
	{
		itsNodes.add(aNode);
		itsDispatcher.addNode(aNode);
		int theId = itsNodes.size();
		System.out.println("Registered node "+theId);
		return theId;
	}
	
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
	 * Ensures that all buffered data is pushed to the nodes.
	 */
	public void flush()
	{
		itsDispatcher.flush();
	}
	
	/**
	 * Creates a new internal thread id for the given host
	 */
	public synchronized int createThreadId(int aHostId)
	{
		int theId = itsLastThreadNumber++;
		return theId;
	}
	
	public GridThreadInfo getThread(int aHostId, long aThreadId)
	{
		GridEventCollector theCollector = itsCollectors.get(aHostId);
		return theCollector.getThread(aThreadId);
	}

	public int getThreadNumber(int aHostId, long aThreadId)
	{
		return getThread(aHostId, aThreadId).getThreadNumber();
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
	
	public RIQueryAggregator getAggregator()
	{
		return itsAggregator;
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
			try
			{
				long theEventsCount = 0;
				long theFirstTimestamp = Long.MAX_VALUE;
				long theLastTimestamp = 0;
				int theThreadsCount = 0;
				
				for (RIDatabaseNode theNode : itsNodes)
				{
					theEventsCount += theNode.getEventsCount();
					theFirstTimestamp = Math.min(theFirstTimestamp, theNode.getFirstTimestamp());
					theLastTimestamp = Math.max(theLastTimestamp, theNode.getLastTimestamp());
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
			catch (RemoteException e)
			{
				fireException(e);
			}
		}
	}


	
}
