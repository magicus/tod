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

import tod.agent.Output;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.impl.common.EventCollector;
import tod.impl.dbgrid.dispatch.AbstractEventDispatcher;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.messages.GridArrayWriteEvent;
import tod.impl.dbgrid.messages.GridBehaviorCallEvent;
import tod.impl.dbgrid.messages.GridBehaviorExitEvent;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridExceptionGeneratedEvent;
import tod.impl.dbgrid.messages.GridFieldWriteEvent;
import tod.impl.dbgrid.messages.GridInstanceOfEvent;
import tod.impl.dbgrid.messages.GridNewArrayEvent;
import tod.impl.dbgrid.messages.GridOutputEvent;
import tod.impl.dbgrid.messages.GridVariableWriteEvent;
import tod.impl.dbgrid.messages.MessageType;

/**
 * Event collector for the grid database backend. It handles events from a single
 * hosts, preprocesses them and sends them to the {@link AbstractEventDispatcher}.
 * Preprocessing is minimal and only involves packaging.
 * This class is not thread-safe.
 * @author gpothier
 */
public class GridEventCollector extends EventCollector
{
	private final DatabaseNode itsDatabaseNode;
	
	/**
	 * Number of events received by this collector
	 */
	private long itsEventsCount;
	
	/**
	 * We keep an instance of each kind of event.
	 * As events are received their attributes are copied to the appropriate
	 * instance, and the instance is sent to the dispatcher, which
	 * should immediately either serializes it or clone it.
	 */
	private final GridBehaviorCallEvent itsCallEvent = new GridBehaviorCallEvent();
	private final GridBehaviorExitEvent itsExitEvent = new GridBehaviorExitEvent();
	private final GridExceptionGeneratedEvent itsExceptionEvent = new GridExceptionGeneratedEvent();
	private final GridFieldWriteEvent itsFieldWriteEvent = new GridFieldWriteEvent();
	private final GridArrayWriteEvent itsArrayWriteEvent = new GridArrayWriteEvent();
	private final GridNewArrayEvent itsNewArrayEvent = new GridNewArrayEvent();
	private final GridInstanceOfEvent itsInstanceOfEvent = new GridInstanceOfEvent();
	private final GridOutputEvent itsOutputEvent = new GridOutputEvent();
	private final GridVariableWriteEvent itsVariableWriteEvent = new GridVariableWriteEvent();
	private final IStructureDatabase itsStructureDatabase;
	
	
	public GridEventCollector(
			IHostInfo aHost,
			IStructureDatabase aStructureDatabase,
			DatabaseNode aDispatcher)
	{
		super(aHost, aStructureDatabase);
		itsDatabaseNode = aDispatcher;
		itsStructureDatabase = itsDatabaseNode.getStructureDatabase();
	}

	private void dispatch(GridEvent aEvent)
	{
		itsDatabaseNode.pushEvent(aEvent);
		itsEventsCount++;
	}
	
	/**
	 * Returns the number of events received by this collector.
	 */
	public long getEventsCount()
	{
		return itsEventsCount;
	}
	
	/**
	 * Returns the probe info corresponding to the given probe id.
	 */
	private final ProbeInfo getProbeInfo(int aProbeId)
	{
		if (aProbeId == -1) return ProbeInfo.NULL;
		else return itsStructureDatabase.getProbeInfo(aProbeId);
	}

	@Override
	protected void exception(
			int aThreadId,
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp,
			int aBehaviorId,
			int aOperationBytecodeIndex,
			Object aException)
	{
		System.out.println("GridEventCollector.exception()");
		itsExceptionEvent.set(
				aThreadId, 
				aDepth,
				aTimestamp,
				aBehaviorId,
				aOperationBytecodeIndex,
				-1, //TODO: retrieve the tag from the structure database.
				aParentTimestamp, 
				aException);
		
		dispatch(itsExceptionEvent);
	}


	public void behaviorExit(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int aAdviceCFlow,
			int aProbeId,
			int aBehaviorId, 
			boolean aHasThrown, Object aResult)
	{
		ProbeInfo theProbeInfo = getProbeInfo(aProbeId);
		
		itsExitEvent.set(
				aThreadId, 
				aDepth, 
				aTimestamp, 
				theProbeInfo.behaviorId,
				theProbeInfo.bytecodeIndex,
				theProbeInfo.adviceSourceId > 0 ? theProbeInfo.adviceSourceId : aAdviceCFlow,
				aParentTimestamp, 
				aHasThrown,
				aResult,
				aBehaviorId);
		
		dispatch(itsExitEvent);
	}


	public void fieldWrite(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aAdviceCFlow,
			int aProbeId,
			int aFieldId,
			Object aTarget, Object aValue)
	{
		ProbeInfo theProbeInfo = getProbeInfo(aProbeId);

		itsFieldWriteEvent.set(
				aThreadId, 
				aDepth,
				aTimestamp,
				theProbeInfo.behaviorId,
				theProbeInfo.bytecodeIndex,
				theProbeInfo.adviceSourceId > 0 ? theProbeInfo.adviceSourceId : aAdviceCFlow,
				aParentTimestamp,
				aFieldId, 
				aTarget, 
				aValue);
		
		dispatch(itsFieldWriteEvent);
	}
	
	
	
