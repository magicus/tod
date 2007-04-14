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

import tod.core.Output;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.IHostInfo;
import tod.impl.common.EventCollector;
import tod.impl.dbgrid.dispatch.AbstractEventDispatcher;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.messages.GridArrayWriteEvent;
import tod.impl.dbgrid.messages.GridBehaviorCallEvent;
import tod.impl.dbgrid.messages.GridBehaviorExitEvent;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridExceptionGeneratedEvent;
import tod.impl.dbgrid.messages.GridFieldWriteEvent;
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
	private final GridOutputEvent itsOutputEvent = new GridOutputEvent();
	private final GridVariableWriteEvent itsVariableWriteEvent = new GridVariableWriteEvent();
	
	
	public GridEventCollector(
			IHostInfo aHost,
			ILocationsRepository aLocationsRepository,
			DatabaseNode aDispatcher)
	{
		super(aHost, aLocationsRepository);
		itsDatabaseNode = aDispatcher;
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
	
	private int getHostId()
	{
		return getHost().getId();
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
				aParentTimestamp, 
				aException);
		
		dispatch(itsExceptionEvent);
	}


	public void behaviorExit(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
			int aBehaviorId,
			boolean aHasThrown, 
			Object aResult)
	{
		itsExitEvent.set(
				aThreadId, 
				aDepth, 
				aTimestamp, 
				aOperationBehaviorId,
				aOperationBytecodeIndex,
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
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
			int aFieldId,
			Object aTarget,
			Object aValue)
	{
		itsFieldWriteEvent.set(
				aThreadId, 
				aDepth,
				aTimestamp,
				aOperationBehaviorId,
				aOperationBytecodeIndex,
				aParentTimestamp,
				aFieldId, 
				aTarget, 
				aValue);
		
		dispatch(itsFieldWriteEvent);
	}
	
	public void arrayWrite(
			int aThreadId,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
			Object aTarget, 
			int aIndex, 
			Object aValue)
	{
		itsArrayWriteEvent.set(
				aThreadId, 
				aDepth,
				aTimestamp,
				aOperationBehaviorId,
				aOperationBytecodeIndex,
				aParentTimestamp,
				aTarget,
				aIndex,
				aValue);
		
		dispatch(itsArrayWriteEvent);
	}


	public void instantiation(
			int aThreadId,
			long aParentTimestamp,
			short aDepth, 
			long aTimestamp,
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
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
				aOperationBehaviorId,
				aOperationBytecodeIndex,
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
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
			int aVariableId,
			Object aValue)
	{
		itsVariableWriteEvent.set(
				aThreadId,
				aDepth, 
				aTimestamp,
				aOperationBehaviorId,
				aOperationBytecodeIndex,
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
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
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
				aOperationBehaviorId,
				aOperationBytecodeIndex,
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
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
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
				aOperationBehaviorId,
				aOperationBytecodeIndex,
				aParentTimestamp,
				MessageType.SUPER_CALL, 
				aDirectParent, 
				aArguments,
				aCalledBehavior,
				aExecutedBehavior,
				aTarget);
		
		dispatch(itsCallEvent);
	}

	public void register(long aObjectUID, Object aObject)
	{
		itsDatabaseNode.register(aObjectUID, aObject);
	}
}
