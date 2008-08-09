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
package tod.impl.dbgrid.db;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.IGridEventFilter;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Iterator for events of a particular query for a given node.
 * @author gpothier
 */
public class NodeEventIterator extends UnicastRemoteObject 
implements RINodeEventIterator
{
	private EventDatabase itsDatabase;
	private IGridEventFilter itsFilter;
	
	private IBidiIterator<GridEvent> itsIterator;
	
	public NodeEventIterator(EventDatabase aDatabase, IGridEventFilter aFilter) throws RemoteException
	{
		itsDatabase = aDatabase;
		itsFilter = aFilter;
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
		itsIterator = itsDatabase.evaluate(itsFilter, aTimestamp);
	}

	public GridEvent[] previous(int aCount)
	{
		List<GridEvent> theList = new ArrayList<GridEvent>(aCount);
		for (int i=0;i<aCount;i++)
		{
			if (itsIterator.hasPrevious()) theList.add(itsIterator.previous());
			else break;
		}
		
		int theSize = theList.size();
		if (theSize == 0) return null;
		
		GridEvent[] theResult = new GridEvent[theSize];
		for (int i=0;i<theSize;i++) theResult[i] = theList.get(theSize-i-1);
		
		return theResult;
	}

	public void setPreviousTimestamp(long aTimestamp)
	{
		throw new UnsupportedOperationException();
	}
	

}
