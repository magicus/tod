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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.formatter.EventFormatter;
import zz.utils.Utils;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.FormattedRenderer;

/**
 * A component that displays a list of events obtained from a 
 * {@link IEventBrowser}.
 * @author gpothier
 */
public class EventList extends JPanel
{
	/**
	 * Range of the scrollbar. We choose a huge value in order to
	 * differentiate unit/block increments/decrements from tracking.
	 */
	private static final int SCROLL_RANGE = 1000000;
	private static final int COUNT_SLOTS = 500;
	
	private final IEventBrowser itsBrowser;
	private long itsFirstTimestamp;
	private long itsLastTimestamp;
	
	private MyListModel itsModel = new MyListModel();
	private EventFormatter itsFormatter;
	
	private long[] itsCounts;
	
	private JScrollBar itsScrollBar;
	private int itsLastScrollValue;
	
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
	
	public EventList(IEventBrowser aBrowser)
	{
		itsBrowser = aBrowser;
		itsFormatter = new EventFormatter(aBrowser.getLogBrowser());
		
		// Find timestamps of first and last event
		aBrowser.setNextTimestamp(0);
		if (aBrowser.hasNext())
		{
			itsFirstTimestamp = aBrowser.next().getTimestamp();
		}
		else itsFirstTimestamp = 0;
		
		aBrowser.setPreviousTimestamp(Long.MAX_VALUE);
		if (aBrowser.hasPrevious())
		{
			itsLastTimestamp = aBrowser.previous().getTimestamp();
		}
		else itsLastTimestamp = 0;
		
		itsCounts = aBrowser.getEventCounts(itsFirstTimestamp, itsLastTimestamp, COUNT_SLOTS, false);
		
		createUI();
		setTimestamp(itsFirstTimestamp);
		update();
	}

	private void createUI()
	{
		setLayout(new BorderLayout());
		
		itsScrollBar = new JScrollBar(JScrollBar.VERTICAL);
		itsScrollBar.setMinimum(0);
		itsScrollBar.setMaximum(SCROLL_RANGE);
		itsScrollBar.setUnitIncrement(1);
		itsScrollBar.setBlockIncrement(2);
		itsScrollBar.addAdjustmentListener(new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent aE)
			{
				int theValue = aE.getValue();

				int theDelta = theValue-itsLastScrollValue;
				switch(theDelta)
				{
				case -1:
					unitUp();
					break;
					
				case 1:
					unitDown();
					break;
					
				case -2:
					blockUp();
					break;
					
				case 2:
					blockDown();
					break;
					
				case 0:
					break;
					
				default:
					track(theValue);
				}
				
				update();
				itsLastScrollValue = theValue;
			}
		});
		
		add(itsScrollBar, BorderLayout.EAST);
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
		
//		MuralScroller theScroller = new MuralScroller(itsBrowser);
//		GraphicNode theNode = new GraphicNode(theScroller);
//		GraphicPanel theGraphicPanel = new GraphicPanel(theNode);
//		theGraphicPanel.setPreferredSize(new Dimension(90, 100));
//		add(theGraphicPanel, BorderLayout.SOUTH);
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
	
	protected void track(int aValue)
	{
		
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
