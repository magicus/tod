/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng;

import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_ADVICE_SRC_ID_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_ARRAY_INDEX_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_BEHAVIOR_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_BYTECODE_LOCS_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_DEPTH_RANGE;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_FIELD_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_OBJECT_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_THREADS_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_VAR_COUNT;

import java.util.Random;

import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ObjectId;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.evdbng.messages.GridArrayWriteEvent;
import tod.impl.evdbng.messages.GridBehaviorCallEvent;
import tod.impl.evdbng.messages.GridBehaviorExitEvent;
import tod.impl.evdbng.messages.GridEventNG;
import tod.impl.evdbng.messages.GridExceptionGeneratedEvent;
import tod.impl.evdbng.messages.GridFieldWriteEvent;
import tod.impl.evdbng.messages.GridInstanceOfEvent;
import tod.impl.evdbng.messages.GridNewArrayEvent;
import tod.impl.evdbng.messages.GridVariableWriteEvent;
import tod.impl.evdbng.test.TimestampGenerator;

public class EventGenerator
{
	private IMutableStructureDatabase itsStructureDatabase;
	
	private Random itsRandom;
	private TimestampGenerator itsTimestampGenerator;
	private TimestampGenerator itsParentTimestampGenerator;
	
	private int itsThreadsRange;
	private int itsDepthRange;
	private int itsBytecodeRange;
	private int itsAdviceSourceIdRange;
	private int itsBehaviorRange;
	private int itsFieldRange;
	private int itsProbeRange;
	private int itsVariableRange;
	private int itsObjectRange;



	public EventGenerator(
			IMutableStructureDatabase aStructureDatabase,
			long aSeed,
			int aThreadsRange, 
			int aDepthRange, 
			int aBytecodeRange, 
			int aAdviceSourceIdRange, 
			int aBehaviorRange, 
			int aFieldRange, 
			int aVariableRange, 
			int aObjectRange)
	{
		itsStructureDatabase = aStructureDatabase;
		itsRandom = new Random(aSeed);
		itsTimestampGenerator = new TimestampGenerator(aSeed);		
		itsParentTimestampGenerator = new TimestampGenerator(aSeed);		
		
		itsThreadsRange = aThreadsRange;
		itsDepthRange = aDepthRange;
		itsBytecodeRange = aBytecodeRange;
		itsAdviceSourceIdRange = aAdviceSourceIdRange;
		itsBehaviorRange = aBehaviorRange;
		itsFieldRange = aFieldRange;
		itsVariableRange = aVariableRange;
		itsObjectRange = aObjectRange;
		
		itsProbeRange = itsBehaviorRange*itsBytecodeRange/100;
		
		for(int i=0;i<itsProbeRange;i++)
		{
			itsStructureDatabase.addProbe(genBehaviorId(), genBytecodeIndex(), null, genAdviceSourceId());
		}
	}

	public EventGenerator(IMutableStructureDatabase aStructureDatabase, long aSeed)
	{
		this(
				aStructureDatabase,
				aSeed, 
				STRUCTURE_THREADS_COUNT,
				STRUCTURE_DEPTH_RANGE,
				STRUCTURE_BYTECODE_LOCS_COUNT,
				STRUCTURE_ADVICE_SRC_ID_COUNT,
				STRUCTURE_BEHAVIOR_COUNT,
				STRUCTURE_FIELD_COUNT,
				STRUCTURE_VAR_COUNT,
				STRUCTURE_OBJECT_COUNT);
	}
	
	public GridEventNG next()
	{
		MessageType theType = genType();
		switch (theType)
		{
		case BEHAVIOR_EXIT:
			return new GridBehaviorExitEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					itsRandom.nextBoolean(),
					genObject(),
					genBehaviorId());
			
		case SUPER_CALL:
			return new GridBehaviorCallEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					MessageType.SUPER_CALL,
					itsRandom.nextBoolean(),
					genArgs(),
					genBehaviorId(),
					genBehaviorId(),
					genObject());
			
		case EXCEPTION_GENERATED:
			return new GridExceptionGeneratedEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genObject());
			
		case FIELD_WRITE:
			return new GridFieldWriteEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genFieldId(),
					genObject(),
					genObject());
			
		case INSTANTIATION:
			return new GridBehaviorCallEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					MessageType.INSTANTIATION,
					itsRandom.nextBoolean(),
					genArgs(),
					genBehaviorId(),
					genBehaviorId(),
					genObject());
			
		case LOCAL_VARIABLE_WRITE:
			return new GridVariableWriteEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genVariableId(),
					genObject());
			
		case METHOD_CALL:
			return new GridBehaviorCallEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					MessageType.METHOD_CALL,
					itsRandom.nextBoolean(),
					genArgs(),
					genBehaviorId(),
					genBehaviorId(),
					genObject());
		
		case ARRAY_WRITE:
			return new GridArrayWriteEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genObject(),
					itsRandom.nextInt(STRUCTURE_ARRAY_INDEX_COUNT),
					genObject());
			
		case NEW_ARRAY:
			return new GridNewArrayEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genObject(),
					genFieldId(),
					1000);
			
		case INSTANCEOF:
			return new GridInstanceOfEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genObject(),
					genTypeId(),
					true);
			
		default: throw new RuntimeException("Not handled: "+theType); 
		}

	}

	public MessageType genType()
	{
		return MessageType.VALUES[itsRandom.nextInt(MessageType.VALUES.length-2)+1];
	}
	
	public long genParentTimestamp()
	{
		return itsParentTimestampGenerator.next();
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
	
	public int genBytecodeIndex()
	{
		return itsRandom.nextInt(itsBytecodeRange) + 1;
	}
	
	public int genTypeId()
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
	
	public int genProbeId()
	{
		return itsRandom.nextInt(itsProbeRange)+1;
	}
	
	public int genAdviceSourceId()
	{
		return itsRandom.nextInt(itsAdviceSourceIdRange) + 1;
	}
	
	public int[] genAdviceCFlow()
	{
		int theCount = itsRandom.nextInt(100);
		theCount -= 94;
		if (theCount < 0) return null;
		int[] theResult = new int[theCount];
		for(int i=0;i<theCount;i++) theResult[i] = genAdviceSourceId();
		return theResult;
	}
	
	public Object genObject()
	{
		return new ObjectId(itsRandom.nextInt(itsObjectRange) + 1);
	}
	
	public Object[] genArgs()
	{
		int theCount = itsRandom.nextInt(10);
		Object[] theArgs = new Object[theCount];
		for (int i = 0; i < theArgs.length; i++) theArgs[i] = genObject();
		return theArgs;
	}

}