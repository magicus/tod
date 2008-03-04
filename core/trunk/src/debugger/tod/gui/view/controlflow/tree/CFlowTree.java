/*
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
