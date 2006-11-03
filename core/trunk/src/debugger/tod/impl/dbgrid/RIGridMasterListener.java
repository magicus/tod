/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import tod.impl.dbgrid.monitoring.Monitor.KeyMonitorData;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;

/**
 * A remote interface for a listener of {@link GridMaster}
 * @author gpothier
 */
public interface RIGridMasterListener extends Remote
{
	/**
	 * Called asynchronously after one ore more events are received.
	 * This method will not be called at short intervals, there will be
	 * typically at least one second between calls.
	 */
	public void eventsReceived() throws RemoteException;
	
	/**
	 * Called when an exception occurred in the grid.
	 */
	public void exception(Throwable aThrowable) throws RemoteException;
	
	/**
	 * Called when new monitoring info has been received from a database node
	 */
	public void monitorData(int aNodeId, MonitorData aData) throws RemoteException;
}
