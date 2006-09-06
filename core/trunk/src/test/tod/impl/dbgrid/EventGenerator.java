/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid;

import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BEHAVIOR_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BYTECODE_LOCS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_DEPTH_RANGE;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_FIELD_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_HOSTS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_OBJECT_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_THREADS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_VAR_COUNT;

import java.util.Random;

import tod.core.database.structure.ObjectId;
import tod.impl.dbgrid.messages.GridBehaviorCallEvent;
import tod.impl.dbgrid.messages.GridBehaviorExitEvent;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridExceptionGeneratedEvent;
import tod.impl.dbgrid.messages.GridFieldWriteEvent;
import tod.impl.dbgrid.messages.GridVariableWriteEvent;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.dbgrid.test.TestHierarchicalIndex.TimestampGenerator;

public class EventGenerator
{
	private Random itsRandom;
	private TimestampGenerator itsTimestampGenerator;
	private TimestampGenerator itsParentTimestampGenerator;
	
	private int itsHostsRange;
	private int itsThreadsRange;
	private int itsDepthRange;
	private int itsBytecodeRange;
	private int itsBehaviorRange;
	private int itsFieldRange;
	private int itsVariableRange;
	private int itsObjectRange;



	public EventGenerator(
			long aSeed,
			int aHostsRange, 
			int aThreadsRange, 
			int aDepthRange, 
			int aBytecodeRange, 
			int aBehaviorRange, 
			int aFieldRange, 
			int aVariableRange, 
			int aObjectRange)
	{
		itsRandom = new Random(aSeed);
		itsTimestampGenerator = new TimestampGenerator(aSeed);		
		itsParentTimestampGenerator = new TimestampGenerator(aSeed);		
		
		itsHostsRange = aHostsRange;
		itsThreadsRange = aThreadsRange;
		itsDepthRange = aDepthRange;
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
				STRUCTURE_DEPTH_RANGE,
				STRUCTURE_BYTECODE_LOCS_COUNT,
				STRUCTURE_BEHAVIOR_COUNT,
				STRUCTURE_FIELD_COUNT,
				STRUCTURE_VAR_COUNT,
				STRUCTURE_OBJECT_COUNT);
	}
	
	public GridEvent next()
	{
		MessageType theType = genType();
		switch (theType)
		{
		case BEHAVIOR_EXIT:
			return new GridBehaviorExitEvent(
					genHostId(),
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genBytecodeIndex(),
					genParentTimestamp(),
					itsRandom.nextBoolean(),
					genObject(),
					genBehaviorId());
			
		case CONSTRUCTOR_CHAINING:
			return new GridBehaviorCallEvent(
					genHostId(),
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genBytecodeIndex(),
					genParentTimestamp(),
					MessageType.CONSTRUCTOR_CHAINING,
					itsRandom.nextBoolean(),
					genArgs(),
					genBehaviorId(),
					genBehaviorId(),
					genObject());
			
		case EXCEPTION_GENERATED:
			return new GridExceptionGeneratedEvent(
					genHostId(),
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genBytecodeIndex(),
					genParentTimestamp(),
					genObject(),
					genBehaviorId());
			
		case FIELD_WRITE:
			return new GridFieldWriteEvent(
					genHostId(),
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genBytecodeIndex(),
					genParentTimestamp(),
					genFieldId(),
					genObject(),
					genObject());
			
		case INSTANTIATION:
			return new GridBehaviorCallEvent(
					genHostId(),
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genBytecodeIndex(),
					genParentTimestamp(),
					MessageType.INSTANTIATION,
					itsRandom.nextBoolean(),
					genArgs(),
					genBehaviorId(),
					genBehaviorId(),
					genObject());
			
		case LOCAL_VARIABLE_WRITE:
			return new GridVariableWriteEvent(
					genHostId(),
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genBytecodeIndex(),
					genParentTimestamp(),
					genVariableId(),
					genObject());
			
		case METHOD_CALL:
			return new GridBehaviorCallEvent(
					genHostId(),
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genBytecodeIndex(),
					genParentTimestamp(),
					MessageType.METHOD_CALL,
					itsRandom.nextBoolean(),
					genArgs(),
					genBehaviorId(),
					genBehaviorId(),
					genObject());
			
		default: throw new RuntimeException("Not handled: "+theType); 
		}

	}

	public MessageType genType()
	{
		return MessageType.values()[itsRandom.nextInt(MessageType.values().length-2)+1];
	}
	
	public long genParentTimestamp()
	{
		return itsParentTimestampGenerator.next();
	}
	
	public int genHostId()
	{
		return itsRandom.nextInt(itsHostsRange) + 1;
	}
	
	public int genThreadId()
	{
		return itsRandom.nextInt(itsThreadsRange) + 1;
	}
	
	public int genDepth()
	{
		return itsRandom.nextInt(itsDepthRange);
	}
	
	public int genBehaviorId()
	{
		return itsRandom.nextInt(itsBehaviorRange) + 1;
	}
	
	public int genFieldId()
	{
		return itsRandom.nextInt(itsFieldRange) + 1;
	}
	
	public int genVariableId()
	{
		return itsRandom.nextInt(itsVariableRange) + 1;
	}
	
	public int genBytecodeIndex()
	{
		return itsRandom.nextInt(itsBytecodeRange);
	}
	
	public Object genObject()
	{
		return new ObjectId.ObjectUID(itsRandom.nextInt(itsObjectRange) + 1);
	}
	
	public Object[] genArgs()
	{
		int theCount = itsRandom.nextInt(10);
		Object[] theArgs = new Object[theCount];
		for (int i = 0; i < theArgs.length; i++) theArgs[i] = genObject();
		return theArgs;
	}

}