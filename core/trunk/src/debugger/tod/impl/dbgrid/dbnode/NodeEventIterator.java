/*
 * Created on Sep 11, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;

/**
 * Iterator for events of a particular query for a given node.
 * @author gpothier
 */
public class NodeEventIterator extends UnicastRemoteObject 
implements RINodeEventIterator
{
	private DatabaseNode itsNode;
	private EventCondition itsCondition;
	
	private Iterator<GridEvent> itsIterator;
	
	public NodeEventIterator(DatabaseNode aNode, EventCondition aCondition) throws RemoteException
	{
		itsNode = aNode;
		itsCondition = aCondition;
	}

	public GridEvent[] next(int aCount)
	{
		List<GridEvent> theList = new ArrayList<GridEvent>(aCount);
		for (int i=0;i<aCount;i++)
		{
			if (itsIterator.hasNext()) theList.add(itsIterator.next());
			else break;
		}
		
		return theList.size() > 0 ?
				theList.toArray(new GridEvent[theList.size()])
				: null;
	}

	public void setNextTimestamp(long aTimestamp)
	{
		itsIterator = itsNode.evaluate(itsCondition, aTimestamp);
	}

	public GridEvent[] previous(int aCount)
	{
		throw new UnsupportedOperationException();
	}

	public void setPreviousTimestamp(long aTimestamp)
	{
		throw new UnsupportedOperationException();
	}
	

}
