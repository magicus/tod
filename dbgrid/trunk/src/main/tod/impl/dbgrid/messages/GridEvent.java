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

import java.io.Serializable;

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.impl.common.event.Event;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.queries.ArrayIndexCondition;
import tod.impl.dbgrid.queries.BehaviorCondition;
import tod.impl.dbgrid.queries.FieldCondition;
import tod.impl.dbgrid.queries.ObjectCondition;
import tod.impl.dbgrid.queries.VariableCondition;
import zz.utils.PublicCloneable;

public abstract class GridEvent extends PublicCloneable 
implements Serializable
{
	private transient IStructureDatabase itsStructureDatabase;
	
	/**
	 * We can find the parent event using only its timestamp,
	 * as it necessarily belongs to the same thread as this
	 * event
	 */
	private long itsParentTimestamp;
	private int itsThread;
	private int itsDepth;
	private long itsTimestamp;

	
	private int itsProbeId;
	private int[] itsAdviceCFlow;
	
	public GridEvent(IStructureDatabase aStructureDatabase)
	{
		itsStructureDatabase = aStructureDatabase;
	}

	protected void set(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp)
	{
		itsThread = aThread;
		itsDepth = aDepth;
		itsTimestamp = aTimestamp;
		itsAdviceCFlow = aAdviceCFlow;
		itsProbeId = aProbeId;
		itsParentTimestamp = aParentTimestamp;
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
		aEvent.setProbeId(getProbeId()); 
		aEvent.setAdviceCFlow(getAdviceCFlow());
		aEvent.setParentTimestamp(getParentTimestamp());
	}

	/**
	 * Should be used only when an event is deserialized.
	 */
	public void _setStructureDatabase(IStructureDatabase aStructureDatabase)
	{
		itsStructureDatabase = aStructureDatabase;
	}
	
	public IStructureDatabase getStructureDatabase()
	{
		return itsStructureDatabase;
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

	public int getProbeId()
	{
		return itsProbeId;
	}
	
	public ProbeInfo getProbeInfo()
	{
		return getProbeId() > 0 ?
				itsStructureDatabase.getProbeInfo(getProbeId())
				: null;
	}
	
	public int[] getAdviceCFlow()
	{
		return itsAdviceCFlow;
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
				"th: %d, pid: %d, t: %d",
				itsThread,
				itsProbeId,
				itsTimestamp); 
	}
}
