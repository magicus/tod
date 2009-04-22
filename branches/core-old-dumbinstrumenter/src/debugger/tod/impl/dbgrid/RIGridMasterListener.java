/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid;

import java.rmi.Remote;
import java.rmi.RemoteException;

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
}
