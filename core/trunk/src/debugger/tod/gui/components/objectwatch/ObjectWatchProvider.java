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
import tod.tools.scheduling.IJobScheduler;
import zz.utils.ui.ZLabel;

public class ObjectWatchProvider extends AbstractWatchProvider
{
	private final ILogEvent itsRefEvent;
	private final ObjectId itsObject;
	
	private boolean itsInvalid = false;
	
	private IObjectInspector itsInspector;
	private List<Entry> itsEntries;
	
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
				
				itsInspector = getLogBrowser().createClassInspector(theBehavior.getType());
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
	public List<Entry> getEntries()
	{
		if (itsEntries == null)
		{
			List<IFieldInfo> theFields = getInspector().getFields();
			itsEntries = new ArrayList<Entry>(theFields.size());
			for (IFieldInfo theField : theFields)
			{
				itsEntries.add(new ObjectEntry(theField));
			}
		}
		return itsEntries;
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
