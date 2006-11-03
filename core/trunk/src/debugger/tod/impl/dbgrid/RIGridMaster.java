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
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dbnode.NodeRejectedException;
import tod.impl.dbgrid.dbnode.RIDatabaseNode;
import tod.impl.dbgrid.monitoring.Monitor.KeyMonitorData;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;
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
	 * Client: frontend
	 */
	public void addListener(RIGridMasterListener aListener) throws RemoteException;
	
	/**
	 * Clears the database managed by this master.
	 * Client: frontend
	 */
	public void clear() throws RemoteException;
	
	/**
	 * Registers a node so that it can be used by the grid.
	 * Client: database nodes
	 * @throws NodeRejectedException Thrown if the master refuses the new node
	 * @return The id assigned to the node.
	 */
	public int registerNode(RIDatabaseNode aNode, String aHostname) throws RemoteException, NodeRejectedException;
	
	/**
	 * {@link DatabaseNode}s call this method when they encounter an exception.
	 * Client: database nodes
	 */
	public void nodeException(NodeException aException) throws RemoteException;
	
	/**
	 * Database nodes can periodically send monitoring data.
	 * Client: database nodes
	 */
	public void pushMonitorData(int aNodeId, MonitorData aData) throws RemoteException;

	/**
	 * Returns a new query aggregator for the specified query
	 * Client: frontend 
	 */
	public RIQueryAggregator createAggregator(EventCondition aCondition) throws RemoteException;
	
	/**
	 * Returns the internal thread id corresponding to the given thread
	 * on the given host.
	 * Client: frontend 
	 */
	public IThreadInfo getThread(int aHostId, long aThreadId) throws RemoteException;
	
	/**
	 * Returns all the threads registered during the execution of the
	 * debugged program.
	 * Client: frontend 
	 */
	public List<IThreadInfo> getThreads() throws RemoteException;
	
	/**
	 * Returns all the hosts registered during the execution of the
	 * debugged program.
	 * Client: frontend 
	 */
	public List<IHostInfo> getHosts() throws RemoteException;
	
	/**
	 * Returns the number of events stored by the nodes of this master.
	 * Client: frontend 
	 */
	public long getEventsCount() throws RemoteException;
	
	/**
	 * Returns the timestamp of the first event recorded in this log.
	 * Client: frontend 
	 */
	public long getFirstTimestamp() throws RemoteException;
	
	/**
	 * Returns the timestamp of the last event recorded in this log.
	 * Client: frontend 
	 */
	public long getLastTimestamp() throws RemoteException;
	
	/**
	 * Returns a remote locations repository.
	 * Client: frontend 
	 */
	public RILocationsRepository getLocationsRepository() throws RemoteException;

}
