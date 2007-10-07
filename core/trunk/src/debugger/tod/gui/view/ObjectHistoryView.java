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
package tod.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.ObjectIdUtils;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ObjectId;
import tod.gui.FontConfig;
import tod.gui.IGUIManager;
import tod.gui.eventlist.EventListPanel;
import tod.gui.kit.Options;
import tod.gui.kit.html.HtmlComponent;
import tod.gui.kit.html.HtmlDoc;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;
import tod.gui.seed.ObjectHistorySeed;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;

public class ObjectHistoryView extends LogView implements IEventListView
{
	private final ObjectHistorySeed itsSeed;

	private EventListPanel itsListPanel;

	private IEventFilter itsCurrentFilter;
	
	private IPropertyListener<Boolean> itsFlagsListener = new PropertyListener<Boolean>()
	{
		@Override
		public void propertyChanged(IProperty<Boolean> aProperty, Boolean aOldValue, Boolean aNewValue)
		{
			updateFilter();
		}
	};
	
	public ObjectHistoryView(IGUIManager aGUIManager, ILogBrowser aLog, ObjectHistorySeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
		
		createUI ();
		connect(itsSeed.pSelectedEvent(), itsListPanel.pSelectedEvent(), true);
	}

	@Override
	protected void initOptions(Options aOptions)
	{
		super.initOptions(aOptions);
		EventListPanel.createDefaultOptions(aOptions, false, true);
	}
	
	private void createUI()
	{
		itsListPanel = new EventListPanel (getLogBrowser(), getJobProcessor());
		
//		itsListPanel.pSelectedEvent().addHardListener(new PropertyListener<ILogEvent>()
//				{
//					@Override
//					public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
//					{
//					}
//				});
		
		setLayout(new BorderLayout());
		add (itsListPanel, BorderLayout.CENTER);
		
		String theTitle = ObjectIdUtils.getObjectDescription(
				getLogBrowser(), 
				itsSeed.getObject(), 
				false);
		
		HtmlComponent theTitleComponent = new HtmlComponent(
				HtmlDoc.create("<b>"+theTitle+"</b>", FontConfig.BIG, Color.BLACK));
		
		theTitleComponent.setOpaque(false);
		add(theTitleComponent, BorderLayout.NORTH);
		
		updateFilter();
	}
	
	/**
	 * Computes the event filter.
	 */
	private void updateFilter()
	{
		ObjectId theObject = itsSeed.getObject();
		
		// Setup role filter
		ICompoundFilter theRoleFilter = getLogBrowser().createUnionFilter();
		
		if (itsSeed.pShowRole_Arg().get())
		{
			theRoleFilter.add(getLogBrowser().createArgumentFilter(theObject));
		}
		
		if (itsSeed.pShowRole_Result().get())
		{
			theRoleFilter.add(getLogBrowser().createResultFilter(theObject));
		}
		
		if (itsSeed.pShowRole_Target().get())
		{
			theRoleFilter.add(getLogBrowser().createTargetFilter(theObject));
		}
		
		if (itsSeed.pShowRole_Value().get())
		{
			theRoleFilter.add(getLogBrowser().createValueFilter(theObject));
		}

		// Setup kind filter
		ICompoundFilter theKindFilter = getLogBrowser().createUnionFilter();
		
		if (itsSeed.pShowKind_ArrayWrite().get())
		{
			theKindFilter.add(getLogBrowser().createArrayWriteFilter());
		}
		
		if (itsSeed.pShowKind_BehaviorCall().get())
		{
			theKindFilter.add(getLogBrowser().createBehaviorCallFilter());
		}
		
		if (itsSeed.pShowKind_Exception().get())
		{
			theKindFilter.add(getLogBrowser().createExceptionGeneratedFilter());
		}
		
		if (itsSeed.pShowKind_FieldWrite().get())
		{
			theKindFilter.add(getLogBrowser().createFieldWriteFilter());
		}
		
		if (itsSeed.pShowKind_LocalWrite().get())
		{
			theKindFilter.add(getLogBrowser().createVariableWriteFilter());
		}
		
		itsCurrentFilter = getLogBrowser().createIntersectionFilter(
				theRoleFilter,
				theKindFilter);
		
		itsListPanel.setBrowser(getEventBrowser());
	}
	
	@Override
	public void addNotify()
	{
		connect(itsSeed.pSelectedEvent(), itsListPanel.pSelectedEvent(), true);
		
		super.addNotify();
		
		itsSeed.pShowKind_ArrayWrite().addHardListener(itsFlagsListener);
		itsSeed.pShowKind_BehaviorCall().addHardListener(itsFlagsListener);
		itsSeed.pShowKind_Exception().addHardListener(itsFlagsListener);
		itsSeed.pShowKind_FieldWrite().addHardListener(itsFlagsListener);
		itsSeed.pShowKind_LocalWrite().addHardListener(itsFlagsListener);
		
		itsSeed.pShowRole_Arg().addHardListener(itsFlagsListener);
		itsSeed.pShowRole_Result().addHardListener(itsFlagsListener);
		itsSeed.pShowRole_Target().addHardListener(itsFlagsListener);
		itsSeed.pShowRole_Value().addHardListener(itsFlagsListener);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		
		itsSeed.pShowKind_ArrayWrite().removeListener(itsFlagsListener);
		itsSeed.pShowKind_BehaviorCall().removeListener(itsFlagsListener);
		itsSeed.pShowKind_Exception().removeListener(itsFlagsListener);
		itsSeed.pShowKind_FieldWrite().removeListener(itsFlagsListener);
		itsSeed.pShowKind_LocalWrite().removeListener(itsFlagsListener);
		
		itsSeed.pShowRole_Arg().removeListener(itsFlagsListener);
		itsSeed.pShowRole_Result().removeListener(itsFlagsListener);
		itsSeed.pShowRole_Target().removeListener(itsFlagsListener);
		itsSeed.pShowRole_Value().removeListener(itsFlagsListener);
	}
	
	public IEventBrowser getEventBrowser()
	{
		return itsCurrentFilter != null ?
				getLogBrowser().createBrowser(itsCurrentFilter)
				: getLogBrowser().createBrowser();
	}

	public ILogEvent getSelectedEvent()
	{
		return itsListPanel.pSelectedEvent().get();
	}

	public void selectEvent(ILogEvent aEvent, SelectionMethod aMethod)
	{
		itsListPanel.pSelectedEvent().set(aEvent);
	}
	

	private static class FlagsPanel extends JPanel
	{
		
	}
}

