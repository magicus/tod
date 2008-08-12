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

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import tod.core.DebugFlags;
import tod.core.ILogCollector;
import tod.core.config.TODConfig;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.ObjectId;
import tod.core.transport.ValueReader;
import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.IGridEventFilter;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.dispatch.RINodeConnector.StringSearchHit;
import tod.impl.dbgrid.messages.GridEvent;
import tod.utils.remote.RemoteStructureDatabase;
import zz.utils.Utils;

/**
 * Performs the indexing of events and handles queries for a single database node.
 * 
 * @author gpothier
 */
public abstract class DatabaseNode 
{
//	private static final ReceiverThread NODE_THREAD = new ReceiverThread();
	
	private RIGridMaster itsMaster;
	private int itsNodeId;
	private TODConfig itsConfig;
	
	private long itsEventsCount = 0;
	private long itsFirstTimestamp = 0;
	private long itsLastTimestamp = 0;
	
	/**
	 * This flag permits to handle the canceling of flushes.
	 */
	private FlushMonitor itsFlushMonitor;
	
	/**
	 * The database node needs the structure database for the following:
	 * <li> Exception resolving
	 * (see EventCollector#exception(int, long, short, long, String, String, String, int, Object)).
	 * <li> Finding location of events.
	 */
	private IMutableStructureDatabase itsStructureDatabase;
	
	private EventDatabase itsEventsDatabase;
	private File itsRootDirectory;
	
	/**
	 * Per-host object databases.
	 */
	private List<ObjectsDatabase> itsObjectsDatabases = new ArrayList<ObjectsDatabase>();
	
	private StringIndexer itsStringIndexer;
	
	private FlusherThread itsFlusherThread = new FlusherThread();

	public DatabaseNode() 
	{
		String thePrefix = DebuggerGridConfig.NODE_DATA_DIR;
		itsRootDirectory = new File(thePrefix);
		System.out.println("Using data directory: "+itsRootDirectory);
	}
	
	public TODConfig getConfig()
	{
		return itsConfig;
	}
	
	public void setConfig(TODConfig aConfig)
	{
		itsConfig = aConfig;
	}
	
