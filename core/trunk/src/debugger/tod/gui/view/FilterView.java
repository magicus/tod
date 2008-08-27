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
import java.awt.Dimension;

import javax.swing.JSplitPane;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.LocationUtils;
import tod.core.database.event.ILogEvent;
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import tod.gui.eventlist.EventListPanel;
import tod.gui.kit.Bus;
import tod.gui.kit.Options;
import tod.gui.kit.SavedSplitPane;
import tod.gui.kit.html.HtmlComponent;
import tod.gui.kit.html.HtmlDoc;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;
import tod.gui.seed.FilterSeed;
import tod.gui.view.highlighter.EventHighlighter;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;

/**
 * A view component that displays a list of events 
 * based on a {@link tod.core.database.browser.IEventFilter}
 * @author gpothier
 */
public class FilterView extends LogView<FilterSeed> 
{
	private static final String PROPERTY_SPLITTER_POS = "filterView.splitterPos";
	
	private EventListPanel itsListPanel;
	private EventHighlighter itsEventHighlighter;
	
	private IPropertyListener<ILogEvent> itsSelectedEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			LocationUtils.gotoSource(getGUIManager(), aNewValue);
			IEventFilter theFilter = aNewValue != null ?
					getLogBrowser().createEventFilter(aNewValue)
					: null;
					
			itsEventHighlighter.pHighlightBrowsers.clear();
			itsEventHighlighter.pHighlightBrowsers.add(new BrowserData(
					getLogBrowser().createBrowser(theFilter),
					Color.BLUE,
					BrowserData.DEFAULT_MARK_SIZE));
		}
	};

	private HtmlComponent itsTitleComponent;
	
	public FilterView(IGUIManager aGUIManager)
	{
		super(aGUIManager);
		
		createUI ();
	}
	
	@Override
	protected void connectSeed(FilterSeed aSeed)
	{
		connect(aSeed.pSelectedEvent(), itsListPanel.pSelectedEvent());
		connect(aSeed.pSelectedEvent(), itsListPanel.pSelectedEvent());
		aSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
		
		itsListPanel.setBrowser(aSeed.getBaseFilter());
		itsTitleComponent.setDoc(aSeed.getTitle());
		itsTitleComponent.getPreferredSize(); // For some reason we have to call this in order for the size to be updated...
	}

	@Override
	protected void disconnectSeed(FilterSeed aSeed)
	{
		disconnect(aSeed.pSelectedEvent(), itsListPanel.pSelectedEvent());
		disconnect(aSeed.pSelectedEvent(), itsListPanel.pSelectedEvent());
		aSeed.pSelectedEvent().removeListener(itsSelectedEventListener);
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
		
		itsListPanel = new EventListPanel (getGUIManager(), getBus(), getLogBrowser(), getJobScheduler()); 
		
		itsListPanel.eEventActivated().addListener(new IEventListener<ILogEvent>()
				{
					public void fired(IEvent< ? extends ILogEvent> aEvent, ILogEvent aData)
					{
						Bus.get(FilterView.this).postMessage(new ShowCFlowMsg(aData));
					}
				});
				
		setLayout(new BorderLayout());
		add (theSplitPane, BorderLayout.CENTER);
		itsTitleComponent = new HtmlComponent();
		itsTitleComponent.setOpaque(false);
		add(itsTitleComponent, BorderLayout.NORTH);
		
		theSplitPane.setLeftComponent(itsListPanel);
		
		itsEventHighlighter = new EventHighlighter(getGUIManager(), getLogBrowser());
		theSplitPane.setRightComponent(itsEventHighlighter);
	}
	
	public IEventBrowser getEventBrowser()
	{
		IEventFilter theFilter = getSeed().getBaseFilter();
		
		return theFilter != null ?
				getLogBrowser().createBrowser(theFilter)
				: getLogBrowser().createBrowser();
	}

	public ILogEvent getSelectedEvent()
	{
		return getSeed().pSelectedEvent().get();
	}

	public void selectEvent(ILogEvent aEvent, SelectionMethod aMethod)
	{
		getSeed().pSelectedEvent().set(aEvent);
	}
	
	
}
