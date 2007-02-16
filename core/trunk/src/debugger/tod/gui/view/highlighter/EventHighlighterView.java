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
package tod.gui.view.highlighter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.gui.IGUIManager;
import tod.gui.MinerUI;
import tod.gui.eventsequences.IEventSequenceSeed;
import tod.gui.eventsequences.SequenceViewsDock;
import tod.gui.view.LogView;

/**
 * A base class that can be used for views that must highlight a set
 * of events in a global/per host/per thread context.
 * @author gpothier
 */
public abstract class EventHighlighterView extends LogView
{
	private static final String PROPERTY_INITIAL_CONTEXT = "EventHighlighterView.initialContext";
	private StripesPanel itsStripesPanel;
	

	public EventHighlighterView(
			IGUIManager aGUIManager, 
			ILogBrowser aLog)
	{
		super(aGUIManager, aLog);
		itsStripesPanel = new StripesPanel();
	}
	
	public void setFilter(IEventFilter aFilter)
	{
		itsStripesPanel.setFilter(aFilter);
	}
	
	
	protected JComponent createStripesPanel()
	{
		return itsStripesPanel;
	}
	
	/**
	 * Returns the kind of context to display at startup. 
	 */
	protected Context getInitialContext()
	{
		String theName = MinerUI.getStringProperty(getGUIManager(), PROPERTY_INITIAL_CONTEXT, Context.PER_HOST.toString());
		return Context.valueOf(theName);
	}
	
	public static enum Context
	{
		GLOBAL, PER_HOST, PER_THREAD;
	}
	
	/**
	 * The panel that displays event stripes, and also lets
	 * the user choose the aggregation mode (global, per
	 * thread, per host).
	 * @author gpothier
	 */
	private class StripesPanel extends JPanel
	implements ActionListener
	{
		private JRadioButton itsGlobalButton;
		private JRadioButton itsPerHostButton;
		private JRadioButton itsPerThreadButton;
		
		private SequenceViewsDock itsDock;


		public StripesPanel()
		{
			super(new BorderLayout());
			createUI();
		}

		private void createUI()
		{
			itsDock = new SequenceViewsDock(EventHighlighterView.this);
			add(itsDock, BorderLayout.CENTER);
			
			itsDock.pStart().set(getLogBrowser().getFirstTimestamp());
			itsDock.pEnd().set(getLogBrowser().getLastTimestamp());

			
			ButtonGroup theGroup = new ButtonGroup();
			
			itsGlobalButton = new JRadioButton("Global");
			theGroup.add(itsGlobalButton);
			
			itsPerHostButton = new JRadioButton("Hosts");
			theGroup.add(itsPerHostButton);
			
			itsPerThreadButton = new JRadioButton("Threads");
			theGroup.add(itsPerThreadButton);

			Context theInitialContext = getInitialContext();
			switch(theInitialContext)
			{
			case GLOBAL:
				itsGlobalButton.setSelected(true);
				global();
				break;
				
			case PER_HOST:
				itsPerHostButton.setSelected(true);
				perHost();
				break;
				
			case PER_THREAD:
				itsPerThreadButton.setSelected(true);
				perThread();
				break;
				
			default:
				throw new RuntimeException("Not handled: "+theInitialContext);
			}

			itsGlobalButton.addActionListener(this);
			itsPerHostButton.addActionListener(this);
			itsPerThreadButton.addActionListener(this);

			
			JPanel theButtonsPanel = new JPanel();
			theButtonsPanel.add(itsGlobalButton);
			theButtonsPanel.add(itsPerHostButton);
			theButtonsPanel.add(itsPerThreadButton);
			add(theButtonsPanel, BorderLayout.NORTH);			
		}
		
		public void actionPerformed(ActionEvent aE)
		{
			Object theSource = aE.getSource();
			if (theSource == itsGlobalButton)
			{
				getGUIManager().setProperty(PROPERTY_INITIAL_CONTEXT, Context.GLOBAL.toString());
				global();
			}
			else if (theSource == itsPerHostButton)
			{
				getGUIManager().setProperty(PROPERTY_INITIAL_CONTEXT, Context.PER_HOST.toString());
				perHost();
			} 
			else if (theSource == itsPerThreadButton)
			{
				getGUIManager().setProperty(PROPERTY_INITIAL_CONTEXT, Context.PER_THREAD.toString());
				perThread();
			} 
			else throw new RuntimeException("Not handled: "+theSource);
		}
		
		/**
		 * Sets the global aggregation mode.
		 */
		private void global()
		{
			itsDock.pSeeds().clear();
			itsDock.pSeeds().add(new HighlighterSequenceSeed(
					"Global",
					getLogBrowser().createBrowser(),
					null));
		}
		
		/**
		 * Sets the per host aggregation mode.
		 */
		private void perHost()
		{
			itsDock.pSeeds().clear();
			
			for(IHostInfo theHost : getLogBrowser().getHosts())
			{
				if (theHost == null) continue;
				
				IEventFilter theFilter = getLogBrowser().createHostFilter(theHost);
				itsDock.pSeeds().add(new HighlighterSequenceSeed(
						theHost.getName(),
						getLogBrowser().createBrowser(theFilter),
						null));				
			}
		}
		
		/**
		 * Sets the per thread aggregation mode.
		 */
		private void perThread()
		{
			itsDock.pSeeds().clear();
			
			for(IThreadInfo theThread : getLogBrowser().getThreads())
			{
				if (theThread == null) continue;
				
				IEventFilter theFilter = getLogBrowser().createThreadFilter(theThread);
				itsDock.pSeeds().add(new HighlighterSequenceSeed(
						"["+theThread.getHost().getName()+"] \""+theThread.getName() + "\"",
						getLogBrowser().createBrowser(theFilter),
						null));				
			}
		}
		
		/**
		 * Sets the filter of all stripes.
		 */
		public void setFilter(IEventFilter aFilter)
		{
			for (IEventSequenceSeed theSeed : itsDock.pSeeds())
			{
				HighlighterSequenceSeed theHighlighterSeed = (HighlighterSequenceSeed) theSeed;
				theHighlighterSeed.setFilter(aFilter);
			}
		}
	}
}
