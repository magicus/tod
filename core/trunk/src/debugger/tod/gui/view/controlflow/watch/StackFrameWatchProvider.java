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
package tod.gui.view.controlflow.watch;

import static tod.gui.FontConfig.STD_HEADER_FONT;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.Util;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IVariablesInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IWriteEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ObjectId;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import tod.gui.JobProcessor;
import zz.utils.ui.ZLabel;

/**
 * Watch provider for stack frame reconstitution
 * @author gpothier
 */
public class StackFrameWatchProvider extends AbstractWatchProvider
{
	private final ILogEvent itsRefEvent;
	
	private IBehaviorCallEvent itsParentEvent;
	private boolean itsInvalid = false;
	private boolean itsIndirectParent = false;

	private IVariablesInspector itsInspector;
	private List<Entry> itsEntries;
	
	public StackFrameWatchProvider(
			IGUIManager aGUIManager, 
			String aTitle,
			ILogEvent aRefEvent)
	{
		super(aGUIManager, aTitle);
		itsRefEvent = aRefEvent;
	}

	private IBehaviorCallEvent getParentEvent()
	{
		if (itsParentEvent == null && ! itsInvalid)
		{
			itsParentEvent = itsRefEvent.getParent();
			if (itsParentEvent == null)
			{
				itsInvalid = true;
				return null;
			}
			else if (! itsParentEvent.isDirectParent())
			{
				itsInvalid = true;
				itsIndirectParent = true;
				return null;
			}
		}
		return itsParentEvent;
	}
	
	private IVariablesInspector getInspector()
	{
		if (itsInspector == null && ! itsInvalid);
		{
			IBehaviorCallEvent theParentEvent = getParentEvent();
			if (theParentEvent != null)
			{
				itsInspector = getLogBrowser().createVariablesInspector(theParentEvent);
				itsInspector.setReferenceEvent(itsRefEvent);
			}
		}
		
		return itsInspector;
	}

	public JComponent buildTitleComponent(JobProcessor aJobProcessor)
	{
		IBehaviorCallEvent theParentEvent = getParentEvent();

		if (itsIndirectParent)
		{
			return GUIUtils.createMessage(
					"Variable information not available", 
					Color.DARK_GRAY,
					"Cause: missing control flow information, check working set.",
					Color.DARK_GRAY);
		}
		else if (itsInvalid)
		{
			return GUIUtils.createMessage(
					"Variable information not available", 
					Color.DARK_GRAY,
					"Cause: the currently selected event is a control flow root.",
					Color.DARK_GRAY);
		}
		else
		{
			IBehaviorInfo theBehavior = theParentEvent.getExecutedBehavior();
			
			JPanel theContainer = new JPanel(GUIUtils.createSequenceLayout());
			theContainer.setOpaque(false);

			if (theBehavior != null)
			{
				theContainer.add(ZLabel.create("Behavior: ", STD_HEADER_FONT, Color.BLACK));
				theContainer.add(Hyperlinks.behavior(Hyperlinks.SWING, theBehavior));
				theContainer.add(ZLabel.create(" ("+Util.getPrettyName(theBehavior.getType().getName())+")", STD_HEADER_FONT, Color.BLACK));
			}
			
			return theContainer;
		}			
	}

	public ObjectId getCurrentObject()
	{
		if (itsInvalid) return null;
		IBehaviorCallEvent theParentEvent = getParentEvent();
		return theParentEvent != null ?
				(ObjectId) theParentEvent.getTarget()
				: null;
	}

	public ILogEvent getRefEvent()
	{
		return itsRefEvent;
	}
	
	public List<Entry> getEntries()
	{
		if (itsInvalid) return null;
		if (itsEntries == null)
		{
			List<LocalVariableInfo> theVariables = getInspector().getVariables();
			itsEntries = new ArrayList<Entry>();
			for (LocalVariableInfo theLocalVariable : theVariables)
			{
				itsEntries.add(new LocalVariableEntry(theLocalVariable));
			}
		}
		return itsEntries;
	}

	private class LocalVariableEntry extends Entry
	{
		private LocalVariableInfo itsLocalVariable;

		public LocalVariableEntry(LocalVariableInfo aLocalVariable)
		{
			itsLocalVariable = aLocalVariable;
		}
		
		public String getName()
		{
			if (itsInvalid) return null;
			return itsLocalVariable.getVariableName();
		}

		public IWriteEvent[] getSetter()
		{
			if (itsInvalid) return null;
			return getInspector().getEntrySetter(itsLocalVariable);
		}

		public Object[] getValue()
		{
			if (itsInvalid) return null;
			return getInspector().getEntryValue(itsLocalVariable);
		}
	}
}
