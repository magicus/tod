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
package tod.impl.dbgrid;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.impl.dbgrid.aggregator.QueryAggregator;
import tod.impl.dbgrid.aggregator.RIQueryAggregator;
import tod.impl.dbgrid.db.NodeRejectedException;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.dispatch.RINodeConnector;
import tod.impl.dbgrid.dispatch.RINodeConnector.StringSearchHit;
import tod.tools.monitoring.RIMonitoringServerProvider;
import tod.tools.monitoring.RemoteLinker;
import tod.utils.remote.RIStructureDatabase;
import zz.utils.ITask;
import zz.utils.monitoring.Monitor.MonitorData;

/**
 * Remote interface of the grid master.
 * This is the entry point to the database grid.
 * Manages configuration and discovery of database nodes,
 * acts as a factory for {@link GridEventCollector}s
 * and {@link QueryAggregator}.
 * @author gpothier
 */
public interface RIGridMaster extends Remote, RIMonitoringServerProvider
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

	public void removeListener(RIGridMasterListener aListener) throws RemoteException;
	
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
	 * Tells the agent to enable/disable trace capture.
	 */
	public void sendEnableCapture(boolean aEnable) throws RemoteException;

	/**
	 * Registers a node so that it can be used by the grid.
	 * @throws NodeRejectedException Thrown if the master refuses the new node
	 * @return The id assigned to the node.
	 */
	public int registerNode(RINodeConnector aNode, String aHostname) 
		throws RemoteException, NodeRejectedException;
	
	/**
	 * Dispatch nodes call this method when they encounter an exception.
	 * Client: dispatch nodes
	 */
	public void nodeException(NodeException aException) throws RemoteException;
	
	/**
	 * Dispatch nodes can periodically send monitoring data.
	 * Client: nodes
	 */
	public void pushMonitorData(int aNodeId, MonitorData aData) throws RemoteException;

	/**
	 * Returns a new query aggregator for the specified query
	 * Client: frontend 
	 */
	@RemoteLinker
	public RIQueryAggregator createAggregator(IGridEventFilter aFilter) throws RemoteException;
		
	/**
	 * Returns all the threads registered during the execution of the
	 * debugged program, in no particular order.
	 * Client: frontend 
	 */
	public List<IThreadInfo> getThreads() throws RemoteException;
	
	/**
	 * Returns all the hosts registered during the execution of the
	 * debugged program, in no particular order.
	 * Client: frontend 
	 */
	public List<IHostInfo> getHosts() throws RemoteException;
	
	/**
	 * Returns the number of events stored by the nodes of this master.
	 * Client: frontend 
	 */
	public long getEventsCount() throws RemoteException;
	
	/**
	 * Returns the number of dropped events.
	 * Client: frontend 
	 */
	public long getDroppedEventsCount() throws RemoteException;
	
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
	@RemoteLinker
	public RIStructureDatabase getRemoteStructureDatabase() throws RemoteException;

	/**
	 * Returns an object registered by the database, or null
	 * if not found.
	 */
	public Object getRegisteredObject(long aId) throws RemoteException;
	
	/**
	 * Returns the type of an object registered by the database.
	 */
	public ITypeInfo getObjectType(long aId) throws RemoteException;

	/**
	 * See {@link ILogBrowser#exec(ITask)}
	 */
	public <O> O exec(ITask<ILogBrowser, O> aTask) throws RemoteException;
	
	/**
	 * Searches a text in the registered strings.
	 * @return An iterator that returns matching strings in order of relevance.
	 */
	@RemoteLinker
	public RIBufferIterator<StringSearchHit[]> searchStrings(String aSearchText) throws RemoteException;

	/**
	 * If the {@link TODConfig#MASTER_TIMEOUT} configuration attribute is set,
	 * the clients must periodically call this method to prevent the grid master from
	 * shutting down.
	 */
	public void keepAlive() throws RemoteException;

	/**
	 * Returns the id of the specified behavior (for exception processing).
	 * This metehod is called by the database nodes' exception resolver.
	 * @see NodeExceptionResolver 
	 */
	public int getBehaviorId(String aClassName, String aMethodName, String aMethodSignature) throws RemoteException;
	
	/**
	 * Returns the number of events that occurred within the given behavior.
	 */
	public long getEventCountAtBehavior(int aBehaviorId) throws RemoteException;

	/**
	 * Returns the number of events that occurred within the given class.
	 */
	public long getEventCountAtClass(int aClassId) throws RemoteException;
}
