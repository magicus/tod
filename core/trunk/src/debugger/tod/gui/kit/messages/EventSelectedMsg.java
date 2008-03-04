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
package tod.gui.kit.messages;

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ObjectId;

/**
 * This message is sent when an event is selected in an event list.
 * @author gpothier
 */
public class EventSelectedMsg extends Message
{
	public static final String ID = "tod.eventSelected";

	/**
	 * The selected event.
	 */
	private final ILogEvent itsEvent;
	
	/**
	 * The way the event was selected (selection, stepping...)
	 */
	private final SelectionMethod itsSelectionMethod;
	
	public EventSelectedMsg(ILogEvent aEvent, SelectionMethod aMethod)
	{
		super(ID);
		itsEvent = aEvent;
		itsSelectionMethod = aMethod;
	}

	public ILogEvent getEvent()
	{
		return itsEvent;
	}

	public SelectionMethod getSelectionMethod()
	{
		return itsSelectionMethod;
	}

	public static abstract class SelectionMethod
	{
		public static final SelectionMethod FORWARD_STEP_INTO = new SM_ForwardStepInto();
		public static final SelectionMethod FORWARD_STEP_OVER = new SM_ForwardStepOver();
		public static final SelectionMethod BACKWARD_STEP_INTO = new SM_BackwardStepInto();
		public static final SelectionMethod BACKWARD_STEP_OVER = new SM_BackwardStepOver();
		public static final SelectionMethod STEP_OUT = new SM_StepOut();
		public static final SelectionMethod SELECT_IN_LIST = new SM_SelectInList();
		public static final SelectionMethod SELECT_IN_CALL_STACK = new SM_SelectInCallStack();
	}
	
	public static class SM_ForwardStepInto extends SelectionMethod
	{
		private SM_ForwardStepInto()
		{
		}
	}
	
	public static class SM_ForwardStepOver extends SelectionMethod
	{
		private SM_ForwardStepOver()
		{
		}
	}
	
	public static class SM_BackwardStepInto extends SelectionMethod
	{
		private SM_BackwardStepInto()
		{
		}
	}
	
	public static class SM_BackwardStepOver extends SelectionMethod
	{
		private SM_BackwardStepOver()
		{
		}
	}

	public static class SM_StepOut extends SelectionMethod
	{
		private SM_StepOut()
		{
		}
	}
	
	public static class SM_SelectInList extends SelectionMethod
	{
		private SM_SelectInList()
		{
		}
	}
	
	public static class SM_SelectInCallStack extends SelectionMethod
	{
		private SM_SelectInCallStack()
		{
		}
	}
	
	public static class SM_JumpFromWhyLink extends SelectionMethod
	{
		private ObjectId itsObject;
		private IFieldInfo itsField;
		
		public SM_JumpFromWhyLink(ObjectId aObject, IFieldInfo aField)
		{
			itsObject = aObject;
			itsField = aField;
		}
	}

	public static class SM_SelectInBookmarks extends SelectionMethod
	{
		private String itsName;

		public SM_SelectInBookmarks(String aName)
		{
			itsName = aName;
		}
	}
	
	public static class SM_SelectInHistory extends SelectionMethod
	{
		private EventSelectedMsg itsSelectedMessage;

		public SM_SelectInHistory(EventSelectedMsg aSelectedMessage)
		{
			itsSelectedMessage = aSelectedMessage;
		}
	}
	
	public static abstract class SM_ShowForLine extends SelectionMethod
	{
		private IBehaviorInfo itsBehavior;
		private int itsLineNumber;
		
		public SM_ShowForLine(IBehaviorInfo aBehavior, int aLineNumber)
		{
			itsBehavior = aBehavior;
			itsLineNumber = aLineNumber;
		}
		
	}

	public static class SM_ShowNextForLine extends SM_ShowForLine
	{
		public SM_ShowNextForLine(IBehaviorInfo aBehavior, int aLineNumber)
		{
			super(aBehavior, aLineNumber);
		}
	}
	
	public static class SM_ShowPreviousForLine extends SM_ShowForLine
	{
		public SM_ShowPreviousForLine(IBehaviorInfo aBehavior, int aLineNumber)
		{
			super(aBehavior, aLineNumber);
		}
	}

}
