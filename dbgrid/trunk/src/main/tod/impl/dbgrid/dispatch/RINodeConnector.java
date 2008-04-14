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
package tod.impl.dbgrid.dispatch;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.structure.IHostInfo;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.db.RINodeEventIterator;
import tod.impl.dbgrid.queries.EventCondition;

import zz.utils.net.Server.ServerAdress;

/**
 * Remote interface for {@link NodeConnector}
 * @author gpothier
 */
public interface RINodeConnector extends Remote
{
	public int getNodeId() throws RemoteException;
	
	/**
	 * Tells this node to establish its incoming data connection to
	 * the grid master at the specified adress.
	 */
	public void connectEventStream(ServerAdress aAdress, IHostInfo aHostInfo) throws RemoteException;

	/**
	 * Flushes currently bufferred events.
	 * @return Number of flushed events.
	 */
	public int flush() throws RemoteException;
	
	/**
	 * Initializes or reinitializes the database.
	 */
	public void clear() throws RemoteException;
	
	/**
	 * Creates a new event iterator for the given condition.
	 */
	public RINodeEventIterator getIterator(EventCondition aCondition) throws RemoteException;
	
	/**
	 * Semantic matches {@link IEventBrowser#getEventCounts(long, long, int)}
	 */
	public long[] getEventCounts(
			EventCondition aCondition,
			long aT1,
			long aT2,
			int aSlotsCount, 
			boolean aForceMergeCounts) throws RemoteException;
	
	public long getEventsCount() throws RemoteException;
	public long getFirstTimestamp() throws RemoteException;
	public long getLastTimestamp() throws RemoteException;
	
	/**
	 * Returns an object registered by this dispatcher, or null
	 * if not found.
	 */
	public Object getRegisteredObject(long aId) throws RemoteException;
	
	/**
	 * Searches the strings that match the given text.
	 * Returns an iterator of object ids of matching strings, ordered
	 * by relevance.
	 */
	public RIBufferIterator<StringSearchHit[]> searchStrings(String aText) throws RemoteException;
	
	/**
	 * Represents a search hit.
	 * @author gpothier
	 */
	public static class StringSearchHit implements Serializable
	{
		private static final long serialVersionUID = 6477792385168896074L;
		private long itsObjectId;
		private long itsScore;
		
		public StringSearchHit(long aObjectId, long aScore)
		{
			itsObjectId = aObjectId;
			itsScore = aScore;
		}

		public long getObjectId()
		{
			return itsObjectId;
		}

		public long getScore()
		{
			return itsScore;
		}
		
		@Override
		public String toString()
		{
			return "Hit: "+itsObjectId+" ("+itsScore+")";
		}
	}

	
}
