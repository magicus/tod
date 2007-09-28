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

import tod.agent.DebugFlags;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;
import tod.impl.common.event.Event;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.RoleIndexSet;
import tod.impl.dbgrid.db.StdIndexSet;
import tod.impl.dbgrid.queries.ArrayIndexCondition;
import tod.impl.dbgrid.queries.BehaviorCondition;
import tod.impl.dbgrid.queries.FieldCondition;
import tod.impl.dbgrid.queries.ObjectCondition;
import tod.impl.dbgrid.queries.VariableCondition;
import zz.utils.bit.BitStruct;
import zz.utils.bit.BitUtils;

public abstract class GridEvent extends GridMessage
{
	static
	{
		System.out.println("GridEvent loaded by: "+GridEvent.class.getClassLoader());
	}
	
	
	/**
	 * We can find the parent event using only its timestamp,
	 * as it necessarily belongs to the same thread as this
	 * event
	 */
	private long itsParentTimestamp;
	private int itsThread;
	private int itsDepth;
	private long itsTimestamp;
	
	private int itsOperationBehaviorId;
	private int itsOperationBytecodeIndex;
	
	public GridEvent()
	{
	}

	public GridEvent(BitStruct aBitStruct)
	{
		itsThread = aBitStruct.readInt(DebuggerGridConfig.EVENT_THREAD_BITS);
		itsDepth = aBitStruct.readInt(DebuggerGridConfig.EVENT_DEPTH_BITS);
		itsTimestamp = aBitStruct.readLong(DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		itsOperationBehaviorId = readShort(aBitStruct, DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		itsOperationBytecodeIndex = readShort(aBitStruct, DebuggerGridConfig.EVENT_BYTECODE_LOCATION_BITS);
		
		itsParentTimestamp = aBitStruct.readLong(DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
	}
	
	private static int readShort(BitStruct aBitStruct, int aBits)
	{
		int theValue = aBitStruct.readInt(aBits);
		return theValue == BitUtils.pow2i(aBits)-1 ? -1 : theValue;
	}
	
	protected void set(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBehaviorId,
			int aOperationBytecodeIndex, 
			long aParentTimestamp)
	{
		itsThread = aThread;
		itsDepth = aDepth;
		itsTimestamp = aTimestamp;
		itsOperationBehaviorId = aOperationBehaviorId;
		if (itsOperationBehaviorId == 65535)
		{
			System.out.println("GridEvent.set()");
		}
		itsOperationBytecodeIndex = aOperationBytecodeIndex;
		itsParentTimestamp = aParentTimestamp;
	}

	/**
	 * Writes out a representation of this event to a {@link BitStruct}.
	 * Subclasses must override this method to serialize their attributes, and
	 * must call super first.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		aBitStruct.writeInt(getThread(), DebuggerGridConfig.EVENT_THREAD_BITS);
		aBitStruct.writeInt(getDepth(), DebuggerGridConfig.EVENT_DEPTH_BITS);
		aBitStruct.writeLong(getTimestamp(), DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		aBitStruct.writeInt(getOperationBehaviorId(), DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		aBitStruct.writeInt(getOperationBytecodeIndex(), DebuggerGridConfig.EVENT_BYTECODE_LOCATION_BITS);
		
		aBitStruct.writeLong(getParentTimestamp(), DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
	}
	
	/**
	 * Returns the number of bits necessary to serialize this event. Subclasses
	 * should override this method and sum the number of bits they need to the
	 * number of bits returned by super.
	 */
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		theCount += DebuggerGridConfig.EVENT_THREAD_BITS;
		theCount += DebuggerGridConfig.EVENT_DEPTH_BITS;
		theCount += DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
		theCount += DebuggerGridConfig.EVENT_BEHAVIOR_BITS;
		theCount += DebuggerGridConfig.EVENT_BYTECODE_LOCATION_BITS;
		theCount += DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
		
		return theCount;
	}
	
	/**
	 * Transforms this event into a {@link ILogEvent}
	 */
	public abstract ILogEvent toLogEvent(GridLogBrowser aBrowser);
	
	/**
	 * Initializes common event fields. Subclasses can use this method in the
	 * implementation of {@link #toLogEvent(GridLogBrowser)}
	 */
	protected void initEvent(GridLogBrowser aBrowser, Event aEvent)
	{
		IThreadInfo theThread = aBrowser.getThread(getThread());
		assert theThread != null;
		aEvent.setThread(theThread);
		aEvent.setTimestamp(getTimestamp());
		aEvent.setDepth(getDepth());
		aEvent.setOperationBehavior(aBrowser.getStructureDatabase().getBehavior(getOperationBehaviorId(), true));
		aEvent.setOperationBytecodeIndex(getOperationBytecodeIndex());
		aEvent.setParentTimestamp(getParentTimestamp());
	}
	
	/**
	 * Returns the type of this event.
	 */
	public abstract MessageType getEventType();
	
	/**
	 * Returns the type of this event.
	 */
	public final MessageType getMessageType()
	{
		return getEventType();
	}
	
	public int getOperationBytecodeIndex()
	{
		return itsOperationBytecodeIndex;
	}

	public int getOperationBehaviorId()
	{
		return itsOperationBehaviorId;
	}

	public long getParentTimestamp()
	{
		return itsParentTimestamp;
	}

	public int getThread()
	{
		return itsThread;
	}

	public int getDepth()
	{
		return itsDepth;
	}

	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	private static StdIndexSet.StdTuple TUPLE = new StdIndexSet.StdTuple(-1, -1);
	private static RoleIndexSet.RoleTuple ROLE_TUPLE = new RoleIndexSet.RoleTuple(-1, -1, -1);
	
	/**
	 * Instructs this event to add relevant data to the indexes. The base
	 * version handles all common data; subclasses should override this method
	 * to index specific data.
	 * 
	 * @param aPointer
	 *            The internal pointer to this event.
	 */
	public void index(Indexes aIndexes, long aPointer)
	{
		// We add the same tuple to all standard indexes so we create it now.
		TUPLE.set(getTimestamp(), aPointer);
		
		aIndexes.indexType((byte) getEventType().ordinal(), TUPLE);
		
		if (! DebugFlags.DISABLE_LOCATION_INDEX)
		{
			if (getOperationBytecodeIndex() >= 0)
				aIndexes.indexLocation(getOperationBytecodeIndex(), TUPLE);
		}
		
		if (getThread() > 0) 
			aIndexes.indexThread(getThread(), TUPLE);
		
		aIndexes.indexDepth(getDepth(), TUPLE);
		
		ROLE_TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_BEHAVIOR_OPERATION);
		
		if (getOperationBehaviorId() >= 0)
			aIndexes.indexBehavior(getOperationBehaviorId(), ROLE_TUPLE);
	}

	/**
	 * Whether this event matches a {@link BehaviorCondition}
	 */
	public boolean matchBehaviorCondition(int aBehaviorId, byte aRole)
	{
		return false;
	}
	
	/**
	 * Whether this event matches a {@link FieldCondition}
	 */
	public boolean matchFieldCondition(int aFieldId)
	{
		return false;
	}
	
	/**
	 * Whether this event matches a {@link ArrayIndexCondition}
	 */
	public boolean matchIndexCondition(int aPart, int aPartialKey)
	{
		return false;
	}
	
	/**
	 * Whether this event matches a {@link VariableCondition}
	 */
	public boolean matchVariableCondition(int aVariableId)
	{
		return false;
	}
	
	/**
	 * Whether this event matches a {@link ObjectCondition}
	 */
	public boolean matchObjectCondition(int aPart, int aPartialKey, byte aRole)
	{
		return false;
	}
	
	/**
	 * Internal version of toString, used by subclasses.
	 */
	protected String toString0()
	{
		return String.format(
				"th: %d, bc: %d, t: %d",
				itsThread,
				itsOperationBytecodeIndex,
				itsTimestamp); 
	}
}
