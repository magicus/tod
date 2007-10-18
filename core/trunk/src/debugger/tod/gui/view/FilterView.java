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

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.IGUIManager;
import tod.gui.MinerUI;
import tod.gui.eventlist.EventListPanel;
import tod.gui.kit.Bus;
import tod.gui.kit.Options;
import tod.gui.kit.html.HtmlComponent;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;
import tod.gui.seed.FilterSeed;
import tod.gui.view.event.EventView;
import tod.gui.view.event.EventViewFactory;
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
	
	private JSplitPane itsSplitPane;

	private EventListPanel itsListPanel;
	private EventHighlighter itsEventHighlighter;
	
	private IPropertyListener<ILogEvent> itsSelectedEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			if (aNewValue != null) getGUIManager().gotoEvent(aNewValue);
			IEventFilter theFilter = aNewValue != null ?
					getLogBrowser().createEventFilter(aNewValue)
					: null;
					
			itsEventHighlighter.setFilter(theFilter);
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
		itsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		itsSplitPane.setResizeWeight(0.5);
		
		itsListPanel = new EventListPanel (getLogBrowser(), getJobProcessor()); 
		
		itsListPanel.eEventActivated().addListener(new IEventListener<ILogEvent>()
				{
					public void fired(IEvent< ? extends ILogEvent> aEvent, ILogEvent aData)
					{
						Bus.get(FilterView.this).postMessage(new ShowCFlowMsg(aData));
					}
				});
				
		setLayout(new BorderLayout());
		add (itsSplitPane, BorderLayout.CENTER);
		HtmlComponent theTitleComponent = new HtmlComponent(itsSeed.getTitle());
		theTitleComponent.setOpaque(false);
		add(theTitleComponent, BorderLayout.NORTH);
		
		itsSplitPane.setLeftComponent(itsListPanel);
		
		itsEventHighlighter = new EventHighlighter(getGUIManager(), getLogBrowser());
		itsSplitPane.setRightComponent(itsEventHighlighter);
	}
	
	@Override
	public void addNotify()
	{
		connect(itsSeed.pSelectedEvent(), itsListPanel.pSelectedEvent(), true);

		super.addNotify();
		
		itsSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
		
		itsListPanel.setBrowser(itsSeed.getBaseFilter());
		
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

		getGUIManager().setProperty(
				PROPERTY_SPLITTER_POS, 
				""+itsSplitPane.getDividerLocation());
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
