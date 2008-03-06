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

import javax.swing.JSplitPane;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.LocationUtils;
import tod.core.database.event.ILogEvent;
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import tod.gui.eventlist.EventListPanel;
import tod.gui.kit.Bus;
import tod.gui.kit.Options;
import tod.gui.kit.SavedSplitPane;
import tod.gui.kit.html.HtmlComponent;
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
public class FilterView extends LogView implements IEventListView
{
	private static final String PROPERTY_SPLITTER_POS = "filterView.splitterPos";
	private FilterSeed itsSeed;
	
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
					Color.BLUE));
		}
	};
	
	public FilterView(IGUIManager aGUIManager, ILogBrowser aLog, FilterSeed aSeed)
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
						Bus.get(FilterView.this).postMessage(new ShowCFlowMsg(aData));
					}
				});
				
		setLayout(new BorderLayout());
		add (theSplitPane, BorderLayout.CENTER);
		HtmlComponent theTitleComponent = new HtmlComponent(itsSeed.getTitle());
		theTitleComponent.setOpaque(false);
		add(theTitleComponent, BorderLayout.NORTH);
		
		theSplitPane.setLeftComponent(itsListPanel);
		
		itsEventHighlighter = new EventHighlighter(getGUIManager(), getLogBrowser());
		theSplitPane.setRightComponent(itsEventHighlighter);
	}
	
	@Override
	public void addNotify()
	{
		connect(itsSeed.pSelectedEvent(), itsListPanel.pSelectedEvent(), true);

		super.addNotify();
		
		itsSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
		
		itsListPanel.setBrowser(itsSeed.getBaseFilter());
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		
		itsSeed.pSelectedEvent().removeListener(itsSelectedEventListener);
	}
	
	public IEventBrowser getEventBrowser()
	{
		IEventFilter theFilter = itsSeed.getBaseFilter();
		return theFilter != null ?
				getLogBrowser().createBrowser(theFilter)
				: getLogBrowser().createBrowser();
	}

	public ILogEvent getSelectedEvent()
	{
		return itsSeed.pSelectedEvent().get();
	}

	public void selectEvent(ILogEvent aEvent, SelectionMethod aMethod)
	{
		itsSeed.pSelectedEvent().set(aEvent);
	}
	
	
}
