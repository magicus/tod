/*
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
	private ILogEvent itsReferenceEvent;
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
	
	public void setReferenceEvent(ILogEvent aEvent)
	{
		if (getBehaviorCall() == null) return;
		itsReferenceEvent = aEvent;
	}

	public ILogEvent getReferenceEvent()
	{
		return itsReferenceEvent;
	}

	public Object[] getEntryValue(LocalVariableInfo aVariable)
	{
		if (itsChildrenBrowser == null) return null;
		
		ILogBrowser theLogBrowser = itsChildrenBrowser.getLogBrowser();
		IEventFilter theFilter = theLogBrowser.createVariableWriteFilter(aVariable);
		IEventBrowser theBrowser = itsChildrenBrowser.createIntersection(theFilter);
		
		theBrowser.setNextEvent(itsReferenceEvent);
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
		
		theBrowser.setNextEvent(itsReferenceEvent);
		while (theBrowser.hasPrevious())
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) theBrowser.previous();
			if (aVariable.equals(theEvent.getVariable())) return new ILocalVariableWriteEvent[] {theEvent};
		}
		
		return null;
	}
}
