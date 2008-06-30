/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import tod.impl.database.IBidiIterator;
import tod.impl.evdbng.messages.GridEventNG;
import tod.impl.evdbng.queries.EventCondition;

/**
 * Iterator for events of a particular query for a given node.
 * @author gpothier
 */
public class NodeEventIterator extends UnicastRemoteObject 
implements RINodeEventIterator
{
	private EventDatabase itsDatabase;
	private EventCondition itsCondition;
	
	private IBidiIterator<GridEventNG> itsIterator;
	
	public NodeEventIterator(EventDatabase aDatabase, EventCondition aCondition) throws RemoteException
	{
		itsDatabase = aDatabase;
		itsCondition = aCondition;
	}

	public GridEventNG[] next(int aCount)
	{
		List<GridEventNG> theList = new ArrayList<GridEventNG>(aCount);
		for (int i=0;i<aCount;i++)
		{
			if (itsIterator.hasNext()) theList.add(itsIterator.next());
			else break;
		}
		
		return theList.size() > 0 ?
				theList.toArray(new GridEventNG[theList.size()])
				: null;
	}

	public void setNextTimestamp(long aTimestamp)
	{
		itsIterator = itsDatabase.evaluate(itsCondition, aTimestamp);
	}

	public GridEventNG[] previous(int aCount)
	{
		List<GridEventNG> theList = new ArrayList<GridEventNG>(aCount);
		for (int i=0;i<aCount;i++)
		{
			if (itsIterator.hasPrevious()) theList.add(itsIterator.previous());
			else break;
		}
		
		int theSize = theList.size();
		if (theSize == 0) return null;
		
		GridEventNG[] theResult = new GridEventNG[theSize];
		for (int i=0;i<theSize;i++) theResult[i] = theList.get(theSize-i-1);
		
		return theResult;
	}

	public void setPreviousTimestamp(long aTimestamp)
	{
		throw new UnsupportedOperationException();
	}
	

}
