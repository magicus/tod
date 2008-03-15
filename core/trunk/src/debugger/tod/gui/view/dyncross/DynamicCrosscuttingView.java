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
package tod.gui.view.dyncross;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IStructureDatabase.AspectInfo;
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import tod.gui.kit.SavedSplitPane;
import tod.gui.seed.DynamicCrosscuttingSeed;
import tod.gui.seed.DynamicCrosscuttingSeed.AdviceHighlight;
import tod.gui.seed.DynamicCrosscuttingSeed.AspectHighlight;
import tod.gui.seed.DynamicCrosscuttingSeed.Highlight;
import tod.gui.view.LogView;
import tod.gui.view.highlighter.EventHighlighter;
import zz.utils.SimpleListModel;
import zz.utils.list.IList;
import zz.utils.list.IListListener;
import zz.utils.ui.StackLayout;

/**
 * This view displays the dynamic crosscutting of aspects
 * (aka. aspect murals).
 * @author gpothier
 */
public class DynamicCrosscuttingView extends LogView
implements IListListener<Highlight>
{
	private static final Color[] COLORS = {
		Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE,
		Color.PINK, Color.RED, Color.WHITE, Color.YELLOW
	};
	
	private final DynamicCrosscuttingSeed itsSeed;
	private EventHighlighter itsHighlighter;
	
	public DynamicCrosscuttingView(IGUIManager aGUIManager, ILogBrowser aLog, DynamicCrosscuttingSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
	}

	@Override
	public void init()
	{
		super.init();
		
		IStructureDatabase theStructureDatabase = getLogBrowser().getStructureDatabase();
		Map<String, AspectInfo> theMap = theStructureDatabase.getAspectInfoMap();
		
		// Create aspects list
		List<AspectHighlight> theAspectsList = new ArrayList<AspectHighlight>();
		for (AspectInfo theAspectInfo : theMap.values())
		{
			theAspectsList.add(new AspectHighlight(theAspectInfo));
		}
		
		// Create advices list
		List<AdviceHighlight> theAdvicesList = new ArrayList<AdviceHighlight>();
		for (AspectHighlight theHighlight : theAspectsList)
		{
			for (int theAdviceSourceId : theHighlight.getAspectInfo().getAdviceIds())
			{
				theAdvicesList.add(new AdviceHighlight(
						theAdviceSourceId,
						theStructureDatabase.getAdviceSource(theAdviceSourceId)));
			}
		}
		
		MouseListener theAddListener = new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent aE)
			{
				if (aE.getButton() != MouseEvent.BUTTON1) return;
				
				JList theList = (JList) aE.getSource();
				Highlight theHighlight = (Highlight) theList.getSelectedValue();

				if (aE.getClickCount() == 1)
				{
					theHighlight.gotoSource(getGUIManager());
				}
				else if (aE.getClickCount() == 2)
				{
					itsSeed.pHighlights.add(theHighlight);
					
					System.out.println("Events for: "+theHighlight);
					IEventBrowser theBrowser = theHighlight.createBrowser(getLogBrowser());
					while (theBrowser.hasNext())
					{
						ILogEvent theEvent = theBrowser.next();
						System.out.println(theEvent);
					}
				}
			}
		};
		
		JSplitPane theSplitPane = new SavedSplitPane(getGUIManager(), "dynamicCrosscuttingView.splitterPos");

		// Left part
		JList theAspectsJList = new JList(new SimpleListModel(theAspectsList));
		JList theAdvicesJList = new JList(new SimpleListModel(theAdvicesList));
		
		theAspectsJList.addMouseListener(theAddListener);
		theAdvicesJList.addMouseListener(theAddListener);
		
		JTabbedPane theTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		theTabbedPane.addTab("Aspects", new JScrollPane(theAspectsJList));
		theTabbedPane.addTab("Advices", new JScrollPane(theAdvicesJList));
		
		// Right part
		JPanel theRightPanel = new JPanel(new BorderLayout());
		theRightPanel.add(new LegendPanel(), BorderLayout.SOUTH);
		
		itsHighlighter = new EventHighlighter(getGUIManager(), getLogBrowser());
		theRightPanel.add(itsHighlighter, BorderLayout.CENTER);
		
		theSplitPane.setLeftComponent(theTabbedPane);
		theSplitPane.setRightComponent(theRightPanel);
		
		setLayout(new StackLayout());
		add(theSplitPane);
	
		setupHighlights();
		
		connect(itsSeed.pStart, itsHighlighter.pStart(), true);
		connect(itsSeed.pEnd, itsHighlighter.pEnd(), true);
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		itsSeed.pHighlights.addHardListener(this);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsSeed.pHighlights.removeListener(this);
	}

	/**
	 * Initial setup of highlights
	 */
	private void setupHighlights()
	{
		int i=0;
		for(Highlight theHighlight : itsSeed.pHighlights)
		{
			itsHighlighter.pHighlightBrowsers.add(new BrowserData(
					theHighlight.createBrowser(getLogBrowser()),
					COLORS[i++],
					BrowserData.DEFAULT_MARK_SIZE*2));
		}
	}
	
	public void elementAdded(IList<Highlight> aList, int aIndex, Highlight aElement)
	{
		itsHighlighter.pHighlightBrowsers.add(aIndex, new BrowserData(
				aElement.createBrowser(getLogBrowser()),
				COLORS[aIndex],
				BrowserData.DEFAULT_MARK_SIZE*2));
	}

	public void elementRemoved(IList<Highlight> aList, int aIndex, Highlight aElement)
	{
		itsHighlighter.pHighlightBrowsers.remove(aIndex);
	}

	private class LegendPanel extends JPanel
	implements IListListener<Highlight>
	{
		public LegendPanel()
		{
			super(new FlowLayout());
			int i=0;
			for(Highlight theHighlight : itsSeed.pHighlights)
			{
				add(new LegendItem(theHighlight, COLORS[i++]));
			}
		}
		
		@Override
		public void addNotify()
		{
			super.addNotify();
			itsSeed.pHighlights.addHardListener(this);
		}
		
		@Override
		public void removeNotify()
		{
			super.removeNotify();
			itsSeed.pHighlights.removeListener(this);
		}

		public void elementAdded(IList<Highlight> aList, int aIndex, Highlight aElement)
		{
			add(new LegendItem(aElement, COLORS[aIndex]), null, aIndex);
			revalidate();
			repaint();
		}

		public void elementRemoved(IList<Highlight> aList, int aIndex, Highlight aElement)
		{
			remove(aIndex);
		}
		
	}
	
	private static class LegendItem extends JPanel
	{
		private final Highlight itsHighlight;
		
		public LegendItem(Highlight aHighlight, Color aMarkColor)
		{
			super(new FlowLayout(FlowLayout.LEFT));
			
			itsHighlight = aHighlight;
			
			JPanel theColorPanel = new JPanel(null);
			theColorPanel.setBackground(aMarkColor);
			theColorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			theColorPanel.setPreferredSize(new Dimension(30, 20));
			add(theColorPanel);
			
			add(new JLabel(itsHighlight.toString()));
		}
	}
}
