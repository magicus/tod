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
import java.util.LinkedList;

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
	private final IEventBrowser itsBrowser;
	
	private long itsFirstTimestamp;
	private long itsLastTimestamp;
	
	private MyListModel itsModel = new MyListModel();
	private EventFormatter itsFormatter;
	
	private boolean itsUpdating = false;

	
	/**
	 * Delta between the first displayed event and the browser's
	 * current event.
	 */
	private int itsCurrentDelta = 0;
	
	private LinkedList<ILogEvent> itsDisplayedEvents = new LinkedList<ILogEvent>();
	
	/**
	 * Number of visible events according to the window's size. 
	 */
	private int itsVisibleEvents = 10;
	
	/**
	 * This property holds the currently selected event
	 */
	private IRWProperty<ILogEvent> pSelectedEvent = new SimpleRWProperty<ILogEvent>();
	private JList itsList; 
	
	public EventList(ILogBrowser aBrowser, IEventFilter aFilter)
	{
		itsLogBrowser = aBrowser;
		itsFilter = aFilter;
		itsBrowser = aBrowser.createBrowser(aFilter);
		itsFormatter = new EventFormatter(itsLogBrowser);
		
		// Find timestamps of first and last event
		itsBrowser.setNextTimestamp(0);
		if (itsBrowser.hasNext())
		{
			itsFirstTimestamp = itsBrowser.next().getTimestamp();
		}
		else itsFirstTimestamp = 0;
		
		itsBrowser.setPreviousTimestamp(Long.MAX_VALUE);
		if (itsBrowser.hasPrevious())
		{
			itsLastTimestamp = itsBrowser.previous().getTimestamp();
		}
		else itsLastTimestamp = 0;
		
		createUI();
		setTimestamp(itsFirstTimestamp);
		update();
	}

	private void createUI()
	{
		setLayout(new BorderLayout());
		
		final MuralScroller theScroller = new MuralScroller(
				itsLogBrowser.createBrowser(itsFilter), //We can't share the event browser 
				itsFirstTimestamp, 
				itsLastTimestamp);

		theScroller.pTrackScroll().addHardListener(new PropertyListener<Long>()
				{
					@Override
					public void propertyChanged(IProperty<Long> aProperty, Long aOldValue, Long aNewValue)
					{
						if (itsUpdating) return;
						track(aNewValue);
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
							unitUp();
							break;
							
						case DOWN:
							unitDown();
							break;
							
						case PAGE_UP:
							blockUp();
							break;
							
						case PAGE_DOWN:
							blockDown();
							break;
							
						default:
							throw new RuntimeException("Not handled: "+aData);
						}
						
						// Update tracker position
						if (itsDisplayedEvents.size() > 0)
						{
							itsUpdating = true;
							long theTimestamp = itsDisplayedEvents.get(0).getTimestamp();
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
			int theIndex = Utils.indexOfIdent(theEvent, itsDisplayedEvents);
			if (theIndex >= 0) itsList.setSelectedIndex(theIndex);
			else itsList.clearSelection();
		}
	}
	
	/**
	 * Updates the displayed events so that the first displayed
	 * event is at or immediately after the specified timestamp. 
	 */
	private void setTimestamp(long aTimestamp)
	{
		itsDisplayedEvents.clear();
		
		itsBrowser.setNextTimestamp(aTimestamp);
		itsCurrentDelta = 0;
		
		for(int i=0;i<itsVisibleEvents && itsBrowser.hasNext();i++)
		{
			itsDisplayedEvents.add(itsBrowser.next());
			itsCurrentDelta++;
		}
	}
	
	protected void unitUp()
	{
		while (itsCurrentDelta > 0)
		{
			ILogEvent theEvent = itsBrowser.previous();
			itsCurrentDelta--;
			
			// Check consistency
			if (itsDisplayedEvents.size() > itsCurrentDelta)
			{
				ILogEvent theDisplayedEvent = itsDisplayedEvents.get(itsCurrentDelta);
				assert theDisplayedEvent.getPointer().equals(theEvent.getPointer());
			}
		}
		
		if (itsBrowser.hasPrevious())
		{
			assert itsCurrentDelta == 0;
			ILogEvent theEvent = itsBrowser.previous();
			
			itsDisplayedEvents.addFirst(theEvent);
			itsDisplayedEvents.removeLast();
		}
	}
	
	protected void unitDown()
	{
		while (itsCurrentDelta < itsVisibleEvents)
		{
			if (! itsBrowser.hasNext()) break;
			
			ILogEvent theEvent = itsBrowser.next();
			
			// Check consistency
			if (itsDisplayedEvents.size() > itsCurrentDelta)
			{
				ILogEvent theDisplayedEvent = itsDisplayedEvents.get(itsCurrentDelta);
				assert theDisplayedEvent.getPointer().equals(theEvent.getPointer());
			}
			
			itsCurrentDelta++;
		}
		
		// This could happen if the number of visible events has reduced
		while (itsCurrentDelta > itsVisibleEvents)
		{
			ILogEvent theEvent = itsBrowser.previous();
			itsCurrentDelta--;
			
			// Check consistency
			if (itsDisplayedEvents.size() > itsCurrentDelta)
			{
				ILogEvent theDisplayedEvent = itsDisplayedEvents.get(itsCurrentDelta);
				assert theDisplayedEvent.getPointer().equals(theEvent.getPointer());
			}
		}
		
		if (itsCurrentDelta == itsVisibleEvents && itsBrowser.hasNext())
		{
			ILogEvent theEvent = itsBrowser.next();
			
			itsDisplayedEvents.addLast(theEvent);
			itsDisplayedEvents.removeFirst();
		}
	}
	
	protected void blockUp()
	{
		for (int i=0;i<itsVisibleEvents-1;i++)
		{
			unitUp();
		}
	}
	
	protected void blockDown()
	{
		for (int i=0;i<itsVisibleEvents-1;i++)
		{
			unitDown();
		}
	}
	
	protected void track(long aValue)
	{
		setTimestamp(aValue);
		update();
	}
	
	private class MyListModel extends AbstractListModel
	{
		public Object getElementAt(int aIndex)
		{
			return itsDisplayedEvents.get(aIndex);
		}

		public int getSize()
		{
			return itsDisplayedEvents.size();
		}
		
		public void fireChanged()
		{
			fireContentsChanged(this, 0, getSize());
		}
	}
	
	
}