	public void newArray(
			int aThreadId,
			long aParentTimestamp,
			short aDepth, 
			long aTimestamp, 
			int aAdviceCFlow,
			int aProbeId, 
			Object aTarget,
			int aBaseTypeId, int aSize)
	{
		ProbeInfo theProbeInfo = getProbeInfo(aProbeId);

		itsNewArrayEvent.set(
				aThreadId, 
				aDepth,
				aTimestamp,
				theProbeInfo.behaviorId,
				theProbeInfo.bytecodeIndex,
				theProbeInfo.adviceSourceId > 0 ? theProbeInfo.adviceSourceId : aAdviceCFlow,
				aParentTimestamp,
				aTarget,
				aBaseTypeId,
				aSize);
		
		dispatch(itsNewArrayEvent);
	}

	public void arrayWrite(
			int aThreadId,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aAdviceCFlow,
			int aProbeId, 
			Object aTarget, 
			int aIndex, Object aValue)
	{
		ProbeInfo theProbeInfo = getProbeInfo(aProbeId);

		itsArrayWriteEvent.set(
				aThreadId, 
				aDepth,
				aTimestamp,
				theProbeInfo.behaviorId,
				theProbeInfo.bytecodeIndex,
				theProbeInfo.adviceSourceId > 0 ? theProbeInfo.adviceSourceId : aAdviceCFlow,
				aParentTimestamp,
				aTarget,
				aIndex,
				aValue);
		
		dispatch(itsArrayWriteEvent);
	}


	public void instanceOf(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int aAdviceCFlow,
			int aProbeId,
			Object aObject, 
			int aTypeId,
			boolean aResult)
	{
		ProbeInfo theProbeInfo = getProbeInfo(aProbeId);

		itsInstanceOfEvent.set(
				aThreadId, 
				aDepth,
				aTimestamp,
				theProbeInfo.behaviorId,
				theProbeInfo.bytecodeIndex,
				theProbeInfo.adviceSourceId > 0 ? theProbeInfo.adviceSourceId : aAdviceCFlow,
				aParentTimestamp,
				aObject,
				aTypeId,
				aResult);
		
		dispatch(itsInstanceOfEvent);
	}

	public void instantiation(
			int aThreadId,
			long aParentTimestamp,
			short aDepth, 
			long aTimestamp,
			int aAdviceCFlow,
			int aProbeId,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget, Object[] aArguments)
	{
		ProbeInfo theProbeInfo = getProbeInfo(aProbeId);

		itsCallEvent.set(
				aThreadId, 
				aDepth, 
				aTimestamp,
				theProbeInfo.behaviorId,
				theProbeInfo.bytecodeIndex,
				aAdviceCFlow,
				aParentTimestamp,
				MessageType.INSTANTIATION, 
				aDirectParent, 
				aArguments,
				aCalledBehavior,
				aExecutedBehavior,
				aTarget);
		
		dispatch(itsCallEvent);
	}


	public void localWrite(
			int aThreadId,
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp,
			int aAdviceCFlow,
			int aProbeId,
			int aVariableId, Object aValue)
	{
		ProbeInfo theProbeInfo = getProbeInfo(aProbeId);
		assert theProbeInfo.behaviorId >= 0 : "Probe has no behavior: "+aProbeId;

		itsVariableWriteEvent.set(
				aThreadId,
				aDepth, 
				aTimestamp,
				theProbeInfo.behaviorId,
				theProbeInfo.bytecodeIndex,
				theProbeInfo.adviceSourceId > 0 ? theProbeInfo.adviceSourceId : aAdviceCFlow,
				aParentTimestamp,
				aVariableId, 
				aValue);
		
		dispatch(itsVariableWriteEvent);
	}


	public void methodCall(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aAdviceCFlow,
			int aProbeId,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget, Object[] aArguments)
	{
		ProbeInfo theProbeInfo = getProbeInfo(aProbeId);

		itsCallEvent.set(
				aThreadId, 
				aDepth, 
				aTimestamp,
				theProbeInfo.behaviorId,
				theProbeInfo.bytecodeIndex,
				aAdviceCFlow,
				aParentTimestamp,
				MessageType.METHOD_CALL, 
				aDirectParent, 
				aArguments,
				aCalledBehavior,
				aExecutedBehavior,
				aTarget);
		
		dispatch(itsCallEvent);
	}


	public void output(
			int aThreadId,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aAdviceCFlow,
			Output aOutput, byte[] aData)
	{
		throw new UnsupportedOperationException();
	}


	public void superCall(
			int aThreadId,
			long aParentTimestamp,
			short aDepth, 
			long aTimestamp,
			int aAdviceCFlow,
			int aProbeId,
			boolean aDirectParent, 
			int aCalledBehavior,
			int aExecutedBehavior, 
			Object aTarget, Object[] aArguments)
	{
		ProbeInfo theProbeInfo = getProbeInfo(aProbeId);

		itsCallEvent.set(
				aThreadId, 
				aDepth, 
				aTimestamp,
				theProbeInfo.behaviorId,
				theProbeInfo.bytecodeIndex,
				aAdviceCFlow,
				aParentTimestamp,
				MessageType.SUPER_CALL, 
				aDirectParent, 
				aArguments,
				aCalledBehavior,
				aExecutedBehavior,
				aTarget);
		
		dispatch(itsCallEvent);
	}

	public void register(long aObjectUID, Object aObject, long aTimestamp)
	{
		itsDatabaseNode.register(aObjectUID, aObject, aTimestamp);
	}

	public void clear()
	{
		itsDatabaseNode.clear();
	}

	public int flush()
	{
		return itsDatabaseNode.flush();
	}
	
	
}
