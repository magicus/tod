/*
 * Created on Jul 26, 2006
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
import tod.core.config.GeneralConfig;
import tod.core.transport.CollectorPacketReader;
import tod.core.transport.MessageType;
import tod.impl.bci.asm.ASMLocationPool;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
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
			HierarchicalIndex<StdIndexSet.StdTuple>[] theIndexes = new HierarchicalIndex[aCount];
			for (int i = 0; i < theIndexes.length; i++)
			{
				theIndexes[i] = new HierarchicalIndex<StdIndexSet.StdTuple>(
						""+i, 
						thePagedFile, 
						StdIndexSet.TUPLE_CODEC);
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
				theIndexes[i] = new HierarchicalIndex<RoleIndexSet.RoleTuple>(
						""+i, 
						thePagedFile, 
						RoleIndexSet.TUPLE_CODEC);
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
			return new EventList(thePagedFile);
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
	public static void fillNode(DatabaseNode aNode, EventGenerator aGenerator, long aCount)
	{
		for(long i=0;i<aCount;i++) 
		{
			aNode.push(aGenerator.next());
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
			DatabaseNode aNode, 
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
		BidiIterator<GridEvent> theIterator = aNode.evaluate(aCondition, theTimestamp);
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
		System.out.println("Expecting "+aExpectedNodes+" nodes");
		
		LocationRegistrer theLocationRegistrer = new LocationRegistrer();
		new ASMLocationPool(theLocationRegistrer, new File(GeneralConfig.LOCATIONS_FILE));
		GridMaster theMaster = new GridMaster(theLocationRegistrer, aExpectedNodes);
		
		aRegistry.bind(GridMaster.RMI_ID, theMaster);
		
		System.out.println("Bound master");

		if (aExpectedNodes > 0)
		{
			while (theMaster.getNodeCount() < aExpectedNodes)
			{
				Thread.sleep(1000);
				System.out.println("Found "+theMaster.getNodeCount()+"/"+aExpectedNodes+" nodes.");
			}
		}
		else new DatabaseNode(true);

		return theMaster;
	}
	
	public static long replay(
			File aFile,
			GridMaster aMaster,
			ILogCollector aCollector) 
			throws IOException
	{
		DataInputStream theStream = new DataInputStream(new BufferedInputStream(new FileInputStream(aFile)));
		
		String theHostName = theStream.readUTF();
		System.out.println("Reading events of "+theHostName);

		long theCount = 0;
		
		DerivativeDataPrinter thePrinter = new DerivativeDataPrinter(
				new File("replay-times.txt"),
				"time (ms)",
				"events");
		
		long t0 = System.currentTimeMillis();
		
		while (true)
		{
			byte theCommand;
			try
			{
				theCommand = theStream.readByte();
			}
			catch (EOFException e)
			{
				break;
			}
			
			try
			{
				if (theCommand == NativeAgentPeer.INSTRUMENT_CLASS)
				{
					throw new RuntimeException();
				}
				else
				{
					MessageType theType = MessageType.values()[theCommand];
					CollectorPacketReader.readPacket(
							theStream, 
							aCollector,
							null,
							theType);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
			
			theCount++;
			
			if (theCount % 100000 == 0)
			{
				System.out.println(theCount);
				
				long t = System.currentTimeMillis()-t0;
				thePrinter.addPoint(t/1000f, theCount);
			}
		}
		
		aMaster.flush();
		System.out.println("Done");

		long t = System.currentTimeMillis()-t0;
		thePrinter.addPoint(t/1000f, theCount);
		thePrinter.close();
		
		return theCount;
	}
	

}
