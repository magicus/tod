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
package tod.impl.dbgrid;


import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.Random;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.bci.NativeAgentPeer;
import tod.core.config.TODConfig;
import tod.core.transport.CollectorPacketReader;
import tod.core.transport.LogReceiver;
import tod.core.transport.MessageType;
import tod.core.transport.LogReceiver.ILogReceiverMonitor;
import tod.impl.bci.asm.ASMLocationPool;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.gridimpl.GridImpl;
import tod.impl.dbgrid.gridimpl.uniform.UniformEventDatabase;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;
import tod.impl.dbgrid.test.TestHierarchicalIndex;
import tod.impl.dbgrid.test.TestHierarchicalIndex.TimestampGenerator;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;


public class Fixtures
{

	public static class FakeThread
	{
		private static int itsExpectedLength = 10;
		
		private int itsHostId;
		private int itsThreadId;
		
		private TimestampGenerator itsGenerator;
		private Random itsRandom;
		private LinkedList<byte[]> itsStack = new LinkedList<byte[]>();
		
		public FakeThread(int aHostId, int aThreadId)
		{
			itsHostId = aHostId;
			itsThreadId = aThreadId;
			
			int theSeed = itsHostId*10000 + itsThreadId;
			itsGenerator = new TimestampGenerator(theSeed);
			itsRandom = new Random(theSeed);
			
			itsStack.addLast(genPointer());
		}
		
		private byte[] genPointer()
		{
			return ExternalPointer.create(itsHostId, itsThreadId, itsGenerator.next());
		}
		
	}

	public static HierarchicalIndex<StdIndexSet.StdTuple> createStdIndex() 
	{
		return createStdIndexes(1)[0];
	}

