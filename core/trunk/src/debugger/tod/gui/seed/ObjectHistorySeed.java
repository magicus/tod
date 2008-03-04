/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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
