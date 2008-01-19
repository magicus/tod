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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.LocationUtils;
import tod.core.database.browser.ObjectIdUtils;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ObjectId;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.MinerUI;
import tod.gui.eventlist.EventListPanel;
import tod.gui.kit.Bus;
import tod.gui.kit.Options;
import tod.gui.kit.html.HtmlComponent;
import tod.gui.kit.html.HtmlDoc;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;
import tod.gui.seed.ObjectHistorySeed;
import tod.gui.view.highlighter.EventHighlighter;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;
import zz.utils.ui.PropertyEditor;

public class ObjectHistoryView extends LogView implements IEventListView
{
	private static final String PROPERTY_SPLITTER_POS = "objectHistoryView.splitterPos";

	private final ObjectHistorySeed itsSeed;

	private JSplitPane itsSplitPane;

	private EventListPanel itsListPanel;
	private EventHighlighter itsEventHighlighter;

	private IEventFilter itsCurrentFilter;
	
	private IPropertyListener<ILogEvent> itsSelectedEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			LocationUtils.gotoSource(getGUIManager(), aNewValue);
			IEventFilter theFilter = aNewValue != null ?
					getLogBrowser().createEventFilter(aNewValue)
					: null;
					
			itsEventHighlighter.setFilter(theFilter);
		}
	};


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
		itsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		itsSplitPane.setResizeWeight(0.5);
		
		itsListPanel = new EventListPanel (getLogBrowser(), getJobProcessor());
		
		itsListPanel.eEventActivated().addListener(new IEventListener<ILogEvent>()
				{
					public void fired(IEvent< ? extends ILogEvent> aEvent, ILogEvent aData)
					{
						Bus.get(ObjectHistoryView.this).postMessage(new ShowCFlowMsg(aData));
					}
				});
		
