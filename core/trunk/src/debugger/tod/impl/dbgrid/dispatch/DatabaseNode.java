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
import tod.core.LocationRegisterer;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILocationStore;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.HostInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ThreadInfo;
import tod.core.transport.CollectorLogReceiver;
import tod.core.transport.LogReceiver;
import tod.core.transport.LogReceiver.ReceiverThread;
import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridEventCollector;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.db.EventDatabase;
import tod.impl.dbgrid.db.ObjectsDatabase;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.db.RINodeEventIterator;
import tod.impl.dbgrid.db.ReorderedObjectsDatabase;
import tod.impl.dbgrid.db.StringIndexer;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.ObjectCodec;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.PrintThroughCollector;
import zz.utils.Utils;

public class DatabaseNode extends AbstractDispatchNode
implements RIDatabaseNode
{
//	private static final ReceiverThread NODE_THREAD = new ReceiverThread();
	
	private long itsEventsCount = 0;
	private long itsFirstTimestamp = 0;
	private long itsLastTimestamp = 0;
	
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
	
	private EventDatabase itsEventsDatabase;
	private File itsObjectsDatabaseFile;
	private File itsStringIndexFile;
	
	private List<ReorderedObjectsDatabase> itsObjectsDatabases = 
		new ArrayList<ReorderedObjectsDatabase>();
	
	private StringIndexer itsStringIndexer;

	public DatabaseNode() throws RemoteException
	{
		itsLocationStore = new LocationRegisterer();
		
		String thePrefix = DebuggerGridConfig.NODE_DATA_DIR;
		File theParent = new File(thePrefix);
		System.out.println("Using data directory: "+theParent);
		
		itsObjectsDatabaseFile = new File(theParent, "objects.bin");
		itsStringIndexFile = new File(theParent, "strings");

	}
	
	@Override
	protected void connectedToMaster()
	{
		super.connectedToMaster();
		initDatabase();
	}
	
	private void initDatabase()
	{
		// Init events database
		if (itsEventsDatabase != null)
		{
			itsEventsDatabase.unregister();
		}
		
		String thePrefix = DebuggerGridConfig.NODE_DATA_DIR;
		File theParent = new File(thePrefix);
		System.out.println("Using data directory: "+theParent);
		
		File theFile = new File(theParent, "events.bin");
		theFile.delete();
		itsEventsDatabase = createDatabase(theFile);		

		// Init objects database
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
	
	protected EventDatabase createDatabase(File aFile)
	{
		int theNodeIndex = Integer.parseInt(getNodeId().substring(3));
		return new EventDatabase(theNodeIndex, aFile);
	}

	
	@Override
	public void clear()
	{
		itsEventsCount = 0;
		itsFirstTimestamp = 0;
		itsLastTimestamp = 0;
		
		initDatabase();
	}
	
	@Override
	public int flush()
	{
		int theObjectsCount = 0;
		
		System.out.println("[DatabaseNode] Flushing...");
		
		for (ReorderedObjectsDatabase theDatabase : itsObjectsDatabases)
		{
			if (theDatabase != null) theObjectsCount += theDatabase.flush();
		}
		
		System.out.println("[DatabaseNode] Flushed "+theObjectsCount+" objects");

		int theEventsCount = itsEventsDatabase.flush();
		
		System.out.println("[DatabaseNode] Flushed "+theEventsCount+" events");
		
		return theObjectsCount+theEventsCount;
	}
	
	public long[] getEventCounts(
			EventCondition aCondition, 
			long aT1, 
			long aT2,
			int aSlotsCount,
			boolean aForceMergeCounts) throws RemoteException
	{
		return itsEventsDatabase.getEventCounts(
				aCondition, 
				aT1, 
				aT2, 
				aSlotsCount,
				aForceMergeCounts);
	}

	public RINodeEventIterator getIterator(EventCondition aCondition) throws RemoteException
	{
		return itsEventsDatabase.getIterator(aCondition);
	}
	
	public void pushEvent(GridEvent aEvent)
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

	@Override
	public LogReceiver createLogReceiver(
			HostInfo aHostInfo, 
			GridMaster aMaster,
			InputStream aInStream,
			OutputStream aOutStream, 
			boolean aStartImmediately)
	{
		System.out.println("[DatabaseNode] Creating LogReceiver");
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
//				NODE_THREAD,
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
			connectToLocalDispatcher(
					new BufferedInputStream(aSocket.getInputStream()),
					new BufferedOutputStream(aSocket.getOutputStream()));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void connectToLocalDispatcher(
			InputStream aInputStream,
			OutputStream aOutputStream)
	{
		connectToDispatcher(aInputStream, aOutputStream);
	}
	
	private void connectToDispatcher(
			InputStream aInputStream,
			OutputStream aOutputStream)
	{
		createLogReceiver(
				new HostInfo(0, null), // TODO: check this, do we correspond to a particular host? 
				null, 
				aInputStream, 
				aOutputStream, 
				true);
	}
	

	public void register(long aId, Object aObject)
	{
		if (DebugFlags.SKIP_OBJECTS) return;
		
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
	
	public Object getRegisteredObject(long aId) 
	{
		if (DebugFlags.SKIP_OBJECTS) return null;
		
		long theObjectId = ObjectCodec.getObjectId(aId);
		int theHostId = ObjectCodec.getHostId(aId);
		ReorderedObjectsDatabase theObjectsDatabase = getObjectsDatabase(theHostId);
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
	
	private static class MyCollector extends GridEventCollector
	{
		private GridMaster itsMaster;
		
		public MyCollector(
				GridMaster aMaster, 
				IHostInfo aHost, 
				ILocationsRepository aLocationsRepository,
				DatabaseNode aNode)
		{
			super(aHost, aLocationsRepository, aNode);
			itsMaster = aMaster;
		}

		@Override
		public void thread(int aThreadId, long aJVMThreadId, String aName)
		{
			ThreadInfo theThread = createThreadInfo(getHost(), aThreadId, aJVMThreadId, aName);
			itsMaster.registerThread(theThread);
		}
	}
	

}
