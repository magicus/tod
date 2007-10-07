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
package tod.gui.seed;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ObjectId;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;
import tod.gui.view.ObjectHistoryView;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * Seed for {@link ObjectHistoryView}.
 * The set of boolean properties are filters that permit to show or hide particular events
 * along two axes: the role of the object in the event, and the kind of event.
 * @author gpothier
 */
public class ObjectHistorySeed extends LogViewSeed
{
	private final ObjectId itsObject;
	
	/**
	 * Timestamp of the first event displayed by this view.
	 */
	private final IRWProperty<Long> pTimestamp = new SimpleRWProperty<Long>(this);
	
	/**
	 * Currently selected event.
	 */
	private final IRWProperty<ILogEvent> pSelectedEvent = new SimpleRWProperty<ILogEvent>(this);
	
	private final IRWProperty<Boolean> pShowRole_Target = new SimpleRWProperty<Boolean>(this);
	private final IRWProperty<Boolean> pShowRole_Value = new SimpleRWProperty<Boolean>(this);
	private final IRWProperty<Boolean> pShowRole_Arg = new SimpleRWProperty<Boolean>(this);
	private final IRWProperty<Boolean> pShowRole_Result = new SimpleRWProperty<Boolean>(this);
	
	private final IRWProperty<Boolean> pShowKind_BehaviorCall = new SimpleRWProperty<Boolean>(this);
	private final IRWProperty<Boolean> pShowKind_FieldWrite = new SimpleRWProperty<Boolean>(this);
	private final IRWProperty<Boolean> pShowKind_LocalWrite = new SimpleRWProperty<Boolean>(this);
	private final IRWProperty<Boolean> pShowKind_ArrayWrite = new SimpleRWProperty<Boolean>(this);
	private final IRWProperty<Boolean> pShowKind_Exception = new SimpleRWProperty<Boolean>(this);
	
	public ObjectHistorySeed(IGUIManager aGUIManager, ILogBrowser aLog, ObjectId aObject)
	{
		super(aGUIManager, aLog);
		itsObject = aObject;
		
		pShowRole_Target.set(true);
		pShowRole_Value.set(true);
		pShowRole_Arg.set(true);
		pShowRole_Result.set(true);
		
		pShowKind_BehaviorCall.set(true);
		pShowKind_FieldWrite.set(true);
		pShowKind_LocalWrite.set(true);
		pShowKind_ArrayWrite.set(true);
		pShowKind_Exception.set(true);
	}
	
	@Override
	protected LogView requestComponent()
	{
		ObjectHistoryView theView = new ObjectHistoryView(getGUIManager(), getLogBrowser(), this);
		theView.init();
		return theView;
	}

	public ObjectId getObject()
	{
		return itsObject;
	}
	
	public IRWProperty<Long> pTimestamp()
	{
		return pTimestamp;
	}
	
	public IRWProperty<ILogEvent> pSelectedEvent()
	{
		return pSelectedEvent;
	}
	
	public IRWProperty<Boolean> pShowRole_Target()
	{
		return pShowRole_Target;
	}
	
	public IRWProperty<Boolean> pShowRole_Value()
	{
		return pShowRole_Value;
	}
	
	public IRWProperty<Boolean> pShowRole_Arg()
	{
		return pShowRole_Arg;
	}
	
	public IRWProperty<Boolean> pShowRole_Result()
	{
		return pShowRole_Result;
	}
	
	public IRWProperty<Boolean> pShowKind_BehaviorCall()
	{
		return pShowKind_BehaviorCall;
	}
	
	public IRWProperty<Boolean> pShowKind_FieldWrite()
	{
		return pShowKind_FieldWrite;
	}
	
	public IRWProperty<Boolean> pShowKind_LocalWrite()
	{
		return pShowKind_LocalWrite;
	}
	
	public IRWProperty<Boolean> pShowKind_ArrayWrite()
	{
		return pShowKind_ArrayWrite;
	}
	
	public IRWProperty<Boolean> pShowKind_Exception()
	{
		return pShowKind_Exception;
	}
}
