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
import java.util.ArrayList;
import java.util.List;

import tod.agent.DebugFlags;
import tod.core.ILogCollector;
import tod.core.database.browser.ILocationStore;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.HostInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ThreadInfo;
import tod.core.transport.CollectorLogReceiver;
import tod.core.transport.LogReceiver;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridEventCollector;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.db.ObjectsDatabase;
import tod.impl.dbgrid.db.ObjectsReorderingBuffer;
import tod.impl.dbgrid.db.ReorderedObjectsDatabase;
import tod.impl.dbgrid.db.ObjectsReorderingBuffer.Entry;
import tod.impl.dbgrid.db.ObjectsReorderingBuffer.ReorderingBufferListener;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.ObjectCodec;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Probe;
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
	private List<ReorderedObjectsDatabase> itsObjectsDatabases = 
		new ArrayList<ReorderedObjectsDatabase>();

	public LeafEventDispatcher(ILocationStore aLocationStore) throws RemoteException
	{
		super(true);
		itsLocationStore = aLocationStore;
		
		String thePrefix = DebuggerGridConfig.NODE_DATA_DIR;
		File theParent = new File(thePrefix);
		System.out.println("Using data directory: "+theParent);
		
		itsObjectsDatabaseFile = new File(theParent, "objects.bin");

		initDatabase();
	}
	
	private void initDatabase()
	{
		for (ObjectsDatabase theDatabase : itsObjectsDatabases)
		{
			if (theDatabase != null) theDatabase.unregister();
		}
		
		itsObjectsDatabases.clear();
		itsObjectsDatabaseFile.delete();
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
}
