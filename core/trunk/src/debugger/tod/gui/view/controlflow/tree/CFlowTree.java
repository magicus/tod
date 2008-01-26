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
package tod.gui.view.controlflow.tree;

import javax.swing.JSplitPane;

import tod.core.config.TODConfig;
import tod.core.database.browser.GroupingEventBrowser;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.JoinpointShadowGroupingDefinition;
import tod.core.database.browser.ShadowId;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.gui.JobProcessor;
import tod.gui.eventlist.EventListPanel;
import tod.gui.kit.BusPanel;
import tod.gui.kit.SavedSplitPane;
import tod.gui.view.controlflow.CFlowView;
import zz.utils.Utils;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyUtils;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.StackLayout;

public class CFlowTree extends BusPanel
{
	private static final String PROPERTY_SPLITTER_POS = "cflowTree.splitterPos";
	
	private final CFlowView itsView;
	private CallStackPanel itsCallStackPanel;
	private EventListPanel itsEventListPanel;
	private IParentEvent itsCurrentParent;
	
	private final IRWProperty<ILogEvent> pSelectedEvent = 
		new SimpleRWProperty<ILogEvent>()
		{
			@Override
			protected void changed(ILogEvent aOldValue, ILogEvent aNewValue)
			{
				IParentEvent theParent = aNewValue.getParent();
				if (theParent == null) theParent = itsView.getSeed().pRootEvent().get();
				
				if (Utils.different(itsCurrentParent, theParent))
				{
					IEventBrowser theBrowser = theParent.getChildrenBrowser();
					
					if (itsView.getConfig().get(TODConfig.WITH_ASPECTS))
					{
						theBrowser = new GroupingEventBrowser<ShadowId>(
								theBrowser,
								JoinpointShadowGroupingDefinition.getInstance(),
								true);
					}
					
					itsEventListPanel.setBrowser(theBrowser);
					itsCurrentParent = theParent;
				}
				
				// Only update call stack, as this property is connected to event list
				itsCallStackPanel.setLeafEvent(aNewValue);
			}
		};
	
	public CFlowTree(CFlowView aView)
	{
		super(aView.getBus());
		itsView = aView;
		createUI();
	}
	
	public JobProcessor getJobProcessor()
	{
		return itsView.getJobProcessor();
	}

	/**
	 * This property corresponds to the currently selected event.
	 * @return
	 */
	public IRWProperty<ILogEvent> pSelectedEvent()
	{
		return pSelectedEvent;
	}
	
	private void createUI()
	{
		JSplitPane theSplitPane = new SavedSplitPane(itsView.getGUIManager(), PROPERTY_SPLITTER_POS);
		setLayout(new StackLayout());
		add(theSplitPane);
		
		itsEventListPanel = new EventListPanel(
				itsView.getGUIManager(), 
				getBus(), 
				itsView.getLogBrowser(), 
				getJobProcessor());
		
		itsEventListPanel.eEventActivated().addListener(new IEventListener<ILogEvent>()
				{
					public void fired(IEvent< ? extends ILogEvent> aEvent, ILogEvent aData)
					{
						if (aData instanceof IBehaviorCallEvent) 
						{
							IBehaviorCallEvent theCall = (IBehaviorCallEvent) aData;
							if (theCall.hasRealChildren()) itsView.forwardStepInto(aData);
						}
					}
				});
		
		theSplitPane.setRightComponent(itsEventListPanel);
		
		PropertyUtils.connect(pSelectedEvent, itsEventListPanel.pSelectedEvent(), true);
		
		itsCallStackPanel = new CallStackPanel(itsView.getLogBrowser(), getJobProcessor());
		theSplitPane.setLeftComponent(itsCallStackPanel);
	}
	
	/**
	 * Scrolls so that the given event is visible.
	 * @return The bounds of the graphic object that represent
	 * the event.
	 */
	public void makeVisible(ILogEvent aEvent)
	{
		itsEventListPanel.makeVisible(aEvent);
	}
	
}