//		itsListPanel.pSelectedEvent().addHardListener(new PropertyListener<ILogEvent>()
//				{
//					@Override
//					public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
//					{
//					}
//				});

		itsSplitPane.setLeftComponent(itsListPanel);
		
		setLayout(new BorderLayout());
		add (itsSplitPane, BorderLayout.CENTER);
		
		String theTitle = ObjectIdUtils.getObjectDescription(
				getLogBrowser(), 
				itsSeed.getObject(), 
				false);
		
		HtmlComponent theTitleComponent = new HtmlComponent(
				HtmlDoc.create("<b>"+theTitle+"</b>", FontConfig.BIG, Color.BLACK));
		
		theTitleComponent.setOpaque(false);
		add(theTitleComponent, BorderLayout.NORTH);
		
		JScrollPane theScrollPane = new JScrollPane(
				new FlagsPanel(), 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		add(theScrollPane, BorderLayout.WEST);
		
		updateFilter();
		
		itsEventHighlighter = new EventHighlighter(getGUIManager(), getLogBrowser());
		itsSplitPane.setRightComponent(itsEventHighlighter);
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
		
		itsSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
		
		itsSeed.pShowKind_ArrayWrite().addHardListener(itsFlagsListener);
		itsSeed.pShowKind_BehaviorCall().addHardListener(itsFlagsListener);
		itsSeed.pShowKind_Exception().addHardListener(itsFlagsListener);
		itsSeed.pShowKind_FieldWrite().addHardListener(itsFlagsListener);
		itsSeed.pShowKind_LocalWrite().addHardListener(itsFlagsListener);
		
		itsSeed.pShowRole_Arg().addHardListener(itsFlagsListener);
		itsSeed.pShowRole_Result().addHardListener(itsFlagsListener);
		itsSeed.pShowRole_Target().addHardListener(itsFlagsListener);
		itsSeed.pShowRole_Value().addHardListener(itsFlagsListener);
		
		int theSplitterPos = MinerUI.getIntProperty(
				getGUIManager(), 
				PROPERTY_SPLITTER_POS, 400);
		
		itsSplitPane.setDividerLocation(theSplitterPos);		
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		
		itsSeed.pSelectedEvent().removeListener(itsSelectedEventListener);

		itsSeed.pShowKind_ArrayWrite().removeListener(itsFlagsListener);
		itsSeed.pShowKind_BehaviorCall().removeListener(itsFlagsListener);
		itsSeed.pShowKind_Exception().removeListener(itsFlagsListener);
		itsSeed.pShowKind_FieldWrite().removeListener(itsFlagsListener);
		itsSeed.pShowKind_LocalWrite().removeListener(itsFlagsListener);
		
		itsSeed.pShowRole_Arg().removeListener(itsFlagsListener);
		itsSeed.pShowRole_Result().removeListener(itsFlagsListener);
		itsSeed.pShowRole_Target().removeListener(itsFlagsListener);
		itsSeed.pShowRole_Value().removeListener(itsFlagsListener);
		
		getGUIManager().setProperty(
				PROPERTY_SPLITTER_POS, 
				""+itsSplitPane.getDividerLocation());		
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
	

	private class FlagsPanel extends JPanel
	{
		public FlagsPanel()
		{
			createUI();
		}

		private void createUI()
		{
			JPanel theRolePanel = new JPanel(GUIUtils.createStackLayout());
			theRolePanel.setBorder(BorderFactory.createTitledBorder("Object role"));
			
			theRolePanel.add(PropertyEditor.createCheckBox(
					itsSeed.pShowRole_Arg(), 
					"Argument",
					"<html>" +
					"<b>Argument role.</b> Selects events where the object <br>" +
					"appears as one of the arguments of a behavior call."));
			
			theRolePanel.add(PropertyEditor.createCheckBox(
					itsSeed.pShowRole_Result(), 
					"Result",
					"<html>" +
					"<b>Result role.</b> Selects events where the object <br>" +
					"is the result of a behavior call."));
			
			theRolePanel.add(PropertyEditor.createCheckBox(
					itsSeed.pShowRole_Target(), 
					"Target",
					"<html>" +
					"<b>Target role.</b> Selects events where the object <br>" +
					"is the target of a behavior call, field write or array write."));
			
			theRolePanel.add(PropertyEditor.createCheckBox(
					itsSeed.pShowRole_Value(), 
					"Value",
					"<html>" +
					"<b>Value role.</b> Selects events where the object <br>" +
					"is the value written into a field, local variable or array."));
			
			JPanel theKindPanel = new JPanel(GUIUtils.createStackLayout());
			theKindPanel.setBorder(BorderFactory.createTitledBorder("Event kind"));
			
			theKindPanel.add(PropertyEditor.createCheckBox(
					itsSeed.pShowKind_ArrayWrite(), 
					"Array write",
					"<html>" +
					"<b>Array write kind.</b> Selects array write events"));
			
			theKindPanel.add(PropertyEditor.createCheckBox(
					itsSeed.pShowKind_BehaviorCall(), 
					"Behavior call",
					"<html>" +
					"<b>Behavior call kind.</b> Selects behavior call events"));
			
			theKindPanel.add(PropertyEditor.createCheckBox(
					itsSeed.pShowKind_Exception(), 
					"Exception",
					"<html>" +
					"<b>Exception kind.</b> Selects exception events"));
			
			theKindPanel.add(PropertyEditor.createCheckBox(
					itsSeed.pShowKind_FieldWrite(), 
					"Field write",
					"<html>" +
					"<b>Field write kind.</b> Selects field write events"));
			
			theKindPanel.add(PropertyEditor.createCheckBox(
					itsSeed.pShowKind_LocalWrite(), 
					"Local variable write",
					"<html>" +
					"<b>Local variable write kind.</b> Selects local variable write events"));

			setLayout(GUIUtils.createStackLayout());
			add(theRolePanel);
			add(theKindPanel);
		}
	}
}

