/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.impl.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.database.browser.IVariablesInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;

public class VariablesInspector implements IVariablesInspector
{
	private IBehaviorCallEvent itsBehaviorCall;
	private int itsCurrentIndex;
	private ILogEvent itsCurrentEvent;
	private List<LocalVariableInfo> itsVariables;
	
	public VariablesInspector(IBehaviorCallEvent aBehaviorCall)
	{
//		assert aBehaviorCall.isDirectParent();
		itsBehaviorCall = aBehaviorCall.isDirectParent() ? aBehaviorCall : null;
	}

	public IBehaviorCallEvent getBehaviorCall()
	{
		return itsBehaviorCall;
	}
	
	public IBehaviorInfo getBehavior()
	{
		return getBehaviorCall() != null ? getBehaviorCall().getExecutedBehavior() : null;
	}
	
	public List<LocalVariableInfo> getVariables()
	{
		if (itsVariables == null)
		{
			itsVariables = getBehaviorCall() != null ?
					Arrays.asList(getBehavior().getLocalVariables())
					: Collections.EMPTY_LIST;
		}
		return itsVariables;
	}
	
	public List<LocalVariableInfo> getVariables(int aBytecodeIndex)
	{
		List<LocalVariableInfo> theResult = new ArrayList<LocalVariableInfo>();
		for (LocalVariableInfo theVariable : getVariables())
		{
			if (theVariable.available(aBytecodeIndex)) theResult.add(theVariable);
		}
		
		return theResult;
	}
	
	public void setCurrentEvent(ILogEvent aEvent)
	{
		if (getBehaviorCall() == null) return;
		
		int theIndex = getBehaviorCall().getChildren().indexOf(aEvent);
		if (theIndex == -1) throw new RuntimeException("Event not found in method execution");
		
		itsCurrentEvent = aEvent;
		itsCurrentIndex = theIndex;
	}

	public ILogEvent getCurrentEvent()
	{
		return itsCurrentEvent;
	}

	public Object getVariableValue(LocalVariableInfo aVariable)
	{
		if (getBehaviorCall() == null) return null;
		
		// TODO: Use proper cursor APIs!!!!!!
		for (int i=itsCurrentIndex; i>=0; i--)
		{
			ILogEvent theEvent = getBehaviorCall().getChildren().get(i);
			if (theEvent instanceof ILocalVariableWriteEvent)
			{
				ILocalVariableWriteEvent theLocalVariableWriteEvent = (ILocalVariableWriteEvent) theEvent;
				if (theLocalVariableWriteEvent.getVariable() == aVariable)
				{
					return theLocalVariableWriteEvent.getValue();
				}
			}
		}
		
		// If we did not find a variable write corresponding to the variable,
		// we consider the behavior call's initial argument values
		IBehaviorInfo theBehavior = itsBehaviorCall.getExecutedBehavior();
		ITypeInfo[] theArgumentTypes = theBehavior.getArgumentTypes();
		int theSlot = theBehavior.isStatic() ? 0 : 1;
		for (int i = 0; i < theArgumentTypes.length; i++)
		{
			if (aVariable.getIndex() == theSlot)
			{
				return itsBehaviorCall.getArguments()[i];
			}
			else
			{
				ITypeInfo theType = theArgumentTypes[i];
				theSlot += theType.getSize();
			}
		}
		
		return null;
	}

	public ILocalVariableWriteEvent getVariableSetter(LocalVariableInfo aVariable)
	{
		if (getBehaviorCall() == null) return null;
		
		// TODO: Use proper cursor APIs!!!!!!
		for (int i=itsCurrentIndex; i>=0; i--)
		{
			ILogEvent theEvent = getBehaviorCall().getChildren().get(i);
			if (theEvent instanceof ILocalVariableWriteEvent)
			{
				ILocalVariableWriteEvent theLocalVariableWriteEvent = (ILocalVariableWriteEvent) theEvent;
				if (theLocalVariableWriteEvent.getVariable() == aVariable)
				{
					return theLocalVariableWriteEvent;
				}
			}
		}
		
		return null;
	}
	
	
	
}
