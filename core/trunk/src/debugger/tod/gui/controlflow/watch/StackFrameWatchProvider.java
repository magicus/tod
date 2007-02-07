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
package tod.gui.controlflow.watch;

import static tod.gui.FontConfig.STD_HEADER_FONT;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.Util;
import tod.core.ILocationRegisterer.LocalVariableInfo;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IVariablesInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IWriteEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import zz.utils.ui.ZLabel;

/**
 * Watch provider for stack frame reconstitution
 * @author gpothier
 */
public class StackFrameWatchProvider implements IWatchProvider<LocalVariableInfo>
{
	private final WatchPanel itsWatchPanel;
	private final ILogBrowser itsLogBrowser;
	private final ILogEvent itsRefEvent;
	
	private IBehaviorCallEvent itsParentEvent;
	private boolean itsInvalid = false;
	private boolean itsIndirectParent = false;

	private IVariablesInspector itsInspector;
	
	public StackFrameWatchProvider(WatchPanel aWatchPanel, ILogBrowser aLogBrowser, ILogEvent aRefEvent)
	{
		itsWatchPanel = aWatchPanel;
		itsLogBrowser = aLogBrowser;
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
				itsInspector = itsLogBrowser.createVariablesInspector(theParentEvent);
				itsInspector.setCurrentEvent(itsRefEvent);
			}
		}
		
		return itsInspector;
	}

	public JComponent buildTitle(JobProcessor aJobProcessor)
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
			
			JPanel theContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			theContainer.setOpaque(false);

			if (theBehavior != null)
			{
				theContainer.add(ZLabel.create("Behavior: ", STD_HEADER_FONT, Color.BLACK));
				theContainer.add(Hyperlinks.behavior(itsWatchPanel.getLogViewSeedFactory(), theBehavior, STD_HEADER_FONT));
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

	public List<LocalVariableInfo> getEntries()
	{
		if (itsInvalid) return null;
		return getInspector().getVariables();
	}

	public String getEntryName(LocalVariableInfo aEntry)
	{
		if (itsInvalid) return null;
		return aEntry.getVariableName();
	}

	public IWriteEvent[] getEntrySetter(LocalVariableInfo aEntry)
	{
		if (itsInvalid) return null;
		return getInspector().getEntrySetter(aEntry);
	}

	public Object[] getEntryValue(LocalVariableInfo aEntry)
	{
		if (itsInvalid) return null;
		return getInspector().getEntryValue(aEntry);
	}
	
	
}
