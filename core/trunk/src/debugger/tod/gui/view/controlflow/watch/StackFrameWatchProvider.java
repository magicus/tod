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
