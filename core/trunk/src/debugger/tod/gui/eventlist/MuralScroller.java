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

import tod.core.DebugFlags;
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
	 * If p is the position of the slider the timestamp t is:
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
		if (DebugFlags.COMPUTE_SCROLLER_MURALS)
		{
			itsMural.pStart().set(itsStart);
			itsMural.pEnd().set(itsEnd);
			itsMural.pEventBrowsers().clear();
			itsMural.pEventBrowsers().add(new BrowserData(itsBrowser, Color.BLACK));
			itsMural.repaint();
		}
		
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
