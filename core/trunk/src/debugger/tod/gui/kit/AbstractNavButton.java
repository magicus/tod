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
package tod.gui.kit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tod.gui.BrowserNavigator;
import tod.gui.GUIUtils;
import tod.gui.Resources;
import tod.gui.kit.html.HtmlBody;
import tod.gui.kit.html.HtmlComponent;
import tod.gui.kit.html.HtmlDoc;
import tod.gui.kit.html.HtmlElement;
import tod.gui.kit.html.HtmlRaw;
import tod.gui.kit.html.HtmlText;
import tod.gui.seed.LogViewSeed;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.ui.ScrollablePanel;
import zz.utils.ui.UIUtils;
import zz.utils.ui.popup.ButtonPopupComponent;

/**
 * Base for the buttons that permits to navigate forward or backward in
 * the seeds stacks from a {@link BrowserNavigator}.
 * @author gpothier
 */
public abstract class AbstractNavButton extends JPanel
{
	private final BrowserNavigator<LogViewSeed> itsNavigator;
	private NavStackPanel itsNavStackPanel;
	private ButtonPopupComponent itsNavPopupButton;
	
	public AbstractNavButton(BrowserNavigator<LogViewSeed> aNavigator)
	{
		itsNavigator = aNavigator;
		itsNavStackPanel = new NavStackPanel();
		createUI();
	}
	
	public BrowserNavigator<LogViewSeed> getNavigator()
	{
		return itsNavigator;
	}

	private void createUI()
	{
		setLayout(new BorderLayout());
		
		JButton theBackButton = new JButton(getAction());
		add(theBackButton, BorderLayout.CENTER);
		
		JButton theArrowButton = new JButton(Resources.ICON_TRIANGLE_DOWN.asIcon(10));
		theArrowButton.setMargin(UIUtils.NULL_INSETS);
		
		itsNavPopupButton = new ButtonPopupComponent(itsNavStackPanel, theArrowButton);
		
		itsNavPopupButton.ePopupShowing().addListener(new IEventListener<Void>()
		{
			public void fired(IEvent< ? extends Void> aEvent, Void aData)
			{
				itsNavStackPanel.setup();
			}
		});
		add(itsNavPopupButton, BorderLayout.EAST);
	}
	
	private static HtmlElement createShortDesc(LogViewSeed aSeed)
	{
		String theDescription = aSeed.getShortDescription();
		if (theDescription == null) return null;
		
		return HtmlText.create(theDescription);
	}
	
	private static HtmlElement createKindDesc(LogViewSeed aSeed)
	{
		return HtmlText.create(aSeed.getKindDescription(), 75, Color.DARK_GRAY);
	}
	
	private static JComponent createSeparator(int aSize)
	{
		JPanel thePanel = new JPanel(null);
		thePanel.setPreferredSize(new Dimension(0, aSize));
		thePanel.setBackground(Color.BLACK);
		return thePanel;
	}
	
	protected abstract Action getAction();
	
	protected abstract Iterable<LogViewSeed> getSeedsStack();
	
	/**
	 * The panel that shows the backward navigation stack.
	 * @author gpothier
	 */
	private class NavStackPanel extends JPanel
	{
		private JPanel itsSimilarSeedsPanel;
		private JPanel itsOtherSeedsPanel;
		
		public NavStackPanel()
		{
			createUI();
		}

		private void createUI()
		{
			removeAll();
			setLayout(new BorderLayout());
			setPreferredSize(new Dimension(300, 400));
			
			itsSimilarSeedsPanel = new ScrollablePanel(GUIUtils.createStackLayout())
			{
				@Override
				public boolean getScrollableTracksViewportWidth()
				{
					return true;
				}
			};
			itsSimilarSeedsPanel.setBackground(Color.white);
			JScrollPane theSimScrollPane = new JScrollPane(itsSimilarSeedsPanel);
			add(theSimScrollPane, BorderLayout.CENTER);
			
//			add(createSeparator(2));
			
			itsOtherSeedsPanel = new ScrollablePanel(GUIUtils.createStackLayout())
			{
				@Override
				public boolean getScrollableTracksViewportWidth()
				{
					return true;
				}
			};
			JScrollPane theOtherScrollPane = new JScrollPane(itsOtherSeedsPanel);
			add(theOtherScrollPane, BorderLayout.SOUTH);
		}
		
		private void setup()
		{
			createUI(); // temp
			
			itsSimilarSeedsPanel.removeAll();
			itsOtherSeedsPanel.removeAll();
			
			Class theFirstClass = null;
			Iterator<LogViewSeed> theIterator = getSeedsStack().iterator();
			
			LogViewSeed theSeed = null;
			boolean theKindShown = false;

			while(theIterator.hasNext())
			{
				theSeed = theIterator.next();
				if (theFirstClass == null) theFirstClass = theSeed.getClass();
				else if (! theFirstClass.equals(theSeed.getClass())) break;

				if (! theKindShown)
				{
					itsSimilarSeedsPanel.add(new HtmlComponent(HtmlDoc.create(createKindDesc(theSeed))));
					theKindShown = true;
				}
				
				itsSimilarSeedsPanel.add(new SeedPanel(theSeed, false));
			}
			
			while(theSeed != null)
			{
				itsOtherSeedsPanel.add(new SeedPanel(theSeed, true));
				
				if (! theIterator.hasNext()) break;
				theSeed = theIterator.next();
				
				itsOtherSeedsPanel.add(createSeparator(1));
			} 
			
			revalidate();
			repaint();
		}
	}
	
	/**
	 * This panel represents a single seed.
	 * @author gpothier
	 */
	private class SeedPanel extends HtmlComponent
	implements MouseListener
	{
		private final LogViewSeed itsSeed;

		public SeedPanel(LogViewSeed aSeed, boolean aShowKindDesc)
		{
			addMouseListener(this);
			itsSeed = aSeed;
			
			HtmlBody theBody = new HtmlBody();
			
			HtmlElement theShortDesc = createShortDesc(aSeed);
			if (aShowKindDesc || theShortDesc == null) 
			{
				theBody.add(createKindDesc(aSeed));
				theBody.add(HtmlRaw.create("<br>"));
			}
			if (theShortDesc != null) theBody.add(theShortDesc);
			
			setDoc(HtmlDoc.create(theBody));
			
			setBackground(Color.WHITE);
		}
		
		public void mouseEntered(MouseEvent aE)
		{
			setBackground(UIUtils.getLighterColor(Color.BLUE));
		}
		
		public void mouseExited(MouseEvent aE)
		{
			setBackground(Color.WHITE);
		}
		
		public void mousePressed(MouseEvent aE)
		{
			itsNavPopupButton.hidePopup();
			itsNavigator.backToSeed(itsSeed);
		}

		public void mouseClicked(MouseEvent aE)
		{
		}

		public void mouseReleased(MouseEvent aE)
		{
		}
	}
}
