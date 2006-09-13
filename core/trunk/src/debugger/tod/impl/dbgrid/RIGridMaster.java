/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.impl.dbgrid.aggregator.QueryAggregator;
import tod.impl.dbgrid.aggregator.RIQueryAggregator;
import tod.impl.dbgrid.dbnode.RIDatabaseNode;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.remote.RILocationsRepository;

/**
 * Remote interface of the grid master.
 * This is the entry point to the database grid.
 * Manages configuration and discovery of database nodes,
 * acts as a factory for {@link GridEventCollector}s
 * and {@link QueryAggregator}.
 * @author gpothier
 */
public interface RIGridMaster extends Remote
{
	/**
	 * Adds a listener to this master.
	 */
	public void addListener(RIGridMasterListener aListener) throws RemoteException;
	
	/**
	 * Registers a node so that it can be used by the grid.
	 */
	public void registerNode(RIDatabaseNode aNode) throws RemoteException;
	

	/**
	 * Returns a new query aggregator for the specified query 
	 */
	public RIQueryAggregator createAggregator(EventCondition aCondition) throws RemoteException;
	
	/**
	 * Returns the internal thread id corresponding to the given thread
	 * on the given host.
	 */
	public IThreadInfo getThread(int aHostId, long aThreadId) throws RemoteException;
	
	/**
	 * Returns all the threads registered during the execution of the
	 * debugged program.
	 */
	public List<IThreadInfo> getThreads() throws RemoteException;
	
	/**
	 * Returns all the hosts registered during the execution of the
	 * debugged program.
	 */
	public List<IHostInfo> getHosts() throws RemoteException;
	
	/**
	 * Returns the number of events stored by the nodes of this master.
	 */
	public long getEventsCount() throws RemoteException;
	
	/**
	 * Returns the timestamp of the first event recorded in this log.
	 */
	public long getFirstTimestamp() throws RemoteException;
	
	/**
	 * Returns the timestamp of the last event recorded in this log.
	 */
	public long getLastTimestamp() throws RemoteException;
	
	/**
	 * Returns a remote locations repository.
	 */
	public RILocationsRepository getLocationsRepository() throws RemoteException;

}
