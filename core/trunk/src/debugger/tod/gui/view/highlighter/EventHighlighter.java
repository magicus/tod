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
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import tod.gui.MinerUI;
import tod.gui.eventsequences.IEventSequenceSeed;
import tod.gui.eventsequences.SequenceViewsDock;
import tod.gui.eventsequences.mural.AbstractMuralPainter;
import zz.utils.list.IList;
import zz.utils.list.IListListener;
import zz.utils.list.ZArrayList;
import zz.utils.properties.IRWProperty;

public class EventHighlighter extends JPanel
implements ActionListener
{
	private static final String PROPERTY_INITIAL_CONTEXT = "EventHighlighterView.initialContext";
	
	private final IGUIManager itsGUIManager;
	private final ILogBrowser itsLogBrowser;
	
	public final IList<BrowserData> pHighlightBrowsers = new ZArrayList<BrowserData>()
	{
		@Override
		protected void elementAdded(int aIndex, BrowserData aElement)
		{
			for (IEventSequenceSeed theSeed : itsDock.pSeeds())
			{
				HighlighterSequenceSeed theHighlighterSeed = (HighlighterSequenceSeed) theSeed;
				theHighlighterSeed.pForegroundBrowsers.add(aIndex, aElement);
			}
		}
		
		@Override
		protected void elementRemoved(int aIndex, BrowserData aElement)
		{
			for (IEventSequenceSeed theSeed : itsDock.pSeeds())
			{
				HighlighterSequenceSeed theHighlighterSeed = (HighlighterSequenceSeed) theSeed;
				theHighlighterSeed.pForegroundBrowsers.remove(aIndex);
			}
		}
	};
	
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
	
	public IRWProperty<Long> pStart()
	{
		return itsDock.pStart();
	}
	
	public IRWProperty<Long> pEnd()
	{
		return itsDock.pEnd();
	}
	
	/**
	 * Returns the kind of context to display at startup. 
	 */
	protected Context getInitialContext()
	{
		String theName = getGUIManager().getSettings().getStringProperty(PROPERTY_INITIAL_CONTEXT, Context.PER_HOST.toString());
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
		
		setupBrowsers();
	}
	
	public void setMuralPainter(AbstractMuralPainter aMuralPainter)
	{
		itsDock.setMuralPainter(aMuralPainter);
	}
	
	public void actionPerformed(ActionEvent aE)
	{
		Object theSource = aE.getSource();
		if (theSource == itsGlobalButton)
		{
			getGUIManager().getSettings().setProperty(PROPERTY_INITIAL_CONTEXT, Context.GLOBAL.toString());
			global();
		}
		else if (theSource == itsPerHostButton)
		{
			getGUIManager().getSettings().setProperty(PROPERTY_INITIAL_CONTEXT, Context.PER_HOST.toString());
			perHost();
		} 
		else if (theSource == itsPerThreadButton)
		{
			getGUIManager().getSettings().setProperty(PROPERTY_INITIAL_CONTEXT, Context.PER_THREAD.toString());
			perThread();
		} 
		else throw new RuntimeException("Not handled: "+theSource);
	}
	
	protected IEventSequenceSeed createGlobalSeed()
	{
		return new HighlighterSequenceSeed("Global", getLogBrowser().createBrowser(), null);
	}
	
	/**
	 * Sets the global aggregation mode.
	 */
	protected void global()
	{
		itsDock.pSeeds().clear();
		itsDock.pSeeds().add(createGlobalSeed());

		setupBrowsers();
	}
	
	protected IEventSequenceSeed createHostSeed(IHostInfo aHost)
	{
		IEventFilter theFilter = getLogBrowser().createHostFilter(aHost);
		return new HighlighterSequenceSeed(
				aHost.getName(),
				getLogBrowser().createBrowser(theFilter),
				null);				
		
	}
	
	
	/**
	 * Sets the per host aggregation mode.
	 */
	protected void perHost()
	{
		itsDock.pSeeds().clear();
		
		for(IHostInfo theHost : getLogBrowser().getHosts())
		{
			itsDock.pSeeds().add(createHostSeed(theHost));				
		}
		
		setupBrowsers();
	}
	
	protected IEventSequenceSeed createThreadSeed(IThreadInfo aThread)
	{ 
		IEventFilter theFilter = getLogBrowser().createThreadFilter(aThread);
		return new HighlighterSequenceSeed(
				"["+aThread.getHost().getName()+"] \""+aThread.getName() + "\"",
				getLogBrowser().createBrowser(theFilter),
				null);				
	}
	
	/**
	 * Sets the per thread aggregation mode.
	 */
	protected void perThread()
	{
		itsDock.pSeeds().clear();
		
		for(IThreadInfo theThread : getLogBrowser().getThreads())
		{
			itsDock.pSeeds().add(createThreadSeed(theThread));				
		}
		
		setupBrowsers();
	}

	/**
	 * Initial forwarding of highlight browsers to the seeds.
	 */
	private void setupBrowsers()
	{
		for (IEventSequenceSeed theSeed : itsDock.pSeeds())
		{
			HighlighterSequenceSeed theHighlighterSeed = (HighlighterSequenceSeed) theSeed;
			for (BrowserData theBrowserData : pHighlightBrowsers)
			{
				theHighlighterSeed.pForegroundBrowsers.add(theBrowserData);
			}
		}
	}
}
