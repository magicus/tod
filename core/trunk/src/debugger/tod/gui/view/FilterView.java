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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.IGUIManager;
import tod.gui.MinerUI;
import tod.gui.eventlist.EventList;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;
import tod.gui.seed.FilterSeed;
import tod.gui.view.event.EventView;
import tod.gui.view.event.EventViewFactory;
import zz.utils.properties.IProperty;
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
	private JScrollPane itsScrollPane;

	private EventList itsList;
	private JPanel itsEventViewHolder;
	
	public FilterView(IGUIManager aGUIManager, ILogBrowser aLog, FilterSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
		
		createUI ();
		connect(itsSeed.pSelectedEvent(), itsList.pSelectedEvent(), true);
	}

	private void createUI()
	{
		itsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		itsSplitPane.setResizeWeight(0.5);
		
		itsList = new EventList (getLogBrowser(), itsSeed.getFilter());
		
		itsList.pSelectedEvent().addHardListener(new PropertyListener<ILogEvent>()
				{
					@Override
					public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
					{
						setSelectedEvent(aNewValue);
					}
				});
		
		setLayout(new BorderLayout());
		add (itsSplitPane, BorderLayout.CENTER);
		
		itsSplitPane.setLeftComponent(itsList);
		
		itsScrollPane = new JScrollPane();
		itsScrollPane.getViewport().setBackground(Color.WHITE);
		itsSplitPane.setRightComponent(itsScrollPane);
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		
		int theSplitterPos = MinerUI.getIntProperty(
				getGUIManager(), 
				PROPERTY_SPLITTER_POS, 400);
		
		itsSplitPane.setDividerLocation(theSplitterPos);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		
		getGUIManager().setProperty(
				PROPERTY_SPLITTER_POS, 
				""+itsSplitPane.getDividerLocation());
	}
	
	private void setSelectedEvent (ILogEvent aEvent)
	{
		if (aEvent != null)
		{
			EventView theView = EventViewFactory.createView(
					getGUIManager(), 
					getLogBrowser(),
					aEvent);
			
			itsScrollPane.setViewportView(theView);
		}
		else
		{
			itsScrollPane.setViewportView(null);
		}
		
		revalidate();
		repaint();
	}

	public IEventBrowser getEventBrowser()
	{
		IEventFilter theFilter = itsSeed.getFilter();
		return theFilter != null ?
				getLogBrowser().createBrowser(theFilter)
				: getLogBrowser().createBrowser();
	}

	public ILogEvent getSelectedEvent()
	{
		return itsList.pSelectedEvent().get();
	}

	public void selectEvent(ILogEvent aEvent, SelectionMethod aMethod)
	{
		itsList.pSelectedEvent().set(aEvent);
	}
	
	
}
