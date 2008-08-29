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
package tod.impl.evdb1;

import tod.agent.Output;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.impl.dbgrid.GridEventCollector;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.db.DatabaseNode;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.evdb1.messages.GridArrayWriteEvent;
import tod.impl.evdb1.messages.GridBehaviorCallEvent;
import tod.impl.evdb1.messages.GridBehaviorExitEvent;
import tod.impl.evdb1.messages.GridExceptionGeneratedEvent;
import tod.impl.evdb1.messages.GridFieldWriteEvent;
import tod.impl.evdb1.messages.GridInstanceOfEvent;
import tod.impl.evdb1.messages.GridNewArrayEvent;
import tod.impl.evdb1.messages.GridOutputEvent;
import tod.impl.evdb1.messages.GridVariableWriteEvent;
import tod.utils.TODUtils;

/**
 * Event collector for the grid database backend. It handles events from a
 * single hosts, preprocesses them and sends them to the
 * {@link AbstractEventDispatcher}. Preprocessing is minimal and only involves
 * packaging. This class is not thread-safe.
 * 
 * @author gpothier
 */
public class GridEventCollector1 extends GridEventCollector
{
	private final DatabaseNode itsDatabaseNode;

	/**
	 * Number of events received by this collector
	 */
	private long itsEventsCount;

	/**
	 * We keep an instance of each kind of event. As events are received their
	 * attributes are copied to the appropriate instance, and the instance is
	 * sent to the dispatcher, which should immediately either serializes it or
	 * clone it.
	 */
	private final GridBehaviorCallEvent itsCallEvent;

	private final GridBehaviorExitEvent itsExitEvent;

	private final GridExceptionGeneratedEvent itsExceptionEvent;

	private final GridFieldWriteEvent itsFieldWriteEvent;

	private final GridArrayWriteEvent itsArrayWriteEvent;

	private final GridNewArrayEvent itsNewArrayEvent;

	private final GridInstanceOfEvent itsInstanceOfEvent;

	private final GridOutputEvent itsOutputEvent;

	private final GridVariableWriteEvent itsVariableWriteEvent;

	private final IMutableStructureDatabase itsStructureDatabase;

	public GridEventCollector1(
			RIGridMaster aMaster,
			IHostInfo aHost,
			IMutableStructureDatabase aStructureDatabase,
			DatabaseNode aDispatcher)
	{
		super(aMaster, aHost, aStructureDatabase);
		itsDatabaseNode = aDispatcher;
		itsStructureDatabase = aStructureDatabase;

		itsCallEvent = new GridBehaviorCallEvent(itsStructureDatabase);
		itsExitEvent = new GridBehaviorExitEvent(itsStructureDatabase);
		itsExceptionEvent = new GridExceptionGeneratedEvent(itsStructureDatabase);
		itsFieldWriteEvent = new GridFieldWriteEvent(itsStructureDatabase);
		itsArrayWriteEvent = new GridArrayWriteEvent(itsStructureDatabase);
		itsNewArrayEvent = new GridNewArrayEvent(itsStructureDatabase);
		itsInstanceOfEvent = new GridInstanceOfEvent(itsStructureDatabase);
		itsOutputEvent = new GridOutputEvent(itsStructureDatabase);
		itsVariableWriteEvent = new GridVariableWriteEvent(itsStructureDatabase);

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
			int[] aAdviceCFlow,
			int aBehaviorId,
			int aOperationBytecodeIndex,
			Object aException)
	{
		ProbeInfo theProbeInfo =
				itsStructureDatabase.getNewExceptionProbe(aBehaviorId, aOperationBytecodeIndex);

		exception(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, theProbeInfo.id, aException);
	}

	public void exception(
			int aThreadId,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int[] aAdviceCFlow,
			int aProbeId,
			Object aException)
	{
		TODUtils.logf(1, "GridEventCollector.exception()");

		itsExceptionEvent.set(
				aThreadId,
				aDepth,
				aTimestamp,
				aAdviceCFlow,
				aProbeId,
				aParentTimestamp,
				aException);

		dispatch(itsExceptionEvent);
	}

	public void behaviorExit(
			int aThreadId,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int[] aAdviceCFlow,
			int aProbeId,
			int aBehaviorId,
			boolean aHasThrown,
			Object aResult)
	{
		itsExitEvent.set(
				aThreadId,
				aDepth,
				aTimestamp,
				aAdviceCFlow,
				aProbeId,
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
			int[] aAdviceCFlow,
			int aProbeId,
			int aFieldId,
			Object aTarget,
			Object aValue)
	{
		itsFieldWriteEvent.set(
				aThreadId,
				aDepth,
				aTimestamp,
				aAdviceCFlow,
				aProbeId,
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
			int[] aAdviceCFlow,
			int aProbeId,
			Object aTarget,
			int aBaseTypeId,
			int aSize)
	{
		itsNewArrayEvent.set(
				aThreadId,
				aDepth,
				aTimestamp,
				aAdviceCFlow,
				aProbeId,
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
			int[] aAdviceCFlow,
			int aProbeId,
			Object aTarget,
			int aIndex,
			Object aValue)
	{
		itsArrayWriteEvent.set(
				aThreadId,
				aDepth,
				aTimestamp,
				aAdviceCFlow,
				aProbeId,
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
			int[] aAdviceCFlow,
			int aProbeId,
			Object aObject,
			int aTypeId,
			boolean aResult)
	{
		itsInstanceOfEvent.set(
				aThreadId,
				aDepth,
				aTimestamp,
				aAdviceCFlow,
				aProbeId,
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
			int[] aAdviceCFlow,
			int aProbeId,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments)
	{
		itsCallEvent.set(
				aThreadId,
				aDepth,
				aTimestamp,
				aAdviceCFlow,
				aProbeId,
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
			int[] aAdviceCFlow,
			int aProbeId,
			int aVariableId,
			Object aValue)
	{
		itsVariableWriteEvent.set(
				aThreadId,
				aDepth,
				aTimestamp,
				aAdviceCFlow,
				aProbeId,
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
			int[] aAdviceCFlow,
			int aProbeId,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments)
	{
		itsCallEvent.set(
				aThreadId,
				aDepth,
				aTimestamp,
				aAdviceCFlow,
				aProbeId,
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
			int[] aAdviceCFlow,
			Output aOutput,
			byte[] aData)
	{
		throw new UnsupportedOperationException();
	}

	public void superCall(
			int aThreadId,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int[] aAdviceCFlow,
			int aProbeId,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments)
	{
		itsCallEvent.set(
				aThreadId,
				aDepth,
				aTimestamp,
				aAdviceCFlow,
				aProbeId,
				aParentTimestamp,
				MessageType.SUPER_CALL,
				aDirectParent,
				aArguments,
				aCalledBehavior,
				aExecutedBehavior,
				aTarget);

		dispatch(itsCallEvent);
	}

	public void register(long aObjectUID, byte[] aData, long aTimestamp, boolean aIndexable)
	{
		itsDatabaseNode.register(aObjectUID, aData, aTimestamp, aIndexable);
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
