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
