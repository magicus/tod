/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Random;

import org.junit.Test;

import tod.core.model.structure.ObjectId;
import tod.impl.dbgrid.TestHierarchicalIndex.TimestampGenerator;
import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.messages.EventType;
import tod.impl.dbgrid.messages.GridBehaviorCallEvent;
import tod.impl.dbgrid.messages.GridBehaviorExitEvent;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridExceptionGeneratedEvent;
import tod.impl.dbgrid.messages.GridFieldWriteEvent;
import tod.impl.dbgrid.messages.GridVariableWriteEvent;
import zz.utils.bit.IntBitStruct;


public class TestEventList
{
	@Test public void writeAndCheck()
	{
		fillCheck(1000000);
	}
	
	private void fillCheck(long aCount)
	{
		EventList theEventList = FixtureIndexes.createEventList();
		FixtureIndexes.fillEventList(theEventList, new EventGenerator(0), aCount);
		checkEventList(theEventList, new EventGenerator(0), aCount);
	}
	
	private void checkEventList(
			EventList aEventList, 
			EventGenerator aGenerator,
			long aCount)
	{
		checkEvents(aEventList.getEventIterator(), aGenerator, aCount, true);
	}
	
	private void checkEvents(
			Iterator<GridEvent> aIterator, 
			EventGenerator aGenerator,
			long aCount,
			boolean aExhaust)
	{
		for (long i=0;i<aCount;i++)
		{
			GridEvent theRefEvent = aGenerator.next();
			
			if (! aIterator.hasNext()) fail("No more tuples");
			GridEvent theEvent = aIterator.next();
			checkEvent(theRefEvent, theEvent);
			
			if (i % 1000000 == 0) System.out.println("v: "+i);
		}
		
		if (aExhaust && aIterator.hasNext()) fail("Too many events");
	}
	
	private void checkEvent(GridEvent aRefEvent, GridEvent aEvent)
	{
		IntBitStruct theRefStruct = new IntBitStruct(1000);
		IntBitStruct theStruct = new IntBitStruct(1000);
		
		aRefEvent.writeTo(theRefStruct);
		aEvent.writeTo(theStruct);
		
		assertTrue("Size mismatch", theRefStruct.getPos() == theStruct.getPos());
		int theSize = (theRefStruct.getPos()+31)/32;
		
		theRefStruct.setPos(0);
		theStruct.setPos(0);
		
		for (int i=0;i<theSize;i++)
		{
			assertTrue("Data mismatch", theRefStruct.readInt(32) == theStruct.readInt(32));
		}
	}


	public static class EventGenerator
	{
		private Random itsRandom;
		private TimestampGenerator itsTimestampGenerator;

		public EventGenerator(long aSeed)
		{
			itsRandom = new Random(aSeed);
			itsTimestampGenerator = new TimestampGenerator(aSeed);
		}
		
		public GridEvent next()
		{
			EventType theType = EventType.values()[itsRandom.nextInt(EventType.values().length-1)];
			switch (theType)
			{
			case BEHAVIOR_EXIT:
				return new GridBehaviorExitEvent(
						genHostId(),
						genThreadId(),
						itsTimestampGenerator.next(),
						genBytecodeIndex(),
						genParentPointer(),
						itsRandom.nextBoolean(),
						genObject(),
						genBehaviorId());
				
			case CONSTRUCTOR_CHAINING:
				return new GridBehaviorCallEvent(
						genHostId(),
						genThreadId(),
						itsTimestampGenerator.next(),
						genBytecodeIndex(),
						genParentPointer(),
						EventType.CONSTRUCTOR_CHAINING,
						itsRandom.nextBoolean(),
						genArgs(),
						genBehaviorId(),
						genBehaviorId(),
						genObject());
				
			case EXCEPTION_GENERATED:
				return new GridExceptionGeneratedEvent(
						genHostId(),
						genThreadId(),
						itsTimestampGenerator.next(),
						genBytecodeIndex(),
						genParentPointer(),
						genObject(),
						genBehaviorId());
				
			case FIELD_WRITE:
				return new GridFieldWriteEvent(
						genHostId(),
						genThreadId(),
						itsTimestampGenerator.next(),
						genBytecodeIndex(),
						genParentPointer(),
						genFieldId(),
						genObject(),
						genObject());
				
			case INSTANTIATION:
				return new GridBehaviorCallEvent(
						genHostId(),
						genThreadId(),
						itsTimestampGenerator.next(),
						genBytecodeIndex(),
						genParentPointer(),
						EventType.INSTANTIATION,
						itsRandom.nextBoolean(),
						genArgs(),
						genBehaviorId(),
						genBehaviorId(),
						genObject());
				
			case LOCAL_VARIABLE_WRITE:
				return new GridVariableWriteEvent(
						genHostId(),
						genThreadId(),
						itsTimestampGenerator.next(),
						genBytecodeIndex(),
						genParentPointer(),
						genVariableId(),
						genObject());
				
			case METHOD_CALL:
				return new GridBehaviorCallEvent(
						genHostId(),
						genThreadId(),
						itsTimestampGenerator.next(),
						genBytecodeIndex(),
						genParentPointer(),
						EventType.METHOD_CALL,
						itsRandom.nextBoolean(),
						genArgs(),
						genBehaviorId(),
						genBehaviorId(),
						genObject());
				
			default: throw new RuntimeException("Not handled: "+theType); 
			}

		}
		
		public byte[] genParentPointer()
		{
			return new byte[(DebuggerGridConfig.EVENTID_POINTER_SIZE+7)/8];
		}
		
		public int genHostId()
		{
			return itsRandom.nextInt(100);
		}
		
		public int genThreadId()
		{
			return itsRandom.nextInt(10000);
		}
		
		public int genBehaviorId()
		{
			return itsRandom.nextInt(1000);
		}
		
		public int genFieldId()
		{
			return itsRandom.nextInt(1000);
		}
		
		public int genVariableId()
		{
			return itsRandom.nextInt(1000);
		}
		
		public int genBytecodeIndex()
		{
			return itsRandom.nextInt(65535);
		}
		
		public Object genObject()
		{
			return new ObjectId.ObjectUID(itsRandom.nextInt(100000));
		}
		
		public Object[] genArgs()
		{
			int theCount = itsRandom.nextInt(10);
			Object[] theArgs = new Object[theCount];
			for (int i = 0; i < theArgs.length; i++) theArgs[i] = genObject();
			return theArgs;
		}

	}
	
	
	
}
