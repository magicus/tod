/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import tod.impl.dbgrid.messages.GridMessage;

/**
 * Remote interface for {@link DatabaseNode}
 * @author gpothier
 */
public interface RIDatabaseNode extends Remote
{
	/**
	 * Pushes a list of messages to this node.
	 * @see #push(GridMessage) 
	 */
	public void push(GridMessage[] aMessages) throws RemoteException;

	/**
	 * Flushes the event buffer. Events should not be added
	 * after this method is called.
	 */
	public void flush() throws RemoteException;
	
	/**
	 * Returns the number of events stored by this node
	 */
	public long getEventsCount() throws RemoteException;
	
	/**
	 * Returns the timestamp of the first event recorded in this node.
	 */
	public long getFirstTimestamp() throws RemoteException;
	
	/**
	 * Returns the timestamp of the last event recorded in this node.
	 */
	public long getLastTimestamp() throws RemoteException;



}
