/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid;

import static tod.impl.dbgrid.DebuggerGridConfig.EVENTID_POINTER_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BEHAVIOR_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BYTECODE_LOCS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_FIELD_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_HOSTS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_OBJECT_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_THREADS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_VAR_COUNT;

import java.util.Random;

import tod.core.model.structure.ObjectId;
import tod.impl.dbgrid.TestHierarchicalIndex.TimestampGenerator;
import tod.impl.dbgrid.messages.EventType;
import tod.impl.dbgrid.messages.GridBehaviorCallEvent;
import tod.impl.dbgrid.messages.GridBehaviorExitEvent;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridExceptionGeneratedEvent;
import tod.impl.dbgrid.messages.GridFieldWriteEvent;
import tod.impl.dbgrid.messages.GridVariableWriteEvent;

public class EventGenerator
{
	private Random itsRandom;
	private TimestampGenerator itsTimestampGenerator;
	
	private int itsHostsRange;
	private int itsThreadsRange;
	private int itsBytecodeRange;
	private int itsBehaviorRange;
	private int itsFieldRange;
	private int itsVariableRange;
	private int itsObjectRange;



	public EventGenerator(
			long aSeed,
			int aHostsRange, 
			int aThreadsRange, 
			int aBytecodeRange, 
			int aBehaviorRange, 
			int aFieldRange, 
			int aVariableRange, 
			int aObjectRange)
	{
		itsRandom = new Random(aSeed);
		itsTimestampGenerator = new TimestampGenerator(aSeed);		
		
		itsHostsRange = aHostsRange;
		itsThreadsRange = aThreadsRange;
		itsBytecodeRange = aBytecodeRange;
		itsBehaviorRange = aBehaviorRange;
		itsFieldRange = aFieldRange;
		itsVariableRange = aVariableRange;
		itsObjectRange = aObjectRange;
	}

	public EventGenerator(long aSeed)
	{
		this(
				aSeed, 
				STRUCTURE_HOSTS_COUNT, 
				STRUCTURE_THREADS_COUNT,
				STRUCTURE_BYTECODE_LOCS_COUNT,
				STRUCTURE_BEHAVIOR_COUNT,
				STRUCTURE_FIELD_COUNT,
				STRUCTURE_VAR_COUNT,
				STRUCTURE_OBJECT_COUNT);
	}
	
	public GridEvent next()
	{
		EventType theType = genType();
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

	public EventType genType()
	{
		return EventType.values()[itsRandom.nextInt(EventType.values().length-1)];
	}
	
	public byte[] genParentPointer()
	{
		return new byte[(EVENTID_POINTER_SIZE+7)/8];
	}
	
	public int genHostId()
	{
		return itsRandom.nextInt(itsHostsRange);
	}
	
	public int genThreadId()
	{
		return itsRandom.nextInt(itsThreadsRange);
	}
	
	public int genBehaviorId()
	{
		return itsRandom.nextInt(itsBehaviorRange);
	}
	
	public int genFieldId()
	{
		return itsRandom.nextInt(itsFieldRange);
	}
	
	public int genVariableId()
	{
		return itsRandom.nextInt(itsVariableRange);
	}
	
	public int genBytecodeIndex()
	{
		return itsRandom.nextInt(itsBytecodeRange);
	}
	
	public Object genObject()
	{
		return new ObjectId.ObjectUID(itsRandom.nextInt(itsObjectRange));
	}
	
	public Object[] genArgs()
	{
		int theCount = itsRandom.nextInt(10);
		Object[] theArgs = new Object[theCount];
		for (int i = 0; i < theArgs.length; i++) theArgs[i] = genObject();
		return theArgs;
	}

}