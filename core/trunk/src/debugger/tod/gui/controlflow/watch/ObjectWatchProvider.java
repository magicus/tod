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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IWriteEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import zz.utils.ui.ZLabel;

public class ObjectWatchProvider extends AbstractWatchProvider
{
	private final ILogBrowser itsLogBrowser;
	private final ILogEvent itsRefEvent;
	private final ObjectId itsObject;
	
	private IBehaviorCallEvent itsParentEvent;
	private boolean itsInvalid = false;
	
	private IObjectInspector itsInspector;
	private List<Entry> itsEntries;
	
	public ObjectWatchProvider(
			String aTitle,
			ILogBrowser aLogBrowser, 
			ILogEvent aRefEvent,
			ObjectId aObject)
	{
		super(aTitle);
		itsLogBrowser = aLogBrowser;
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
				itsInspector = itsLogBrowser.createObjectInspector(itsObject);
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
				
				itsInspector = itsLogBrowser.createClassInspector(theBehavior.getType());
			}
			
			itsInspector.setCurrentEvent(itsRefEvent);
		}
		
		return itsInspector;
	}
	
	public JComponent buildTitleComponent(JobProcessor aJobProcessor)
	{
		JPanel theContainer = new JPanel(GUIUtils.createSequenceLayout());
		theContainer.setOpaque(false);
		
		theContainer.add(ZLabel.create(
				"Object: ", 
				STD_HEADER_FONT, 
				Color.BLACK));
		
		if (itsObject != null)
		{
			theContainer.add(Hyperlinks.object(
					Hyperlinks.SWING, 
					itsLogBrowser, 
					aJobProcessor,
					itsObject,
					itsRefEvent,
					showPackageNames()));
		}
		else
		{
			theContainer.add(ZLabel.create(
					"(static)", 
					STD_HEADER_FONT, 
					Color.GRAY));
		}
		
		// Setup history link
		theContainer.add(GUIUtils.createLabel(" ("));
		theContainer.add(Hyperlinks.history(Hyperlinks.SWING, itsObject));
		theContainer.add(GUIUtils.createLabel(")"));
		
		return theContainer;
	}
	
	public ObjectId getCurrentObject()
	{
		return null;
	}
	
	public ILogEvent getRefEvent()
	{
		return itsRefEvent;
	}

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
		
		public String getName()
		{
			return itsField.getName();
		}

		public IWriteEvent[] getSetter()
		{
			return getInspector().getEntrySetter(itsField);
		}

		public Object[] getValue()
		{
			return getInspector().getEntryValue(itsField);
		}
	}
}
