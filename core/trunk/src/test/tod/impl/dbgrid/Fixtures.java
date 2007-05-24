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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import tod.agent.DebugFlags;
import tod.core.LocationRegisterer;
import tod.core.config.TODConfig;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.HostInfo;
import tod.core.transport.LogReceiver;
import tod.core.transport.LogReceiver.ILogReceiverMonitor;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.dbgrid.db.EventDatabase;
import tod.impl.dbgrid.db.EventList;
import tod.impl.dbgrid.db.HierarchicalIndex;
import tod.impl.dbgrid.db.RoleIndexSet;
import tod.impl.dbgrid.db.StdIndexSet;
import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;
import tod.impl.dbgrid.test.TestHierarchicalIndex;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;


public class Fixtures
{

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
			
			HierarchicalIndex<StdIndexSet.StdTuple>[] theIndexes = new HierarchicalIndex[aCount];
			for (int i = 0; i < theIndexes.length; i++)
			{
				theIndexes[i] = new HierarchicalIndex<StdIndexSet.StdTuple>(StdIndexSet.TUPLE_CODEC, thePagedFile);
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
			
			HierarchicalIndex<RoleIndexSet.RoleTuple>[] theIndexes = new HierarchicalIndex[aCount];
			for (int i = 0; i < theIndexes.length; i++)
			{
				theIndexes[i] = new HierarchicalIndex<RoleIndexSet.RoleTuple>(RoleIndexSet.TUPLE_CODEC, thePagedFile);
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
	public static void fillDatabase(EventDatabase aDatabase, EventGenerator aGenerator, long aCount)
	{
		for(long i=0;i<aCount;i++) 
		{
			aDatabase.push(aGenerator.next());
			if (i % 100000 == 0) System.out.println(i);
		}
		aDatabase.flush();
	}
	
	public static void fillDatabase(GridMaster aMaster, EventGenerator aGenerator, long aCount)
	{
		throw new UnsupportedOperationException();
//		AbstractEventDispatcher theDispatcher = 
//			(AbstractEventDispatcher) aMaster._getDispatcher();
//		
//		for(long i=0;i<aCount;i++) 
//		{
//			theDispatcher.dispatchEvent(aGenerator.next());
//			if (i % 100000 == 0) System.out.println(i);
//		}
//		
//		aMaster.flush();
	}

	public static void assertEquals(String aMessage, ILogEvent aRefEvent, ILogEvent aEvent)
	{
		if (! aRefEvent.equals(aEvent)) fail(aMessage);
	}
	
	/**
	 * Checks that two events are equal.
	 */
	public static void assertEquals(String aMessage, GridEvent aRefEvent, GridEvent aEvent)
	{
		BitStruct theRefStruct = new IntBitStruct(1000);
		BitStruct theStruct = new IntBitStruct(1000);
		
		aRefEvent.writeTo(theRefStruct);
		aEvent.writeTo(theStruct);
		
		if (theRefStruct.getPos() != theStruct.getPos())
		{
			System.out.println("ref:  "+aRefEvent);
			System.out.println("test: "+aEvent);
			fail("Size mismatch - "+aMessage);
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
				fail("Data mismatch - "+aMessage);				
			}
		}
	}
	
	public static int checkCondition(
			EventDatabase aDatabase, 
			EventCondition aCondition, 
			EventGenerator aReferenceGenerator,
			int aSkip,
			int aCount)
	{
		GridEvent theEvent = null;
		for (int i=0;i<aSkip;i++) theEvent = aReferenceGenerator.next();
		
		long theTimestamp = theEvent != null ? theEvent.getTimestamp()+1 : 0;
		
		BidiIterator<GridEvent> theIterator = aDatabase.evaluate(aCondition, theTimestamp);
		return checkCondition(theIterator, aCondition, aReferenceGenerator, aCount);
	}
	
	public static int checkCondition(
			BidiIterator<GridEvent> aIterator, 
			EventCondition aCondition,
			EventGenerator aReferenceGenerator,
			int aCount)
	{
		int theMatched = 0;
		for (int i=0;i<aCount;i++)
		{
			GridEvent theRefEvent = aReferenceGenerator.next();
			if (aCondition._match(theRefEvent))
			{
				GridEvent theTestedEvent = aIterator.next(); 
				Fixtures.assertEquals(""+i, theRefEvent, theTestedEvent);
				theMatched++;
//				System.out.println(i+"m");
			}
//			else System.out.println(i);
		}
		
		System.out.println("Matched: "+theMatched);
		return theMatched;
	}
	
	public static void checkIteration(
			EventDatabase aDatabase, 
			EventCondition aCondition,
			EventGenerator aReferenceGenerator,
			int aCount)
	{
		List<GridEvent> theEvents = new ArrayList<GridEvent>(aCount);

		BidiIterator<GridEvent> theIterator = aDatabase.evaluate(aCondition, 0);
		while (theEvents.size() < aCount)
		{
			GridEvent theRefEvent = aReferenceGenerator.next();
			if (aCondition._match(theRefEvent)) theEvents.add(theIterator.next());
		}
		
		GridEvent theFirstEvent = theEvents.get(0);
		theIterator = aDatabase.evaluate(aCondition, theFirstEvent.getTimestamp());
		assertEquals("first.a", theFirstEvent, theIterator.next());
		assertEquals("first.b", theFirstEvent, theIterator.previous());
		
		GridEvent theSecondEvent = theEvents.get(1);
		theIterator = aDatabase.evaluate(aCondition, theFirstEvent.getTimestamp()+1);
		assertEquals("sec.a", theSecondEvent, theIterator.next());
		assertEquals("sec.b", theSecondEvent, theIterator.previous());
		
		theIterator = aDatabase.evaluate(aCondition, 0);
		
		int theIndex = 0;
		int theDelta = aCount;
		boolean theForward = true;
		while(theDelta > 1)
		{
			for (int i=0;i<theDelta;i++)
			{
				GridEvent theRefEvent;
				GridEvent theTestEvent;
				if (theForward)
				{
					theRefEvent = theEvents.get(theIndex);
					theTestEvent = theIterator.next();
					theIndex++;
				}
				else
				{
					theTestEvent = theIterator.previous();
					theIndex--;
					theRefEvent = theEvents.get(theIndex);
				}
				
				assertEquals("index: "+theIndex, theRefEvent, theTestEvent);
			}
			
			theDelta /= 2;
			theForward = ! theForward;
		}
	}

	public static GridMaster setupLocalMaster()
	{
		try
		{
			TODConfig theConfig = new TODConfig();
			LocationRegisterer theRegistrer = new LocationRegisterer();
			
			ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(
					theConfig,
					theRegistrer);

			ASMInstrumenter theInstrumenter = new ASMInstrumenter(theDebuggerConfig);
			
			DatabaseNode theNode = new DatabaseNode();
			GridMaster theMaster = new GridMaster(theConfig, theRegistrer, theInstrumenter, theNode, false);
			theMaster.waitReady();
			return theMaster;
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static long replay(
			File aFile,
			GridMaster aMaster) 
			throws IOException
	{
		DebugFlags.REPLAY_MODE = true;
		
		DataInputStream theStream = new DataInputStream(
				new BufferedInputStream(new FileInputStream(aFile)));
		

		LogReceiver theReceiver = aMaster._getDispatcher().createLogReceiver(
				new HostInfo(1, null), 
				aMaster, 
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
