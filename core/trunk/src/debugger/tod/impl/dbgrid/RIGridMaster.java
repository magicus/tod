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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.impl.dbgrid.aggregator.QueryAggregator;
import tod.impl.dbgrid.aggregator.RIQueryAggregator;
import tod.impl.dbgrid.db.NodeRejectedException;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.dispatch.RIDispatchNode;
import tod.impl.dbgrid.dispatch.RILeafDispatcher.StringSearchHit;
import tod.impl.dbgrid.dispatch.tree.DispatchTreeStructure;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.remote.RILocationsRepository;
import zz.utils.ITask;

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
	 * Returns the configuration of this master.
	 */
	public TODConfig getConfig() throws RemoteException;
	
	/**
	 * Returns the configuration of this master.
	 */
	public void setConfig(TODConfig aConfig) throws RemoteException;
	
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
	 * Ensures that all buffered data is pushed to the nodes.
	 */
	public void flush() throws RemoteException;
	
	/**
	 * Disconnects from currently connected debuggees.
	 */
	public void disconnect() throws RemoteException;

	
	/**
	 * Nodes can call this method to determine the current needs
	 * of the dispatcher: database nodes, leaf dispatcher nodes
	 * or internal dispatcher nodes.
	 * Client: undetermined nodes.
	 * @param aHostName The name of the host on which the node executes.
	 */
	public DispatchTreeStructure.NodeRole getRoleForNode(String aHostName) throws RemoteException;
	
	/**
	 * Registers a node so that it can be used by the grid.
	 * Client: dispatch nodes
	 * @throws NodeRejectedException Thrown if the master refuses the new node
	 * @return The id assigned to the node.
	 */
	public String registerNode(RIDispatchNode aNode, String aHostname) 
		throws RemoteException, NodeRejectedException;
	
	/**
	 * Dispatch nodes call this method when they encounter an exception.
	 * Client: dispatch nodes
	 */
	public void nodeException(NodeException aException) throws RemoteException;
	
	/**
	 * Dispatch nodes can periodically send monitoring data.
	 * Client: dispatch nodes
	 */
	public void pushMonitorData(String aNodeId, MonitorData aData) throws RemoteException;

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

	/**
	 * Returns an object registered by the database, or null
	 * if not found.
	 */
	public Object getRegisteredObject(long aId) throws RemoteException;

	/**
	 * See {@link ILogBrowser#exec(ITask)}
	 */
	public <O> O exec(ITask<ILogBrowser, O> aTask) throws RemoteException;
	
	/**
	 * Searches a text in the registered strings.
	 * @return An iterator that returns matching strings in order of relevance.
	 */
	public RIBufferIterator<StringSearchHit[]> searchStrings(String aSearchText) throws RemoteException;

}
