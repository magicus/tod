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
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tod.gui.BrowserNavigator;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.Resources;
import tod.gui.seed.LogViewSeed;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.ui.MousePanel;
import zz.utils.ui.UIUtils;
import zz.utils.ui.popup.ButtonPopupComponent;

/**
 * A browser-like back button that can also show the navigation stack
 * @author gpothier
 */
public class NavBackButton extends JPanel
{
	private final IGUIManager itsGUIManager;
	private final BrowserNavigator<LogViewSeed> itsNavigator;
	private NavStackPanel itsNavStackPanel;
	private ButtonPopupComponent itsNavPopupButton;
	
	public NavBackButton(IGUIManager aGUIManager, BrowserNavigator<LogViewSeed> aNavigator)
	{
		itsGUIManager = aGUIManager;
		itsNavigator = aNavigator;
		itsNavStackPanel = new NavStackPanel();
		createUI();
	}

	private void createUI()
	{
		setLayout(new BorderLayout());
		
		JButton theBackButton = new JButton(itsNavigator.getBackwardAction());
		add(theBackButton, BorderLayout.CENTER);
		
		JButton theArrowButton = new JButton(Resources.ICON_TRIANGLE_DOWN.asIcon(10));
		theArrowButton.setMargin(UIUtils.NULL_INSETS);
		
		itsNavPopupButton = new ButtonPopupComponent(itsNavStackPanel, theArrowButton);
		
		itsNavPopupButton.ePopupShowing().addListener(new IEventListener<Void>()
		{
			public void fired(IEvent< ? extends Void> aEvent, Void aData)
			{
				itsNavStackPanel.setup(itsNavigator.getBackwardSeeds());
			}
		});
		add(itsNavPopupButton, BorderLayout.EAST);
	}
	
	private static JLabel createShortDesc(LogViewSeed aSeed)
	{
		String theDescription = aSeed.getShortDescription();
		if (theDescription == null) return null;
		
		JLabel theLabel = new JLabel(theDescription);
		return theLabel;
	}
	
	private static JLabel createKindDesc(LogViewSeed aSeed)
	{
		JLabel theLabel = new JLabel(aSeed.getKindDescription());
		return theLabel;
	}
	
	private static JComponent createSeparator(int aSize)
	{
		JPanel thePanel = new JPanel(null);
		thePanel.setPreferredSize(new Dimension(0, aSize));
		thePanel.setBackground(Color.black);
		return thePanel;
	}
	
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
			setLayout(new BorderLayout());
			
			itsSimilarSeedsPanel = new JPanel(GUIUtils.createStackLayout());
			add(new JScrollPane(itsSimilarSeedsPanel));
			
			add(createSeparator(2));
			
			itsOtherSeedsPanel = new JPanel(GUIUtils.createStackLayout());
			add(new JScrollPane(itsOtherSeedsPanel));
		}
		
		private void setup(Collection<LogViewSeed> aBackwardSeeds)
		{
			itsSimilarSeedsPanel.removeAll();
			itsOtherSeedsPanel.removeAll();
			
			Class theFirstClass = null;
			Iterator<LogViewSeed> theIterator = aBackwardSeeds.iterator();
			
			LogViewSeed theSeed = null;
			boolean theKindShown = false;

			while(theIterator.hasNext())
			{
				theSeed = theIterator.next();
				if (theFirstClass == null) theFirstClass = theSeed.getClass();
				else if (! theFirstClass.equals(theSeed.getClass())) break;

				if (! theKindShown)
				{
					itsSimilarSeedsPanel.add(createKindDesc(theSeed));
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
	private class SeedPanel extends MousePanel
	{
		private final LogViewSeed itsSeed;

		public SeedPanel(LogViewSeed aSeed, boolean aShowKindDesc)
		{
			super(GUIUtils.createStackLayout());
			itsSeed = aSeed;
			
			if (aShowKindDesc)
			{
				JLabel theKindLabel = createKindDesc(aSeed);
				theKindLabel.setOpaque(false);
				add(theKindLabel);
			}

			JLabel theDesc = createShortDesc(aSeed);
			if (theDesc != null)
			{
				theDesc.setOpaque(false);
				add(theDesc);
			}
			else if (!aShowKindDesc)
			{
				// Show something anyway here...
				JLabel theKindLabel = createKindDesc(aSeed);
				theKindLabel.setOpaque(false);
				add(theKindLabel);
			}
			
			setBackground(Color.WHITE);
		}
		
		@Override
		public void mouseEntered(MouseEvent aE)
		{
			setBackground(UIUtils.getLighterColor(Color.BLUE));
		}
		
		@Override
		public void mouseExited(MouseEvent aE)
		{
			setBackground(Color.WHITE);
		}
		
		@Override
		public void mousePressed(MouseEvent aE)
		{
			itsNavPopupButton.hidePopup();
			itsGUIManager.openSeed(itsSeed, false);
		}
	}
}
