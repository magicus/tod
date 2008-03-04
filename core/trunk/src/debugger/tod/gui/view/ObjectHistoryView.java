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
import tod.gui.kit.SavedSplitPane;
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
		JSplitPane theSplitPane = new SavedSplitPane(JSplitPane.HORIZONTAL_SPLIT, getGUIManager(), PROPERTY_SPLITTER_POS);
		theSplitPane.setResizeWeight(0.5);
		
		itsListPanel = new EventListPanel (getGUIManager(), getBus(), getLogBrowser(), getJobProcessor());
		
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

		theSplitPane.setLeftComponent(itsListPanel);
		
		setLayout(new BorderLayout());
		add (theSplitPane, BorderLayout.CENTER);
		
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
		theSplitPane.setRightComponent(itsEventHighlighter);
	}
	
	/**
	 * Computes the event filter.
	 */
	private void updateFilter()
	{
		ObjectId theObject = itsSeed.getObject();
		
		// Setup role filter
		IEventFilter theRoleFilter;
		
		Boolean r_arg = itsSeed.pShowRole_Arg().get();
		Boolean r_result = itsSeed.pShowRole_Result().get();
		Boolean r_target = itsSeed.pShowRole_Target().get();
		Boolean r_value = itsSeed.pShowRole_Value().get();
		
		if (r_arg && r_result && r_target && r_value)
		{
			theRoleFilter = getLogBrowser().createObjectFilter(theObject);
		}
		else
		{
			ICompoundFilter theCompound = getLogBrowser().createUnionFilter();
			if (r_arg) theCompound.add(getLogBrowser().createArgumentFilter(theObject));
			if (r_result) theCompound.add(getLogBrowser().createResultFilter(theObject));
			if (r_target) theCompound.add(getLogBrowser().createTargetFilter(theObject));
			if (r_value) theCompound.add(getLogBrowser().createValueFilter(theObject));
			theRoleFilter = theCompound;
		}

		// Setup kind filter
		Boolean k_arrayWrite = itsSeed.pShowKind_ArrayWrite().get();
		Boolean k_behaviorCall = itsSeed.pShowKind_BehaviorCall().get();
		Boolean k_exception = itsSeed.pShowKind_Exception().get();
		Boolean k_fieldWrite = itsSeed.pShowKind_FieldWrite().get();
		Boolean k_localWrite = itsSeed.pShowKind_LocalWrite().get();

		if (k_arrayWrite && k_behaviorCall && k_exception && k_fieldWrite && k_localWrite)
		{
			// If all kinds are selected, there is no need to filter
			itsCurrentFilter = theRoleFilter;
		}
		else
		{
			ICompoundFilter theKindFilter = getLogBrowser().createUnionFilter();
			if (k_arrayWrite) theKindFilter.add(getLogBrowser().createArrayWriteFilter());
			if (k_behaviorCall) theKindFilter.add(getLogBrowser().createBehaviorCallFilter());
			if (k_exception) theKindFilter.add(getLogBrowser().createExceptionGeneratedFilter());
			if (k_fieldWrite) theKindFilter.add(getLogBrowser().createFieldWriteFilter());
			if (k_localWrite) theKindFilter.add(getLogBrowser().createVariableWriteFilter());
			
			itsCurrentFilter = getLogBrowser().createIntersectionFilter(
					theRoleFilter,
					theKindFilter);
		}
			
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

