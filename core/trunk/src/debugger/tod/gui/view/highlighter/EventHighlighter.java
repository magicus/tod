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

public class EventHighlighter extends JPanel
implements ActionListener
{
	private static final String PROPERTY_INITIAL_CONTEXT = "EventHighlighterView.initialContext";
	
	private final IGUIManager itsGUIManager;
	private final ILogBrowser itsLogBrowser;
	
	private IEventFilter itsCurrentFilter;
	
	private JRadioButton itsGlobalButton;
	private JRadioButton itsPerHostButton;
	private JRadioButton itsPerThreadButton;
	
	private SequenceViewsDock itsDock;

	public EventHighlighter(
			IGUIManager aGUIManager, 
			ILogBrowser aLogBrowser)
	{
		super(new BorderLayout());
		itsGUIManager = aGUIManager;
		itsLogBrowser = aLogBrowser;
		createUI();
	}
	
	public IGUIManager getGUIManager()
	{
		return itsGUIManager;
	}
	
	public ILogBrowser getLogBrowser()
	{
		return itsLogBrowser;
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

	private void createUI()
	{
		itsDock = new SequenceViewsDock(getGUIManager());
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
		
		setFilter(itsCurrentFilter);
	}
	
	/**
	 * Sets the per host aggregation mode.
	 */
	private void perHost()
	{
		itsDock.pSeeds().clear();
		
		for(IHostInfo theHost : getLogBrowser().getHosts())
		{
			IEventFilter theFilter = getLogBrowser().createHostFilter(theHost);
			itsDock.pSeeds().add(new HighlighterSequenceSeed(
					theHost.getName(),
					getLogBrowser().createBrowser(theFilter),
					null));				
		}
		
		setFilter(itsCurrentFilter);
	}
	
	/**
	 * Sets the per thread aggregation mode.
	 */
	private void perThread()
	{
		itsDock.pSeeds().clear();
		
		for(IThreadInfo theThread : getLogBrowser().getThreads())
		{
			IEventFilter theFilter = getLogBrowser().createThreadFilter(theThread);
			itsDock.pSeeds().add(new HighlighterSequenceSeed(
					"["+theThread.getHost().getName()+"] \""+theThread.getName() + "\"",
					getLogBrowser().createBrowser(theFilter),
					null));				
		}
		
		setFilter(itsCurrentFilter);
	}
	
	/**
	 * Sets the filter of all stripes.
	 */
	public void setFilter(IEventFilter aFilter)
	{
		itsCurrentFilter = aFilter;
		for (IEventSequenceSeed theSeed : itsDock.pSeeds())
		{
			HighlighterSequenceSeed theHighlighterSeed = (HighlighterSequenceSeed) theSeed;
			theHighlighterSeed.setFilter(aFilter);
		}
	}
}
