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
package tod.gui.eventlist;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.eventlist.MuralScroller.UnitScroll;
import tod.gui.formatter.EventFormatter;
import zz.utils.Utils;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.properties.IProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyListener;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.FormattedRenderer;

/**
 * A component that displays a list of events obtained from a 
 * {@link IEventBrowser}.
 * @author gpothier
 */
public class EventList extends JPanel
{
	private final ILogBrowser itsLogBrowser;
	private final IEventFilter itsFilter;
	
	private EventListCore itsCore;
	
	private MyListModel itsModel = new MyListModel();
	private EventFormatter itsFormatter;
	
	private boolean itsUpdating = false;
	
	/**
	 * This property holds the currently selected event
	 */
	private IRWProperty<ILogEvent> pSelectedEvent = new SimpleRWProperty<ILogEvent>();
	private JList itsList; 
	
	public EventList(ILogBrowser aBrowser, IEventFilter aFilter)
	{
		itsLogBrowser = aBrowser;
		itsFilter = aFilter;
		itsFormatter = new EventFormatter(itsLogBrowser);
		itsCore = new EventListCore(itsLogBrowser.createBrowser(itsFilter), 10);
		
		createUI();
		update();
	}

	private long getFirstTimestamp()
	{
		return itsCore.getFirstTimestamp();
	}
	
	private long getLastTimestamp()
	{
		return itsCore.getLastTimestamp();
	}
	
	private List<ILogEvent> getDisplayedEvents()
	{
		return itsCore.getDisplayedEvents();
	}
	
	private void createUI()
	{
		setLayout(new BorderLayout());
		
		final MuralScroller theScroller = new MuralScroller(
				itsLogBrowser.createBrowser(itsFilter), //We can't share the event browser 
				getFirstTimestamp(), 
				getLastTimestamp());

		theScroller.pTrackScroll().addHardListener(new PropertyListener<Long>()
				{
					@Override
					public void propertyChanged(IProperty<Long> aProperty, Long aOldValue, Long aNewValue)
					{
						if (itsUpdating) return;
						itsCore.setTimestamp(aNewValue);
						update();
					}
				});
		
		theScroller.eUnitScroll().addListener(new IEventListener<UnitScroll>()
				{
					public void fired(IEvent< ? extends UnitScroll> aEvent, UnitScroll aData)
					{
						switch(aData)
						{
						case UP:
							itsCore.backward(1);
							break;
							
						case DOWN:
							itsCore.forward(1);
							break;
							
						case PAGE_UP:
							itsCore.backward(5);
							break;
							
						case PAGE_DOWN:
							itsCore.forward(5);
							break;
							
						default:
							throw new RuntimeException("Not handled: "+aData);
						}
						
						// Update tracker position
						if (getDisplayedEvents().size() > 0)
						{
							itsUpdating = true;
							long theTimestamp = getDisplayedEvents().get(0).getTimestamp();
							theScroller.pTrackScroll().set(theTimestamp);
							itsUpdating = false;
						}
						
						update();
					}
				});
		
		add(theScroller, BorderLayout.EAST);
		itsList = new JList(itsModel);
		itsList.setCellRenderer(new FormattedRenderer(itsFormatter));
		itsList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent aE)
			{
				ILogEvent theEvent = (ILogEvent) itsList.getSelectedValue();
				if (theEvent != null) pSelectedEvent.set(theEvent);
			}
		});
		
		JScrollPane theScrollPane = new JScrollPane(
				itsList, 
				JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		add(theScrollPane, BorderLayout.CENTER);
	}
	
	public IRWProperty<ILogEvent> pSelectedEvent()
	{
		return pSelectedEvent;
	}

	protected void update()
	{
		itsModel.fireChanged();
		
		// Update selected event
		ILogEvent theEvent = pSelectedEvent().get();
		if (theEvent == null) itsList.clearSelection();
		else
		{
			int theIndex = Utils.indexOfIdent(theEvent, getDisplayedEvents());
			if (theIndex >= 0) itsList.setSelectedIndex(theIndex);
			else itsList.clearSelection();
		}
	}
	
	
	private class MyListModel extends AbstractListModel
	{
		public Object getElementAt(int aIndex)
		{
			return getDisplayedEvents().get(aIndex);
		}

		public int getSize()
		{
			return getDisplayedEvents().size();
		}
		
		public void fireChanged()
		{
			fireContentsChanged(this, 0, getSize());
		}
	}
	
	
}
