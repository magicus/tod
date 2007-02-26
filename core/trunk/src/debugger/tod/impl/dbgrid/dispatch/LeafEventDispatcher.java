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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import tod.agent.DebugFlags;
import tod.core.ILogCollector;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILocationStore;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.HostInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ThreadInfo;
import tod.core.transport.CollectorLogReceiver;
import tod.core.transport.LogReceiver;
import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridEventCollector;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.db.ObjectsDatabase;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.db.ReorderedObjectsDatabase;
import tod.impl.dbgrid.db.StringIndexer;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.ObjectCodec;
import tod.utils.PrintThroughCollector;
import zz.utils.Utils;

/**
 * A leaf event dispatcher in the dispatching hierarchy.
 * Leaf dispatchers dispatch to database nodes through {@link DBNodeProxy}.
 * @author gpothier
 */
public abstract class LeafEventDispatcher extends AbstractEventDispatcher
implements RILeafDispatcher
{
	
	/**
	 * A leaf dispatcher maintains a local copy of the location
	 * store for efficiency reasons.
	 * The registerer (actually the repository) is needed for
	 * exceptions processing.
	 * If the root dispatcher is a leaf, the location store
	 * is shared with that of the master. Otherwise each leaf dispatcher
	 * has its own store.
	 * @see EventCollector#exception(int, long, short, long, String, String, String, int, Object).
	 */
	private ILocationStore itsLocationStore;
	
	private File itsObjectsDatabaseFile;
	private File itsStringIndexFile;
	
	private List<ReorderedObjectsDatabase> itsObjectsDatabases = 
		new ArrayList<ReorderedObjectsDatabase>();
	
	private StringIndexer itsStringIndexer;
	
	public LeafEventDispatcher(ILocationStore aLocationStore) throws RemoteException
	{
		super(true);
		itsLocationStore = aLocationStore;
		
		String thePrefix = DebuggerGridConfig.NODE_DATA_DIR;
		File theParent = new File(thePrefix);
		System.out.println("Using data directory: "+theParent);
		
		itsObjectsDatabaseFile = new File(theParent, "objects.bin");
		itsStringIndexFile = new File(theParent, "strings");
	}
	
	private void initDatabase()
	{
		for (ObjectsDatabase theDatabase : itsObjectsDatabases)
		{
			if (theDatabase != null) theDatabase.unregister();
		}
		
		itsObjectsDatabases.clear();
		itsObjectsDatabaseFile.delete();
		
		if (getConfig().get(TODConfig.INDEX_STRINGS))
		{
			System.out.println("[LeafEventDispatcher] Creating string indexer");
			itsStringIndexer = new StringIndexer(getConfig(), itsStringIndexFile);
		}
		else
		{
			System.out.println("[LeafEventDispatcher] Not creating string indexer");
			itsStringIndexer = null;
		}
	}
	
	@Override
	public synchronized void clear()
	{
		super.clear();
		initDatabase();
	}
	
	protected DBNodeProxy getNode(int aIndex)
	{
		return (DBNodeProxy) getChild(aIndex);
	}
	
	@Override
	public LogReceiver createLogReceiver(
			HostInfo aHostInfo, 
			GridMaster aMaster,
			InputStream aInStream,
			OutputStream aOutStream, boolean aStartImmediately)
	{
		ILogCollector theCollector = new MyCollector(
				aMaster,
				aHostInfo,
				itsLocationStore,
				this);
		
		if (DebugFlags.COLLECTOR_LOG) theCollector = new PrintThroughCollector(
				aHostInfo,
				theCollector,
				aMaster.getLocationStore());
		
		return new CollectorLogReceiver(
				aHostInfo,
				theCollector,
				itsLocationStore,
				aInStream,
				aOutStream,
				aStartImmediately);
	}

	@Override
	protected void connectToDispatcher(Socket aSocket)
	{
		try
		{
			createLogReceiver(
					null, 
					null, 
					new BufferedInputStream(aSocket.getInputStream()), 
					new BufferedOutputStream(aSocket.getOutputStream()), 
					true);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Dispatches a grid event to the children of this dispatcher 
	 * (nodes or other dispatchers). 
	 */
	public final void dispatchEvent(GridEvent aEvent)
	{
		checkNodeException();
		dispatchEvent0(aEvent);
	}
	
	/**
	 * Dispatches a grid event to the children of this dispatcher 
	 * (nodes or other dispatchers). 
	 */
	protected abstract void dispatchEvent0(GridEvent aEvent);
	
	public void register(long aId, Object aObject)
	{
		long theObjectId = ObjectCodec.getObjectId(aId);
		int theHostId = ObjectCodec.getHostId(aId);
		getObjectsDatabase(theHostId).store(theObjectId, aObject);
		
		if (itsStringIndexer != null && (aObject instanceof String))
		{
			itsStringIndexer.register(aId, (String) aObject);
		}
	}
	
	/**
	 * Retrieves the objects database that stores object for 
	 * the given host id.
	 * @param aHostId A host id, of those embedded in object
	 * ids.
	 */
	private ReorderedObjectsDatabase getObjectsDatabase(int aHostId)
	{
		ReorderedObjectsDatabase theDatabase = null;
		if (aHostId < itsObjectsDatabases.size())
		{
			theDatabase = itsObjectsDatabases.get(aHostId);
		}
		
		if (theDatabase == null)
		{
			theDatabase = new ReorderedObjectsDatabase(itsObjectsDatabaseFile);
			Utils.listSet(itsObjectsDatabases, aHostId, theDatabase);
		}
		
		return theDatabase;
	}
	
	
	@Override
	public synchronized int flush()
	{
		System.out.println("[LeafEventDispatcher] Flushing...");
		
		int theCount = 0;
		for (ReorderedObjectsDatabase theDatabase : itsObjectsDatabases)
		{
			if (theDatabase != null) theCount += theDatabase.flush();
		}
		
		System.out.println("[LeafEventDispatcher] Flushed "+theCount+" objects.");

		return super.flush() + theCount;
	}
	
	public Object getRegisteredObject(long aId) 
	{
		long theObjectId = ObjectCodec.getObjectId(aId);
		int theHostId = ObjectCodec.getHostId(aId);
		return getObjectsDatabase(theHostId).load(theObjectId);
	}

	public RIBufferIterator<StringSearchHit[]> searchStrings(String aText) throws RemoteException
	{
		if (itsStringIndexer != null)
		{
			return new BidiHitIterator(itsStringIndexer.search(aText));
		}
		else return null;
	}



	private static class MyCollector extends GridEventCollector
	{
		private GridMaster itsMaster;
		
		public MyCollector(
				GridMaster aMaster, 
				IHostInfo aHost, 
				ILocationsRepository aLocationsRepository,
				LeafEventDispatcher aDispatcher)
		{
			super(aHost, aLocationsRepository, aDispatcher);
			itsMaster = aMaster;
		}

		@Override
		public void thread(int aThreadId, long aJVMThreadId, String aName)
		{
			ThreadInfo theThread = createThreadInfo(getHost(), aThreadId, aJVMThreadId, aName);
			itsMaster.registerThread(theThread);
		}
	}
	
	private static class BidiHitIterator extends UnicastRemoteObject
	implements RIBufferIterator<StringSearchHit[]>
	{
		private BidiIterator<StringSearchHit> itsIterator;

		public BidiHitIterator(BidiIterator<StringSearchHit> aIterator) throws RemoteException
		{
			itsIterator = aIterator;
		}
		
		public StringSearchHit[] next(int aCount) 
		{
			StringSearchHit[] theArray = new StringSearchHit[aCount];
			
			int theCount = 0;
			for (int i=0;i<aCount;i++)
			{
				if (itsIterator.hasNext()) 
				{
					theArray[i] = itsIterator.next();
					theCount++;
				}
				else break;
			}
			
			if (theCount == aCount)
			{
				return theArray;
			}
			else if (theCount > 0)
			{
				StringSearchHit[] theResult = new StringSearchHit[theCount];
				System.arraycopy(theArray, 0, theResult, 0, theCount);
				return theResult;
			}
			else return null;
		}

		public StringSearchHit[] previous(int aCount)
		{
			StringSearchHit[] theArray = new StringSearchHit[aCount];
			
			int theCount = 0;
			for (int i=aCount-1;i>=0;i--)
			{
				if (itsIterator.hasPrevious()) 
				{
					theArray[i] = itsIterator.previous();
					theCount++;
				}
				else break;
			}
			
			if (theCount == aCount)
			{
				return theArray;
			}
			else if (theCount > 0)
			{
				StringSearchHit[] theResult = new StringSearchHit[theCount];
				System.arraycopy(
						theArray, 
						aCount-theCount, 
						theResult, 
						0, 
						theCount);
				
				return theResult;
			}
			else return null;
		}
	}
}
