/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.components.objectwatch;

import static tod.gui.FontConfig.STD_HEADER_FONT;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.ICompoundInspector.EntryValue;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import tod.gui.components.objectwatch.AbstractWatchProvider.Entry;
import tod.tools.scheduling.IJobScheduler;
import zz.utils.ui.ZLabel;

public class ObjectWatchProvider extends AbstractWatchProvider
{
	private final ILogEvent itsRefEvent;
	private final ObjectId itsObject;
	
	private boolean itsInvalid = false;
	
	private IObjectInspector itsInspector;
	
	public ObjectWatchProvider(
			IGUIManager aGUIManager, 
			String aTitle,
			ILogEvent aRefEvent,
			ObjectId aObject)
	{
		super(aGUIManager, aTitle);
		itsRefEvent = aRefEvent;
		itsObject = aObject;
	}
	
	protected boolean showPackageNames()
	{
		return false;
	}
	
	private IObjectInspector getInspector()
	{
		if (itsInspector == null && ! itsInvalid)
		{
			if (itsObject != null)
			{
				itsInspector = getLogBrowser().createObjectInspector(itsObject);
			}
			else
			{
				IBehaviorCallEvent theParent = itsRefEvent.getParent();
				if (theParent == null)
				{
					itsInvalid = true;
					return null;
				}
				
				IBehaviorInfo theBehavior = theParent.getExecutedBehavior();
				if (theBehavior == null) theBehavior = theParent.getCalledBehavior();
				
				itsInspector = getLogBrowser().createClassInspector(theBehavior.getDeclaringType());
			}
		}
		
		return itsInspector;
	}
	
	@Override
	public JComponent buildTitleComponent(IJobScheduler aJobScheduler)
	{
		JPanel theContainer = new JPanel(GUIUtils.createStackLayout());
		theContainer.setOpaque(false);
		
		JPanel theObjectContainer = new JPanel(GUIUtils.createSequenceLayout());
		theObjectContainer.setOpaque(false);
		
		theObjectContainer.add(ZLabel.create(
				"Object: ", 
				STD_HEADER_FONT, 
				Color.BLACK));
		
		if (itsObject != null)
		{
			theObjectContainer.add(Hyperlinks.object(
					getGUIManager(),
					Hyperlinks.SWING, 
					aJobScheduler,
					itsObject,
					itsRefEvent,
					showPackageNames()));
		}
		else
		{
			theObjectContainer.add(ZLabel.create(
					"(static)", 
					STD_HEADER_FONT, 
					Color.GRAY));
		}
		
		JPanel theLinksContainer = new JPanel(GUIUtils.createSequenceLayout());
		theLinksContainer.setOpaque(false);
		
		// Setup history link
		theLinksContainer.add(GUIUtils.createLabel("("));
		theLinksContainer.add(Hyperlinks.history(getGUIManager(), Hyperlinks.SWING, itsObject));
		theLinksContainer.add(GUIUtils.createLabel(")"));
		
		theContainer.add(theObjectContainer);
		theContainer.add(theLinksContainer);
		
		return theContainer;
	}
	
	@Override
	public ObjectId getCurrentObject()
	{
		return null;
	}
	
	@Override
	public ObjectId getInspectedObject()
	{
		return itsObject;
	}

	@Override
	public ILogEvent getRefEvent()
	{
		return itsRefEvent;
	}

	@Override
	public int getEntryCount()
	{
		return getInspector().getFieldCount();
	}

	@Override
	public List<Entry> getEntries(int aRangeStart, int aRangeSize)
	{
		List<IFieldInfo> theFields = getInspector().getFields(aRangeStart, aRangeSize);
		List<Entry> theResult = new ArrayList<Entry>(theFields.size());
		for (IFieldInfo theField : theFields) theResult.add(new ObjectEntry(theField));

		return theResult;
	}

	private class ObjectEntry extends Entry
	{
		private IFieldInfo itsField;

		public ObjectEntry(IFieldInfo aField)
		{
			itsField = aField;
		}
		
		@Override
		public String getName()
		{
			return itsField.getName();
		}

		@Override
		public EntryValue[] getValue()
		{
			IObjectInspector theInspector = getInspector();
			theInspector.setReferenceEvent(itsRefEvent);
			return theInspector.getEntryValue(itsField);
		}

		@Override
		public EntryValue[] getNextValue()
		{
			IObjectInspector theInspector = getInspector();
			theInspector.setReferenceEvent(itsRefEvent);
			return theInspector.nextEntryValue(itsField);
		}

		@Override
		public EntryValue[] getPreviousValue()
		{
			IObjectInspector theInspector = getInspector();
			theInspector.setReferenceEvent(itsRefEvent);
			return theInspector.previousEntryValue(itsField);
		}
		
		
	}
}
