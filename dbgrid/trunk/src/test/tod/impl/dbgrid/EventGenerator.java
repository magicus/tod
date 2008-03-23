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

import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_ADVICE_SRC_ID_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_ARRAY_INDEX_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BEHAVIOR_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BYTECODE_LOCS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_DEPTH_RANGE;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_FIELD_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_OBJECT_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_THREADS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_VAR_COUNT;

import java.util.Random;

import tod.core.database.TimestampGenerator;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ObjectId;
import tod.impl.dbgrid.messages.BitGridEvent;
import tod.impl.dbgrid.messages.GridArrayWriteEvent;
import tod.impl.dbgrid.messages.GridBehaviorCallEvent;
import tod.impl.dbgrid.messages.GridBehaviorExitEvent;
import tod.impl.dbgrid.messages.GridExceptionGeneratedEvent;
import tod.impl.dbgrid.messages.GridFieldWriteEvent;
import tod.impl.dbgrid.messages.GridNewArrayEvent;
import tod.impl.dbgrid.messages.GridVariableWriteEvent;
import tod.impl.dbgrid.messages.MessageType;

public class EventGenerator
{
	private Random itsRandom;
	private TimestampGenerator itsTimestampGenerator;
	private TimestampGenerator itsParentTimestampGenerator;
	
	private IStructureDatabase itsStructureDatabase;
	
	private int itsThreadsRange;
	private int itsDepthRange;
	private int itsBytecodeRange;
	private int itsAdviceSourceIdRange;
	private int itsBehaviorRange;
	private int itsFieldRange;
	private int itsVariableRange;
	private int itsObjectRange;



	public EventGenerator(
			long aSeed,
			int aThreadsRange, 
			int aDepthRange, 
			int aBytecodeRange, 
			int aBehaviorRange, 
			int aAdviceSourceIdRange, 
			int aFieldRange, 
			int aVariableRange, 
			int aObjectRange)
	{
		itsRandom = new Random(aSeed);
		itsTimestampGenerator = new TimestampGenerator(aSeed);		
		itsParentTimestampGenerator = new TimestampGenerator(aSeed);		
		
		itsThreadsRange = aThreadsRange;
		itsDepthRange = aDepthRange;
		itsBytecodeRange = aBytecodeRange;
		itsBehaviorRange = aBehaviorRange;
		itsAdviceSourceIdRange = aAdviceSourceIdRange;
		itsFieldRange = aFieldRange;
		itsVariableRange = aVariableRange;
		itsObjectRange = aObjectRange;
	}

	public EventGenerator(long aSeed)
	{
		this(
				aSeed, 
				STRUCTURE_THREADS_COUNT,
				STRUCTURE_DEPTH_RANGE,
				STRUCTURE_BYTECODE_LOCS_COUNT,
				STRUCTURE_BEHAVIOR_COUNT,
				STRUCTURE_ADVICE_SRC_ID_COUNT,
				STRUCTURE_FIELD_COUNT,
				STRUCTURE_VAR_COUNT,
				STRUCTURE_OBJECT_COUNT);
	}
	
	public BitGridEvent next()
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
					null,
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
					null,
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
					null,
					genProbeId(),
					genParentTimestamp(),
					genObject());
			
		case FIELD_WRITE:
			return new GridFieldWriteEvent(
					itsStructureDatabase,
					genThreadId(),
					genDepth(),
					itsTimestampGenerator.next(),
					null,
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
					null,
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
					null,
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
					null,
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
					null,
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
					null,
					genProbeId(),
					genParentTimestamp(),
					genObject(),
					genFieldId(),
					1000);
			

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
	
	public int genProbeId()
	{
		return itsRandom.nextInt(itsBehaviorRange) + 1; // TODO: fix if necessary
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
	
	public int genAdviceSourceId()
	{
		return itsRandom.nextInt(itsAdviceSourceIdRange);
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