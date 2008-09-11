/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng;


import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import tod.core.config.TODConfig;
import tod.core.database.event.ILogEvent;
import tod.core.transport.LogReceiver.ILogReceiverMonitor;
import tod.impl.database.IBidiIterator;
import tod.impl.database.structure.standard.StructureDatabase;
import tod.impl.dbgrid.DBGridUtils;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.evdbng.db.EventDatabaseNG;
import tod.impl.evdbng.db.EventList;
import tod.impl.evdbng.db.file.PagedFile;
import tod.impl.evdbng.db.file.RoleTree;
import tod.impl.evdbng.db.file.SimpleTree;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;
import tod.impl.evdbng.messages.GridEventNG;
import tod.impl.evdbng.queries.EventCondition;
import tod.impl.evdbng.test.TimestampGenerator;


public class FixturesNG
{

	
	public static EventList createEventList() 
	{
		PagedFile theIndexesFile = PagedFile.create(new File("indexes.bin"));
		PagedFile theEventsFile = PagedFile.create(new File("events.bin"));
		return new EventList(0, StructureDatabase.create(new TODConfig()), 0, theIndexesFile, theEventsFile);
	}
	
	public static PageIOStream createPageIOStream()
	{
		PagedFile theFile = new PagedFile(new File("test"+Math.random()+".bin"));
		return theFile.create().asIOStream();
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
			SimpleTree aIndex, 
			TimestampGenerator aGenerator,
			long aTupleCount)
	{
		for (long i=0;i<aTupleCount;i++)
		{
			long theTimestamp = aGenerator.next();
			aIndex.add(theTimestamp);
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}

	/**
	 * Fills an std index with values.
	 */
	public static void fillRoleIndex(
			RoleTree aIndex, 
			TimestampGenerator aGenerator,
			long aTupleCount)
	{
		for (long i=0;i<aTupleCount;i++)
		{
			long theTimestamp = aGenerator.next();
			aIndex.add(theTimestamp, inventRole(theTimestamp));
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}
	
	/**
	 * Fills an event list
	 */
	public static void fillEventList(
			EventList aEventList, 
			EventGeneratorNG aGenerator,
			long aCount)
	{
		for (long i=0;i<aCount;i++)
		{
			GridEventNG theEvent = aGenerator.next();
			aEventList.add(theEvent);
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}
	
	/**
	 * Fills an event list and returns the ids of the events
	 */
	public static int[] fillEventListReport(
			EventList aEventList, 
			EventGeneratorNG aGenerator,
			int aCount)
	{
		int[] theIds = new int[aCount];
		for (int i=0;i<aCount;i++)
		{
			GridEventNG theEvent = aGenerator.next();
			theIds[i] = aEventList.add(theEvent);
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
		
		return theIds;
	}
	
	/**
	 * Fills a database node with events generated by the specified generator.
	 */
	public static void fillDatabase(EventDatabaseNG aDatabase, EventGeneratorNG aGenerator, long aCount)
	{
		for(long i=0;i<aCount;i++) 
		{
			aDatabase.push(aGenerator.next());
			if (i % 100000 == 0) System.out.println(i);
		}
		aDatabase.flush(null);
	}
	
	public static void fillDatabase(GridMaster aMaster, EventGeneratorNG aGenerator, long aCount)
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
	
	private static final ThreadLocal<PageIOStream> refStruct = new ThreadLocal<PageIOStream>()
	{
		@Override
		protected PageIOStream initialValue()
		{
			return createPageIOStream();
		}
	};
	
	private static final ThreadLocal<PageIOStream> struct = new ThreadLocal<PageIOStream>()
	{
		@Override
		protected PageIOStream initialValue()
		{
			return createPageIOStream();
		}
	};
	
	/**
	 * Checks that two events are equal.
	 */
	public static void assertEquals(String aMessage, GridEventNG aRefEvent, GridEventNG aEvent)
	{
		PageIOStream theRefStruct = refStruct.get();
		PageIOStream theStruct = struct.get();
		
		theRefStruct.setPos(0);
		theStruct.setPos(0);
		
		aRefEvent.writeTo(theRefStruct);
		aEvent.writeTo(theStruct);
		
		if (theRefStruct.getPos() != theStruct.getPos())
		{
			System.out.println("ref:  "+aRefEvent);
			System.out.println("test: "+aEvent);
			fail("Size mismatch - "+aMessage);
		}
		
		int theSize = theRefStruct.getPos();

		theRefStruct.setPos(0);
		theStruct.setPos(0);

		int i;
		for (i=0;i<theSize-3;i+=4)
		{
			int theRef = theRefStruct.readInt();
			int theEv = theStruct.readInt();
			if (theRef != theEv)
			{
				System.out.println("ref:  "+aRefEvent);
				System.out.println("test: "+aEvent);
				fail("Data mismatch - "+aMessage);				
			}
		}
		
		for (;i<theSize;i++)
		{
			byte theRefByte = theRefStruct.readByte();
			byte theByte = theStruct.readByte();
			if (theRefByte != theByte)
			{
				System.out.println("ref:  "+aRefEvent);
				System.out.println("test: "+aEvent);
				fail("Data mismatch - "+aMessage);				
			}
		}
	}
	
	public static int checkCondition(
			EventDatabaseNG aDatabase, 
			EventCondition aCondition, 
			EventGeneratorNG aReferenceGenerator,
			int aSkip,
			int aCount)
	{
		assert aDatabase.getEventsCount() > aSkip+aCount;
		
		GridEventNG theEvent = null;
		for (int i=0;i<aSkip;i++) theEvent = aReferenceGenerator.next();
		
		long theTimestamp = theEvent != null ? theEvent.getTimestamp()+1 : 0;
		
		IBidiIterator<GridEvent> theIterator = aDatabase.evaluate(aCondition, theTimestamp);
		return checkCondition(theIterator, aCondition, aReferenceGenerator, aCount);
	}
	
	public static int checkCondition(
			IBidiIterator<GridEvent> aIterator, 
			EventCondition aCondition,
			EventGeneratorNG aReferenceGenerator,
			int aCount)
	{
		int theMatched = 0;
		for (int i=0;i<aCount;i++)
		{
			GridEventNG theRefEvent = aReferenceGenerator.next();
			if (aCondition._match(theRefEvent))
			{
				GridEventNG theTestedEvent = (GridEventNG) aIterator.next(); 
				FixturesNG.assertEquals(""+i, theRefEvent, theTestedEvent);
				theMatched++;
//				System.out.println(i+"m");
			}
//			else System.out.println(i);
		}
		
		System.out.println("Matched: "+theMatched);
		return theMatched;
	}
	
	public static void checkIteration(
			EventDatabaseNG aDatabase, 
			EventCondition aCondition,
			EventGeneratorNG aReferenceGenerator,
			int aCount)
	{
		List<GridEventNG> theEvents = new ArrayList<GridEventNG>(aCount);

		IBidiIterator<GridEvent> theIterator = aDatabase.evaluate(aCondition, 0);
		while (theEvents.size() < aCount)
		{
			GridEventNG theRefEvent = aReferenceGenerator.next();
			if (aCondition._match(theRefEvent)) theEvents.add((GridEventNG) theIterator.next());
		}
		
		GridEventNG theFirstEvent = theEvents.get(0);
		theIterator = aDatabase.evaluate(aCondition, theFirstEvent.getTimestamp());
		assertEquals("first.a", theFirstEvent, (GridEventNG) theIterator.next());
		assertEquals("first.b", theFirstEvent, (GridEventNG) theIterator.previous());
		
		GridEventNG theSecondEvent = theEvents.get(1);
		theIterator = aDatabase.evaluate(aCondition, theFirstEvent.getTimestamp()+1);
		assertEquals("sec.a", theSecondEvent, (GridEventNG) theIterator.next());
		assertEquals("sec.b", theSecondEvent, (GridEventNG) theIterator.previous());
		
		theIterator = aDatabase.evaluate(aCondition, 0);
		
		int theIndex = 0;
		int theDelta = aCount;
		boolean theForward = true;
		while(theDelta > 1)
		{
			for (int i=0;i<theDelta;i++)
			{
				GridEventNG theRefEvent;
				GridEventNG theTestEvent;
				if (theForward)
				{
					theRefEvent = theEvents.get(theIndex);
					theTestEvent = (GridEventNG) theIterator.next();
					theIndex++;
				}
				else
				{
					theTestEvent = (GridEventNG) theIterator.previous();
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
			return DBGridUtils.setupLocalMaster(null);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
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
	
	// ha!
}
