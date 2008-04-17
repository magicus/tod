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
package tod.impl.dbgrid.messages;

import static tod.impl.dbgrid.DebuggerGridConfig.MESSAGE_TYPE_BITS;
import tod.core.DebugFlags;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.RoleIndexSet;
import tod.impl.dbgrid.db.StdIndexSet;
import zz.utils.bit.BitStruct;
import zz.utils.bit.BitUtils;

/**
 * Base class for the events of DBGrid based on bit structs.
 * @author gpothier
 */
public abstract class BitGridEvent extends GridEvent
{
	private static final long serialVersionUID = 5405735108318929805L;

	public BitGridEvent(IStructureDatabase aStructureDatabase)
	{
		super(aStructureDatabase);
	}

	public BitGridEvent(IStructureDatabase aStructureDatabase, BitStruct aBitStruct)
	{
		this(aStructureDatabase);
		int theThread = aBitStruct.readInt(DebuggerGridConfig.EVENT_THREAD_BITS);
		int theDepth = aBitStruct.readInt(DebuggerGridConfig.EVENT_DEPTH_BITS);
		long theTimestamp = aBitStruct.readLong(DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		int[] theAdviceCFlow = readAdviceCFlow(aBitStruct);
		int theProbeId = aBitStruct.readInt(DebuggerGridConfig.EVENT_PROBEID_BITS);
		
		long theParentTimestamp = aBitStruct.readLong(DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		set(
				theThread, 
				theDepth,
				theTimestamp,
				theAdviceCFlow,
				theProbeId,
				theParentTimestamp);
	}
	
	private static int readShort(BitStruct aBitStruct, int aBits)
	{
		int theValue = aBitStruct.readInt(aBits);
		return theValue == BitUtils.pow2i(aBits)-1 ? -1 : theValue;
	}
	
	/**
	 * Writes out a representation of this event to a {@link BitStruct}.
	 * Subclasses must override this method to serialize their attributes, and
	 * must call super first.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
		aBitStruct.writeInt(getMessageType().ordinal(), MESSAGE_TYPE_BITS);
		aBitStruct.writeInt(getThread(), DebuggerGridConfig.EVENT_THREAD_BITS);
		aBitStruct.writeInt(getDepth(), DebuggerGridConfig.EVENT_DEPTH_BITS);
		aBitStruct.writeLong(getTimestamp(), DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		writeAdviceCFlow(aBitStruct, getAdviceCFlow());
		aBitStruct.writeInt(getProbeId(), DebuggerGridConfig.EVENT_PROBEID_BITS);
		
		aBitStruct.writeLong(getParentTimestamp(), DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
	}
	
	private static void writeAdviceCFlow(BitStruct aBitStruct, int[] aAdviceCFlow)
	{
		aBitStruct.writeInt(aAdviceCFlow != null ? aAdviceCFlow.length : 0, DebuggerGridConfig.EVENT_ADCFLOW_COUNT_BITS);
		if (aAdviceCFlow != null) 
		{
			for(int theSourceId : aAdviceCFlow) aBitStruct.writeInt(
					theSourceId, 
					DebuggerGridConfig.EVENT_ADVICE_SRC_ID_BITS);
		}
	}
	
	private static int[] readAdviceCFlow(BitStruct aBitStruct)
	{
		int theCount = aBitStruct.readInt(DebuggerGridConfig.EVENT_ADCFLOW_COUNT_BITS);
		if (theCount == 0) return null;
		
		int[] theCFlow = new int[theCount];
		for(int i=0;i<theCount;i++) theCFlow[i] = aBitStruct.readInt(DebuggerGridConfig.EVENT_ADVICE_SRC_ID_BITS);
		return theCFlow;
	}
	
	private static int coundAdviceCFlowBits(int[] aAdviceCFlow)
	{
		int theCount = DebuggerGridConfig.EVENT_ADCFLOW_COUNT_BITS;
		if (aAdviceCFlow != null) theCount += aAdviceCFlow.length * DebuggerGridConfig.EVENT_ADVICE_SRC_ID_BITS;
		return theCount;
	}
	
	/**
	 * Returns the number of bits necessary to serialize this event. Subclasses
	 * should override this method and sum the number of bits they need to the
	 * number of bits returned by super.
	 */
	public int getBitCount()
	{
		int theCount = MESSAGE_TYPE_BITS;
		theCount += DebuggerGridConfig.EVENT_THREAD_BITS;
		theCount += DebuggerGridConfig.EVENT_DEPTH_BITS;
		theCount += DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
		theCount += coundAdviceCFlowBits(getAdviceCFlow());
		theCount += DebuggerGridConfig.EVENT_PROBEID_BITS;
		theCount += DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
		
		return theCount;
	}
	
	
	private static StdIndexSet.StdTuple TUPLE = new StdIndexSet.StdTuple(-1, -1);
	private static RoleIndexSet.RoleTuple ROLE_TUPLE = new RoleIndexSet.RoleTuple(-1, -1, -1);
	
	
	/**
	 * Instructs this event to add relevant data to the indexes. The base
	 * version handles all common data; subclasses should override this method
	 * to index specific data.
	 * @param aPointer
	 *            The internal pointer to this event.
	 */
	public void index(Indexes aIndexes, long aPointer)
	{
		// We add the same tuple to all standard indexes so we create it now.
		TUPLE.set(getTimestamp(), aPointer);
		
		aIndexes.indexType((byte) getEventType().ordinal(), TUPLE);
		
		ProbeInfo theProbeInfo = getProbeInfo();
		
		if (! DebugFlags.DISABLE_LOCATION_INDEX)
		{
			if (theProbeInfo != null && theProbeInfo.bytecodeIndex >= 0)
				aIndexes.indexLocation(theProbeInfo.bytecodeIndex, TUPLE);
		}
		
		if (theProbeInfo != null && theProbeInfo.adviceSourceId >= 0)
		{
			aIndexes.indexAdviceSourceId(theProbeInfo.adviceSourceId, TUPLE);
		}
		
		if (getAdviceCFlow() != null)
		{
			for (int theSourceId : getAdviceCFlow()) aIndexes.indexAdviceCFlow(theSourceId, TUPLE);
		}
		
		if (theProbeInfo != null && theProbeInfo.role != null && theProbeInfo.role != BytecodeRole.UNKNOWN)
		{
			aIndexes.indexRole(theProbeInfo.role.ordinal()+1, TUPLE);
		}
		
		if (getThread() > 0) 
			aIndexes.indexThread(getThread(), TUPLE);
		
		aIndexes.indexDepth(getDepth(), TUPLE);
		
		ROLE_TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_BEHAVIOR_OPERATION);
		
		if (theProbeInfo != null && theProbeInfo.behaviorId >= 0)
			aIndexes.indexBehavior(theProbeInfo.behaviorId, ROLE_TUPLE);
	}

	/**
	 * Creates an event from a serialized representation, symmetric to
	 * {@link #writeTo(BitStruct)}.
	 */
	public static BitGridEvent read(IStructureDatabase aStructureDatabase, BitStruct aBitStruct)
	{
		MessageType theType = MessageType.VALUES[aBitStruct.readInt(MESSAGE_TYPE_BITS)];
		switch (theType)
		{
		case BEHAVIOR_EXIT:
			return new GridBehaviorExitEvent(aStructureDatabase, aBitStruct);
			
		case SUPER_CALL:
			return new GridBehaviorCallEvent(aStructureDatabase, aBitStruct, MessageType.SUPER_CALL);
			
		case EXCEPTION_GENERATED:
			return new GridExceptionGeneratedEvent(aStructureDatabase, aBitStruct);
			
		case FIELD_WRITE:
			return new GridFieldWriteEvent(aStructureDatabase, aBitStruct);
			
		case NEW_ARRAY:
			return new GridNewArrayEvent(aStructureDatabase, aBitStruct);
			
		case ARRAY_WRITE:
			return new GridArrayWriteEvent(aStructureDatabase, aBitStruct);
			
		case INSTANTIATION:
			return new GridBehaviorCallEvent(aStructureDatabase, aBitStruct, MessageType.INSTANTIATION);
			
		case LOCAL_VARIABLE_WRITE:
			return new GridVariableWriteEvent(aStructureDatabase, aBitStruct);
			
		case INSTANCEOF:
			return new GridInstanceOfEvent(aStructureDatabase, aBitStruct);
			
		case METHOD_CALL:
			return new GridBehaviorCallEvent(aStructureDatabase, aBitStruct, MessageType.METHOD_CALL);
			
		default: throw new RuntimeException("Not handled: "+theType); 
		}
	}
	

}