	public static HierarchicalIndex<StdIndexSet.StdTuple>[] createStdIndexes(int aCount) 
	{
		try
		{
			File theFile = new File("stdIndexTest.bin");
			theFile.delete();
			HardPagedFile thePagedFile = new HardPagedFile(theFile, DebuggerGridConfig.DB_PAGE_SIZE);
			
			StdIndexSet theIndexSet = new StdIndexSet("test", thePagedFile, aCount);
			
			HierarchicalIndex<StdIndexSet.StdTuple>[] theIndexes = new HierarchicalIndex[aCount];
			for (int i = 0; i < theIndexes.length; i++)
			{
				theIndexes[i] = new HierarchicalIndex<StdIndexSet.StdTuple>(theIndexSet, -1);
			}
			
			return theIndexes;
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static HierarchicalIndex<RoleIndexSet.RoleTuple> createRoleIndex() 
	{
		return createRoleIndexes(1)[0];
	}
	
	public static HierarchicalIndex<RoleIndexSet.RoleTuple>[] createRoleIndexes(int aCount) 
	{
		try
		{
			File theFile = new File("roleIndexTest.bin");
			theFile.delete();
			HardPagedFile thePagedFile = new HardPagedFile(theFile, DebuggerGridConfig.DB_PAGE_SIZE);
			
			RoleIndexSet theIndexSet = new RoleIndexSet("test", thePagedFile, aCount);
			HierarchicalIndex<RoleIndexSet.RoleTuple>[] theIndexes = new HierarchicalIndex[aCount];
			for (int i = 0; i < theIndexes.length; i++)
			{
				theIndexes[i] = new HierarchicalIndex<RoleIndexSet.RoleTuple>(theIndexSet, -1);
			}
			
			return theIndexes;
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static EventList createEventList() 
	{
		try
		{
			File theFile = new File("eventTest.bin");
			theFile.delete();
			HardPagedFile thePagedFile = new HardPagedFile(theFile, DebuggerGridConfig.DB_PAGE_SIZE);
			return new EventList(0, thePagedFile);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	
	public static long inventData(long aTimestamp)
	{
		return aTimestamp*7;
	}

	public static byte inventRole(long aTimestamp)
	{
		return (byte) aTimestamp;
	}
	
	/**
	 * Fills an std index with values.
	 */
	public static void fillStdIndex(
			HierarchicalIndex<StdIndexSet.StdTuple> aIndex, 
			TestHierarchicalIndex.TimestampGenerator aGenerator,
			long aTupleCount)
	{
		for (long i=0;i<aTupleCount;i++)
		{
			long theTimestamp = aGenerator.next();
			aIndex.add(new StdIndexSet.StdTuple(theTimestamp, inventData(theTimestamp)));
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}

	/**
	 * Fills an std index with values.
	 */
	public static void fillRoleIndex(
			HierarchicalIndex<RoleIndexSet.RoleTuple> aIndex, 
			TestHierarchicalIndex.TimestampGenerator aGenerator,
			long aTupleCount)
	{
		for (long i=0;i<aTupleCount;i++)
		{
			long theTimestamp = aGenerator.next();
			aIndex.add(new RoleIndexSet.RoleTuple(
					theTimestamp, 
					inventData(theTimestamp), 
					inventRole(theTimestamp)));
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}
	
	/**
	 * Fills an event list
	 */
	public static void fillEventList(
			EventList aEventList, 
			EventGenerator aGenerator,
			long aCount)
	{
		for (long i=0;i<aCount;i++)
		{
			GridEvent theEvent = aGenerator.next();
			aEventList.add(theEvent);
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}
	
	/**
	 * Fills an event list and returns the ids of the events
	 */
	public static long[] fillEventListReport(
			EventList aEventList, 
			EventGenerator aGenerator,
			int aCount)
	{
		long[] theIds = new long[aCount];
		for (int i=0;i<aCount;i++)
		{
			GridEvent theEvent = aGenerator.next();
			theIds[i] = aEventList.add(theEvent);
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
		
		return theIds;
	}
	
	/**
	 * Fills a database node with events generated by the specified generator.
	 */
	public static void fillDatabase(UniformEventDatabase aDatabase, EventGenerator aGenerator, long aCount)
	{
		for(long i=0;i<aCount;i++) 
		{
			aDatabase.push(aGenerator.next());
			if (i % 1000000 == 0) System.out.println(i);
		}
	}

	/**
	 * Checks that two events are equal.
	 */
	public static void assertEquals(GridEvent aRefEvent, GridEvent aEvent)
	{
		BitStruct theRefStruct = new IntBitStruct(1000);
		BitStruct theStruct = new IntBitStruct(1000);
		
		aRefEvent.writeTo(theRefStruct);
		aEvent.writeTo(theStruct);
		
		if (theRefStruct.getPos() != theStruct.getPos())
		{
			System.out.println("ref:  "+aRefEvent);
			System.out.println("test: "+aEvent);
			fail("Size mismatch");
		}
		
		int theSize = (theRefStruct.getPos()+31)/32;
		
		theRefStruct.setPos(0);
		theStruct.setPos(0);
		
		for (int i=0;i<theSize;i++)
		{
			if (theRefStruct.readInt(32) != theStruct.readInt(32))
			{
				System.out.println("ref:  "+aRefEvent);
				System.out.println("test: "+aEvent);
				fail("Data mismatch");				
			}
		}
	}
	
	public static void checkCondition(
			UniformEventDatabase aDatabase, 
			EventCondition aCondition, 
			EventGenerator aReferenceGenerator,
			int aSkip,
			int aCount)
	{
		GridEvent theEvent = null;
		for (int i=0;i<aSkip;i++)
		{
			theEvent = aReferenceGenerator.next();
		}
		
		int theMatched = 0;
		long theTimestamp = theEvent != null ? theEvent.getTimestamp()+1 : 0;
		BidiIterator<GridEvent> theIterator = aDatabase.evaluate(aCondition, theTimestamp);
		for (int i=0;i<aCount;i++)
		{
			GridEvent theRefEvent = aReferenceGenerator.next();
			if (aCondition.match(theRefEvent))
			{
				GridEvent theTestedEvent = theIterator.next(); 
				Fixtures.assertEquals(theRefEvent, theTestedEvent);
				theMatched++;
//				System.out.println(i+"m");
			}
//			else System.out.println(i);
		}
		
		System.out.println("Matched: "+theMatched);
	}

	/**
	 * Standard setup of a grid master that waits for a number
	 * of database nodes to connect
	 */
	public static GridMaster setupMaster(Registry aRegistry, String[] args) throws Exception
	{
		int theExpectedNodes = 0;
		if (args.length > 0)
		{
			theExpectedNodes = Integer.parseInt(args[0]);
		}
		
		return setupMaster(aRegistry, theExpectedNodes);
	}
		
	/**
	 * Standard setup of a grid master that waits for a number
	 * of database nodes to connect
	 */
	public static GridMaster setupMaster(Registry aRegistry, int aExpectedNodes) throws Exception
	{
		return setupMaster(new TODConfig(), aRegistry, aExpectedNodes);
	}
	
	public static GridMaster setupMaster(
			TODConfig aConfig,
			Registry aRegistry,
			int aExpectedNodes) throws Exception
	{
		System.out.println("Expecting "+aExpectedNodes+" nodes");
		
		LocationRegistrer theLocationRegistrer = new LocationRegistrer();
		
		new ASMLocationPool(
				theLocationRegistrer, 
				new File(aConfig.get(TODConfig.INSTRUMENTER_LOCATIONS_FILE)));
		
		GridMaster theMaster = new GridMaster(
				aConfig, 
				theLocationRegistrer, 
				null,
				aExpectedNodes);
		
		aRegistry.bind(GridMaster.RMI_ID, theMaster);
		
		System.out.println("Bound master");
		
		theMaster.waitReady();

		if (aExpectedNodes == 0) GridImpl.getFactory(aConfig).createNode(true);

		return theMaster;
	}
	
	public static long replay(
			File aFile,
			GridMaster aMaster) 
			throws IOException
	{
		DataInputStream theStream = new DataInputStream(
				new BufferedInputStream(new FileInputStream(aFile)));
		

		LogReceiver theReceiver = aMaster.getDispatcher().createLogReceiver(
				null, 
				aMaster, 
				null, 
				theStream, 
				null,
				false);
		
		MyLogReceiverMonitor theMonitor = new MyLogReceiverMonitor();
		theReceiver.setMonitor(theMonitor);
		
		theReceiver.start();

		theReceiver.waitEof();
		
		aMaster.flush();
		System.out.println("Done");

		theMonitor.processedMessages(theReceiver.getMessageCount());
		
		return theReceiver.getMessageCount();
	}
	
	private static class MyLogReceiverMonitor implements ILogReceiverMonitor
	{
		private long t0;
		private DerivativeDataPrinter itsPrinter;
		
		private MyLogReceiverMonitor()
		{
			try
			{
				itsPrinter = new DerivativeDataPrinter(
									new File("replay-times.txt"),
									"time (ms)",
									"events");
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void started()
		{
			t0 = System.currentTimeMillis();
		}

		public void processedMessages(long aCount)
		{
			System.out.println(aCount);

			long t = System.currentTimeMillis()-t0;
			itsPrinter.addPoint(t/1000f, aCount);
		}

		
	}
	

}
