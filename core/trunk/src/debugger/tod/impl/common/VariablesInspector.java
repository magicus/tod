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
package tod.impl.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IVariablesInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;

public class VariablesInspector implements IVariablesInspector
{
	private final IBehaviorCallEvent itsBehaviorCall;
	private final IEventBrowser itsChildrenBrowser;
	private ILogEvent itsCurrentEvent;
	private List<LocalVariableInfo> itsVariables;
	
	public VariablesInspector(IBehaviorCallEvent aBehaviorCall)
	{
//		assert aBehaviorCall.isDirectParent();
		itsBehaviorCall = aBehaviorCall.isDirectParent() ? aBehaviorCall : null;
		itsChildrenBrowser = itsBehaviorCall != null && itsBehaviorCall.hasRealChildren() ? 
				itsBehaviorCall.getChildrenBrowser() 
				: null;
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
		itsCurrentEvent = aEvent;
	}

	public ILogEvent getCurrentEvent()
	{
		return itsCurrentEvent;
	}

	public Object[] getEntryValue(LocalVariableInfo aVariable)
	{
		if (itsChildrenBrowser == null) return null;
		
		ILogBrowser theLogBrowser = itsChildrenBrowser.getLogBrowser();
		IEventFilter theFilter = theLogBrowser.createVariableWriteFilter(aVariable);
		IEventBrowser theBrowser = itsChildrenBrowser.createIntersection(theFilter);
		
		theBrowser.setNextEvent(itsCurrentEvent);
		while (theBrowser.hasPrevious())
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) theBrowser.previous();
			if (aVariable.equals(theEvent.getVariable())) return new Object[] {theEvent.getValue()};
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
				return new Object[] {itsBehaviorCall.getArguments()[i]};
			}
			else
			{
				ITypeInfo theType = theArgumentTypes[i];
				theSlot += theType.getSize();
			}
		}
		
		return null;
	}
	
	public ILocalVariableWriteEvent[] getEntrySetter(LocalVariableInfo aVariable)
	{
		if (itsChildrenBrowser == null) return null;
		
		ILogBrowser theLogBrowser = itsChildrenBrowser.getLogBrowser();
		IEventFilter theFilter = theLogBrowser.createVariableWriteFilter(aVariable);
		IEventBrowser theBrowser = itsChildrenBrowser.createIntersection(theFilter);
		
		theBrowser.setNextEvent(itsCurrentEvent);
		while (theBrowser.hasPrevious())
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) theBrowser.previous();
			if (aVariable.equals(theEvent.getVariable())) return new ILocalVariableWriteEvent[] {theEvent};
		}
		
		return null;
	}
}
