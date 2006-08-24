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
	public void push(List<GridMessage> aMessagesList) throws RemoteException;

	/**
	 * Flushes the event buffer. Events should not be added
	 * after this method is called.
	 */
	public void flush() throws RemoteException;

}
