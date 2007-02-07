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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicArrowButton;

import tod.core.database.browser.IEventBrowser;
import tod.gui.BrowserData;
import tod.gui.eventsequences.EventMural;
import zz.utils.notification.IEvent;
import zz.utils.notification.SimpleEvent;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.Autorepeat;
import zz.utils.ui.Orientation;

/**
 * A widget that permits to scroll in an {@link IEventBrowser}.
 * There are two scrolling modes:
 * <li>Unit scrolling: moves forward or backwards by a certain amount of events
 * <li>Track scrolling: moves to a given timestamp
 * @author gpothier
 */
public class MuralScroller extends JPanel
{
	private static final int THICKNESS = 25;
	
	public static enum UnitScroll
	{
		UP, PAGE_UP, DOWN, PAGE_DOWN
	}
	
	private SimpleEvent<UnitScroll> eUnitScroll = new SimpleEvent<UnitScroll>();
	private IRWProperty<Long> pTrackScroll = new SimpleRWProperty<Long>()
	{
		@Override
		protected void changed(Long aOldValue, Long aNewValue)
		{
			if (itsUpdating) return;
			itsSlider.setValue((int) ((aNewValue-itsStart)/itsSliderFactor));
		}
	};
	
	private IEventBrowser itsBrowser;
	
	private EventMural itsMural;
	private JSlider itsSlider;
	private long itsStart;
	private long itsEnd;
	private boolean itsUpdating = false;
		
	/**
	 * If p is the position of the sliderm the timestamp t is:
	 * p*{@link #itsSliderFactor} + {@link #itsStart} 
	 */
	private long itsSliderFactor;

	public MuralScroller()
	{
		createUI();		
	}
	
	public MuralScroller(IEventBrowser aBrowser, long aStart, long aEnd)
	{
		this();
		set(aBrowser, aStart, aEnd);
	}

	private void createUI()
	{
		itsMural = new EventMural(Orientation.VERTICAL);
		itsMural.setPreferredSize(new Dimension(THICKNESS, 100));

		// Setup slider
		
		itsSlider = new JSlider(JSlider.VERTICAL);
		itsSlider.setInverted(true);
		itsSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent aE)
			{
				long theTimestamp = (itsSliderFactor * itsSlider.getValue()) + itsStart;
				itsUpdating = true;
				pTrackScroll.set(theTimestamp);
				itsUpdating = false;
			}
		});
		
		// Setup center container (mural + cursor)
		JPanel theCenterContainer = new JPanel(new BorderLayout());
		theCenterContainer.add(itsMural, BorderLayout.CENTER);
		theCenterContainer.add(itsSlider, BorderLayout.WEST);
		
		// Setup main container (center + buttons)
		setLayout(new BorderLayout());
		
		JButton theUpButton = new BasicArrowButton(BasicArrowButton.NORTH);
		Autorepeat.install(theUpButton, new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				eUnitScroll.fire(UnitScroll.UP);
			}
		});
		
		JButton theDownButton = new BasicArrowButton(BasicArrowButton.SOUTH);
		Autorepeat.install(theDownButton, new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				eUnitScroll.fire(UnitScroll.DOWN);
			}
		});
		
		add(theUpButton, BorderLayout.NORTH);
		add(theDownButton, BorderLayout.SOUTH);
		add(theCenterContainer, BorderLayout.CENTER);
	}

	public void set(IEventBrowser aBrowser, long aStart, long aEnd)
	{
		itsBrowser = aBrowser;
		itsStart = aStart;
		itsEnd = aEnd;

		// Setup mural
		itsMural.pStart().set(itsStart);
		itsMural.pEnd().set(itsEnd);
		itsMural.pEventBrowsers().clear();
		itsMural.pEventBrowsers().add(new BrowserData(itsBrowser, Color.BLACK));
		itsMural.repaint();
		
		// Setup slider
		long theDelta = itsEnd-itsStart;
		itsSliderFactor = 1;
		while (theDelta > Integer.MAX_VALUE)
		{
			theDelta /= 2;
			itsSliderFactor *= 2;
		}

		itsSlider.setMinimum(0);
		itsSlider.setMaximum((int) theDelta);
		itsSlider.setValue(0);
	}
	
	/**
	 * This property holds the current tracker timestamp.
	 * The client is responsible of updating the tracker position
	 * according to unit scrolls.
	 */
	public IRWProperty<Long> pTrackScroll()
	{
		return pTrackScroll;
	}

	/**
	 * This event is fired when the user performs unit/block scrolling
	 */
	public IEvent<UnitScroll> eUnitScroll()
	{
		return eUnitScroll;
	}

	
	
}
