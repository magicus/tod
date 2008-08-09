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

import static tod.impl.dbgrid.DebuggerGridConfig1.STRUCTURE_ADVICE_SRC_ID_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig1.STRUCTURE_ARRAY_INDEX_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig1.STRUCTURE_BEHAVIOR_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig1.STRUCTURE_BYTECODE_LOCS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig1.STRUCTURE_DEPTH_RANGE;
import static tod.impl.dbgrid.DebuggerGridConfig1.STRUCTURE_FIELD_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig1.STRUCTURE_OBJECT_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig1.STRUCTURE_THREADS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig1.STRUCTURE_VAR_COUNT;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.impl.dbgrid.messages.BitGridEvent;
import tod.impl.dbgrid.messages.GridArrayWriteEvent;
import tod.impl.dbgrid.messages.GridBehaviorCallEvent;
import tod.impl.dbgrid.messages.GridBehaviorExitEvent;
import tod.impl.dbgrid.messages.GridExceptionGeneratedEvent;
import tod.impl.dbgrid.messages.GridFieldWriteEvent;
import tod.impl.dbgrid.messages.GridInstanceOfEvent;
import tod.impl.dbgrid.messages.GridNewArrayEvent;
import tod.impl.dbgrid.messages.GridVariableWriteEvent;
import tod.impl.dbgrid.messages.MessageType;

public class EventGenerator1 extends EventGenerator
{
	public EventGenerator1(IMutableStructureDatabase aStructureDatabase, long aSeed)
	{
		super(
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
	
	public EventGenerator1(IMutableStructureDatabase aStructureDatabase, long aSeed, int aThreadsRange, int aDepthRange,
			int aBytecodeRange, int aBehaviorRange, int aAdviceSourceIdRange,
			int aFieldRange, int aVariableRange, int aObjectRange,
			int aArrayIndexRange)
	{
		super(aStructureDatabase, aSeed, aThreadsRange, aDepthRange, aBytecodeRange, aBehaviorRange,
				aAdviceSourceIdRange, aFieldRange, aVariableRange, aObjectRange,
				aArrayIndexRange);
	}

	public BitGridEvent next()
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