/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng;

import static tod.impl.evdbng.DebuggerGridConfigNG.STRUCTURE_ADVICE_SRC_ID_COUNT;
import static tod.impl.evdbng.DebuggerGridConfigNG.STRUCTURE_ARRAY_INDEX_COUNT;
import static tod.impl.evdbng.DebuggerGridConfigNG.STRUCTURE_BEHAVIOR_COUNT;
import static tod.impl.evdbng.DebuggerGridConfigNG.STRUCTURE_BYTECODE_LOCS_COUNT;
import static tod.impl.evdbng.DebuggerGridConfigNG.STRUCTURE_DEPTH_RANGE;
import static tod.impl.evdbng.DebuggerGridConfigNG.STRUCTURE_FIELD_COUNT;
import static tod.impl.evdbng.DebuggerGridConfigNG.STRUCTURE_OBJECT_COUNT;
import static tod.impl.evdbng.DebuggerGridConfigNG.STRUCTURE_THREADS_COUNT;
import static tod.impl.evdbng.DebuggerGridConfigNG.STRUCTURE_VAR_COUNT;

import java.util.Random;

import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ObjectId;
import tod.impl.dbgrid.EventGenerator;
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

public class EventGeneratorNG extends EventGenerator
{

	public EventGeneratorNG(
			IMutableStructureDatabase aStructureDatabase,
			long aSeed,
			int aThreadsRange, 
			int aDepthRange, 
			int aBytecodeRange, 
			int aBehaviorRange, 
			int aAdviceSourceIdRange, 
			int aFieldRange, 
			int aVariableRange, 
			int aObjectRange,
			int aArrayIndexRange)
	{
		super(aStructureDatabase, aSeed, aThreadsRange, aDepthRange, aBytecodeRange, aBehaviorRange, aAdviceSourceIdRange, aFieldRange, aVariableRange, aObjectRange, aArrayIndexRange);
	}

	public EventGeneratorNG(IMutableStructureDatabase aStructureDatabase, long aSeed)
	{
		this(
				aStructureDatabase,
				aSeed, 
				STRUCTURE_THREADS_COUNT,
				STRUCTURE_DEPTH_RANGE,
				STRUCTURE_BYTECODE_LOCS_COUNT,
				STRUCTURE_BEHAVIOR_COUNT,
				STRUCTURE_ADVICE_SRC_ID_COUNT,
				STRUCTURE_FIELD_COUNT,
				STRUCTURE_VAR_COUNT,
				STRUCTURE_OBJECT_COUNT,
				STRUCTURE_ARRAY_INDEX_COUNT);
	}
	
	@Override
	public GridEventNG next()
	{
		MessageType theType = genType();
		switch (theType)
		{
		case BEHAVIOR_EXIT:
			return new GridBehaviorExitEvent(
					getStructureDatabase(),
					genThreadId(),
					genDepth(),
					genTimestamp(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genBoolean(),
					genObject(),
					genBehaviorId());
			
		case SUPER_CALL:
			return new GridBehaviorCallEvent(
					getStructureDatabase(),
					genThreadId(),
					genDepth(),
					genTimestamp(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					MessageType.SUPER_CALL,
					genBoolean(),
					genArgs(),
					genBehaviorId(),
					genBehaviorId(),
					genObject());
			
		case EXCEPTION_GENERATED:
			return new GridExceptionGeneratedEvent(
					getStructureDatabase(),
					genThreadId(),
					genDepth(),
					genTimestamp(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genObject());
			
		case FIELD_WRITE:
			return new GridFieldWriteEvent(
					getStructureDatabase(),
					genThreadId(),
					genDepth(),
					genTimestamp(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genFieldId(),
					genObject(),
					genObject());
			
		case INSTANTIATION:
			return new GridBehaviorCallEvent(
					getStructureDatabase(),
					genThreadId(),
					genDepth(),
					genTimestamp(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					MessageType.INSTANTIATION,
					genBoolean(),
					genArgs(),
					genBehaviorId(),
					genBehaviorId(),
					genObject());
			
		case LOCAL_VARIABLE_WRITE:
			return new GridVariableWriteEvent(
					getStructureDatabase(),
					genThreadId(),
					genDepth(),
					genTimestamp(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genVariableId(),
					genObject());
			
		case METHOD_CALL:
			return new GridBehaviorCallEvent(
					getStructureDatabase(),
					genThreadId(),
					genDepth(),
					genTimestamp(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					MessageType.METHOD_CALL,
					genBoolean(),
					genArgs(),
					genBehaviorId(),
					genBehaviorId(),
					genObject());
		
		case ARRAY_WRITE:
			return new GridArrayWriteEvent(
					getStructureDatabase(),
					genThreadId(),
					genDepth(),
					genTimestamp(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genObject(),
					genArrayIndex(),
					genObject());
			
		case NEW_ARRAY:
			return new GridNewArrayEvent(
					getStructureDatabase(),
					genThreadId(),
					genDepth(),
					genTimestamp(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genObject(),
					genFieldId(),
					1000);
			
		case INSTANCEOF:
			return new GridInstanceOfEvent(
					getStructureDatabase(),
					genThreadId(),
					genDepth(),
					genTimestamp(),
					genAdviceCFlow(),
					genProbeId(),
					genParentTimestamp(),
					genObject(),
					genTypeId(),
					genBoolean());
			
		default: throw new RuntimeException("Not handled: "+theType); 
		}

	}

	

}