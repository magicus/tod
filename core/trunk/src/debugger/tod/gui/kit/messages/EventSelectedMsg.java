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
package tod.gui.kit.messages;

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ObjectId;

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
