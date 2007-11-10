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
import tod.core.database.structure.IExceptionResolver;
import tod.core.database.structure.IHostInfo;
import tod.core.transport.CollectorLogReceiver;
import tod.core.transport.LogReceiver;
import tod.impl.database.IBidiIterator;
import tod.impl.database.structure.standard.ExceptionResolver;
import tod.impl.database.structure.standard.HostInfo;
import tod.impl.database.structure.standard.ThreadInfo;
import tod.impl.database.structure.standard.ExceptionResolver.BehaviorInfo;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridEventCollector;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.db.EventDatabase;
import tod.impl.dbgrid.db.ObjectsDatabase;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.db.RINodeEventIterator;
import tod.impl.dbgrid.db.ReorderedObjectsDatabase;
import tod.impl.dbgrid.db.StringIndexer;
import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.ObjectCodec;
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
	 * A database node maintains a local copy of the exception resolver
	 * for efficiency reasons.
	 * In a local setup the exception resolver is shared with that of the master.
	 * Otherwise each node has its own resolver.
	 * @see EventCollector#exception(int, long, short, long, String, String, String, int, Object).
	 */
	private IExceptionResolver itsExceptionResolver;
	
	private EventDatabase itsEventsDatabase;
	private File itsObjectsDatabaseFile;
	private File itsStringIndexFile;
	
	private List<ReorderedObjectsDatabase> itsObjectsDatabases = 
		new ArrayList<ReorderedObjectsDatabase>();
	
	private StringIndexer itsStringIndexer;
	
	private FlusherThread itsFlusherThread = new FlusherThread();

	private DatabaseNode() throws RemoteException
	{
		String thePrefix = DebuggerGridConfig.NODE_DATA_DIR;
		File theParent = new File(thePrefix);
		System.out.println("Using data directory: "+theParent);
		
		itsObjectsDatabaseFile = new File(theParent, "objects.bin");
		itsStringIndexFile = new File(theParent, "strings");
	}
	
	/**
	 * Creates a node that will work with a local master.
	 */
	public static DatabaseNode createLocalNode()
	{
		try
		{
			DatabaseNode theNode = new DatabaseNode();
			theNode.itsExceptionResolver = new LocalNodeExceptionResolver(theNode);
			return theNode;
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static DatabaseNode createRemoteNode() throws RemoteException
	{
		DatabaseNode theNode = new DatabaseNode();
		theNode.itsExceptionResolver = new NodeExceptionResolver(theNode);
		return theNode;
	}
	
	@Override
	protected void connectedToMaster()
	{
		super.connectedToMaster();
		initDatabase();
	}
	
	private synchronized void initDatabase()
	{
		HardPagedFile.clearCache(); //TODO: only clear pages of current database
		
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
		
		File theFile = new File(theParent, "events.bin");
		theFile.delete();
		itsEventsDatabase = createDatabase(theFile);		

		// Init objects database
		for (ObjectsDatabase theDatabase : itsObjectsDatabases)
		{
			if (theDatabase != null) theDatabase.dispose();
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
	public synchronized int flush()
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
	
	public void registerBehaviors(BehaviorInfo[] aBehaviorInfos)
	{
		if (itsExceptionResolver instanceof ExceptionResolver)
		{
			ExceptionResolver theResolver = (ExceptionResolver) itsExceptionResolver;
			theResolver.registerBehaviors(aBehaviorInfos);
		}
	}
	
	public void pushEvent(GridEvent aEvent)
	{
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
				itsExceptionResolver,
				this);
		
//		if (DebugFlags.COLLECTOR_LOG) theCollector = new PrintThroughCollector(
//				aHostInfo,
//				theCollector,
//				aMaster.getLocationStore());
		
		return new MyReceiver(
//				NODE_THREAD,
				aHostInfo,
				theCollector,
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
	
	private static class MyCollector extends GridEventCollector
	{
		private GridMaster itsMaster;
		
		public MyCollector(
				GridMaster aMaster, 
				IHostInfo aHost, 
				IExceptionResolver aExceptionResolver,
				DatabaseNode aNode)
		{
			super(aHost, aExceptionResolver, aNode);
			itsMaster = aMaster;
		}

		@Override
		public void thread(int aThreadId, long aJVMThreadId, String aName)
		{
			ThreadInfo theThread = createThreadInfo(getHost(), aThreadId, aJVMThreadId, aName);
			itsMaster.registerThread(theThread);
		}
	}
	
	private class MyReceiver extends CollectorLogReceiver
	{
		public MyReceiver(HostInfo aHostInfo, ILogCollector aCollector, InputStream aInStream, OutputStream aOutStream, boolean aStart)
		{
			super(aHostInfo, aCollector, aInStream, aOutStream, aStart);
		}

		@Override
		protected int flush()
		{
			return DatabaseNode.this.flush();
		}
		
		@Override
		protected void clear()
		{
			DatabaseNode.this.clear();
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
							flush();
							itsFlushed = true;
						}
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

}