	public void connectedToMaster(RIGridMaster aMaster, int aNodeId)
	{
		itsMaster = aMaster;
		itsNodeId = aNodeId;
		
		if (itsMaster instanceof GridMaster)
		{
			// This is the case where the master is local.
			GridMaster theLocalMaster = (GridMaster) itsMaster;
			itsStructureDatabase = theLocalMaster.getStructureDatabase();
			itsConfig = theLocalMaster.getConfig();
		}
		else
		{
			try
			{
				itsStructureDatabase = 
					RemoteStructureDatabase.createMutableDatabase(itsMaster.getRemoteStructureDatabase());
				
				itsConfig = itsMaster.getConfig();
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}
		initDatabase();
	}
	
	protected synchronized void initDatabase()
	{
		if (itsEventsDatabase != null)
		{
			itsEventsDatabase.dispose();
			
			// We detach the database so that its space can be reclaimed while
			// we create the new one.
			itsEventsDatabase = null; 
		}
		
		// Init events database
		String thePrefix = DebuggerGridConfig.NODE_DATA_DIR;
		File theParent = new File(thePrefix);
		System.out.println("Using data directory: "+theParent);
		
		itsEventsDatabase = createEventDatabase(theParent);		

		// Init objects database
		for (ObjectsDatabase theDatabase : itsObjectsDatabases)
		{
			if (theDatabase != null) theDatabase.dispose();
		}
		
		itsObjectsDatabases.clear();
		
		if (getConfig().get(TODConfig.INDEX_STRINGS))
		{
			System.out.println("[LeafEventDispatcher] Creating string indexer");
			itsStringIndexer = new StringIndexer(getConfig(), new File(itsRootDirectory, "strings.bin"));
		}
		else
		{
			System.out.println("[LeafEventDispatcher] Not creating string indexer");
			itsStringIndexer = null;
		}
	}
	
	/**
	 * Creates an event database in the given directory. 
	 */
	protected abstract EventDatabase createEventDatabase(File aDirectory);
	
	/**
	 * Returns the structure database used by this node.
	 */
	public IMutableStructureDatabase getStructureDatabase()
	{
		return itsStructureDatabase;
	}

	public int getNodeId()
	{
		return itsNodeId;
	}
	
	public RIGridMaster getMaster()
	{
		return itsMaster;
	}
	
	public void clear()
	{
		itsEventsCount = 0;
		itsFirstTimestamp = 0;
		itsLastTimestamp = 0;
		
		initDatabase();
	}
	
	public int flush()
	{
		return flush(false); 
	}
	
	public synchronized int flush(boolean aCancellable)
	{
		if (aCancellable) itsFlushMonitor = new FlushMonitor();
		int theObjectsCount = 0;
		
		System.out.println("[DatabaseNode] Flushing...");
		
		// Flush objects database
		for (ObjectsDatabase theDatabase : itsObjectsDatabases)
		{
			if (theDatabase != null) theObjectsCount += theDatabase.flush(itsFlushMonitor);
		}
		
		System.out.println("[DatabaseNode] Flushed "+theObjectsCount+" objects");

		// Flush events database
		int theEventsCount = itsEventsDatabase.flush(itsFlushMonitor);
		
		System.out.println("[DatabaseNode] Flushed "+theEventsCount+" events");
		
		itsFlushMonitor = null;
		
		return theObjectsCount+theEventsCount;
	}
	
	public synchronized void flushOldestEvent()
	{
		itsEventsDatabase.flushOldestEvent();
	}
	
	public long[] getEventCounts(
			IGridEventFilter aCondition, 
			long aT1, 
			long aT2,
			int aSlotsCount,
			boolean aForceMergeCounts) 
	{
		return itsEventsDatabase.getEventCounts(
				aCondition, 
				aT1, 
				aT2, 
				aSlotsCount,
				aForceMergeCounts);
	}

	public RINodeEventIterator getIterator(IGridEventFilter aCondition) throws RemoteException 
	{
		return itsEventsDatabase.getIterator(aCondition);
	}
	
	/**
	 * Adds an event to the database.
	 */
	public void pushEvent(GridEvent aEvent)
	{
		// Cancel flushing if events are pushed at the same time.
		FlushMonitor theFlushMonitor = itsFlushMonitor;
		if (theFlushMonitor != null) theFlushMonitor.cancel(); // Must be outside the lock.
		
		synchronized (this)
		{
			// The GridEventCollector uses a pool of events
			// we cannot hold references to those events
			aEvent = (GridEvent) aEvent.clone(); 
			itsEventsDatabase.push(aEvent);

			long theTimestamp = aEvent.getTimestamp();
			itsEventsCount++;
			
			// The following code is a bit faster than using min & max
			// (Pentium M 2ghz)
			if (itsFirstTimestamp == 0) itsFirstTimestamp = theTimestamp;
			if (itsLastTimestamp < theTimestamp) itsLastTimestamp = theTimestamp;			
		}
		
		// This must be outside the lock otherwise it might deadlock.
		itsFlusherThread.active();
	}
	
	public long getEventsCount()
	{
		return itsEventsCount;
	}

	public long getFirstTimestamp()
	{
		return itsFirstTimestamp;
	}

	public long getLastTimestamp()
	{
		return itsLastTimestamp;
	}

	public abstract ILogCollector createLogCollector(IHostInfo aHostInfo);

	public void register(long aId, byte[] aData, long aTimestamp, boolean aIndexable)
	{
		if (DebugFlags.SKIP_OBJECTS) return;
		
		long theObjectId = ObjectId.getObjectId(aId);
		int theHostId = ObjectId.getHostId(aId);
		getObjectsDatabase(theHostId).store(theObjectId, aData, aTimestamp);
		
		if (itsStringIndexer != null && aIndexable)
		{
			Object theObject = ValueReader.readRegistered(aData);
			if (theObject instanceof String)
			{
				String theString = (String) theObject;
				itsStringIndexer.register(aId, theString);
			}
			else throw new UnsupportedOperationException("Not handled: "+theObject);
		}
	}
	
	/**
	 * Retrieves the objects database that stores object for 
	 * the given host id.
	 * @param aHostId A host id, of those embedded in object
	 * ids.
	 */
	private ObjectsDatabase getObjectsDatabase(int aHostId)
	{
		ObjectsDatabase theDatabase = null;
		if (aHostId < itsObjectsDatabases.size())
		{
			theDatabase = itsObjectsDatabases.get(aHostId);
		}
		
		if (theDatabase == null)
		{
			theDatabase = createObjectsDatabase(itsRootDirectory, "h"+aHostId);
			Utils.listSet(itsObjectsDatabases, aHostId, theDatabase);
		}
		
		return theDatabase;
	}
	
	/**
	 * Creates an object database in the given directory.
	 * As there can be several object databases (one per host), and additional name
	 * is given to differentiate them.
	 */
	protected abstract ObjectsDatabase createObjectsDatabase(File aDirectory, String aName);
	
	public Object getRegisteredObject(long aId) 
	{
		if (DebugFlags.SKIP_OBJECTS) return null;
		
		long theObjectId = ObjectId.getObjectId(aId);
		int theHostId = ObjectId.getHostId(aId);
		ObjectsDatabase theObjectsDatabase = getObjectsDatabase(theHostId);
		return theObjectsDatabase != null ? theObjectsDatabase.load(theObjectId) : null;
	}

	public RIBufferIterator<StringSearchHit[]> searchStrings(String aText) throws RemoteException 
	{
		if (itsStringIndexer != null)
		{
			return new BidiHitIterator(itsStringIndexer.search(aText));
		}
		else return null;
	}

	private static class BidiHitIterator extends UnicastRemoteObject
	implements RIBufferIterator<StringSearchHit[]>
	{
		private IBidiIterator<StringSearchHit> itsIterator;

		public BidiHitIterator(IBidiIterator<StringSearchHit> aIterator) throws RemoteException
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
	
	
	/**
	 * This thread flushes the database when no event has been added
	 * for some period of time.
	 * @author gpothier
	 */
	private class FlusherThread extends Thread
	{
		private boolean itsActive = false;
		private boolean itsFlushed = true;
		
		public FlusherThread()
		{
			super("FlusherThread");
			start();
		}
		
		/**
		 * Notifies the thread that event recording is active,
		 * and therefore flushing should be postponed.
		 */
		public synchronized void active()
		{
			itsActive = true;
			itsFlushed = false;
		}
		
		@Override
		public synchronized void run()
		{
			try
			{
				while(true)
				{
					wait(2000);

					if (! itsActive)
					{
						if (! itsFlushed)
						{
							System.out.println("[FlusherThread] Performing full flush...");
							flush(true);
							System.out.println("[FlusherThread] Full flush done.");
							itsFlushed = true;
						}
					}
					else
					{
						// Flush oldest event if the newest was created more than 2s after
						int theCount = 0;
						while (itsEventsDatabase.isNextEventFlushable(2000000000)) 
						{
							flushOldestEvent();
							theCount++;
						}
						
						// Flush oldest object if the newest was created more than 2s after
						for (ObjectsDatabase theDatabase : itsObjectsDatabases)
						{
							if (theDatabase != null) while (theDatabase.isNextEventFlushable(2000000000)) 
							{
								theDatabase.flushOldestEvent();
								theCount++;
							}
						}	
						
						System.out.println("Flushed "+theCount+" events and  objects older than 2s");
					}
					
					itsActive = false;
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * This class permits to cancel a flushing operation if 
	 * a push is attempted during the flush.
	 * @author gpothier
	 */
	public static class FlushMonitor
	{
		private boolean itsCancelled = false;
		
		public boolean isCancelled()
		{
			return itsCancelled;
		}
		
		public void cancel()
		{
			itsCancelled = true;
		}
	}
}
