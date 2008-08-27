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
package tod.gui.components.objectwatch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ObjectId;
import tod.gui.BrowserNavigator;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.IContext;
import tod.gui.IGUIManager;
import tod.gui.components.objectwatch.AbstractWatchProvider.Entry;
import tod.gui.kit.AsyncPanel;
import tod.gui.kit.Bus;
import tod.gui.kit.BusPanel;
import tod.gui.kit.IBusListener;
import tod.gui.kit.messages.ShowObjectMsg;
import tod.tools.scheduling.IJobScheduler;
import tod.tools.scheduling.JobGroup;
import tod.tools.scheduling.IJobScheduler.JobPriority;
import zz.utils.SimpleAction;
import zz.utils.ui.ScrollablePanel;

/**
 * A panel that shows the contents of a stack frame or of an object.
 * @author gpothier
 */
public class ObjectWatchPanel extends BusPanel
{
	private final IContext itsContext;
	private WatchBrowserNavigator itsBrowserNavigator;
	private JobGroup itsJobGroup;
	private JScrollPane itsScrollPane;
	private AbstractWatchProvider itsProvider;
	private List<Entry> itsEntries;
	
	private IBusListener<ShowObjectMsg> itsShowObjectListener = new IBusListener<ShowObjectMsg>()
	{
		public boolean processMessage(ShowObjectMsg aMessage)
		{
			openWatch(new ObjectWatchSeed(
					getGUIManager(),
					aMessage.getTitle(), 
					ObjectWatchPanel.this,
					aMessage.getRefEvent(), 
					aMessage.getObjectId()));
			
			return true;
		}
	};
	
	public ObjectWatchPanel(IContext aContext)
	{
		super(aContext.getBus());
		itsContext = aContext;
		itsJobGroup = new JobGroup(getGUIManager().getJobScheduler());
		itsBrowserNavigator = new WatchBrowserNavigator(itsJobGroup);
	}
	
	private void createUI()
	{
		setLayout(new BorderLayout());
		
		JPanel theToolbar = new JPanel();
		
		Action theShowFrameAction = new SimpleAction("frame")
		{
			public void actionPerformed(ActionEvent aE)
			{
				showStackFrame();
			}
		};
		
		theToolbar.add(new JButton(theShowFrameAction));
		theToolbar.add(new JButton(itsBrowserNavigator.getBackwardAction()));
		theToolbar.add(new JButton(itsBrowserNavigator.getForwardAction()));
		
		add(theToolbar, BorderLayout.NORTH);
		
		itsScrollPane = new JScrollPane();
		itsScrollPane.getViewport().setBackground(Color.WHITE);
		add(itsScrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		Bus.get(this).subscribe(ShowObjectMsg.ID, itsShowObjectListener);
		
		createUI();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsJobGroup.cancelAll();
		Bus.get(this).unsubscribe(ShowObjectMsg.ID, itsShowObjectListener);
	}
	
	public IGUIManager getGUIManager()
	{
		return itsContext.getGUIManager();
	}
	
	public IContext getContext()
	{
		return itsContext;
	}
	
	/**
	 * Returns a job processor that only contains jobs for this watch
	 * panel.
	 */
	public IJobScheduler getJobScheduler()
	{
		return itsJobGroup;
	}

	public void showStackFrame()
	{
		ILogEvent theRefEvent = getContext().pSelectedEvent().get();
		if (theRefEvent == null) return;
		
		itsBrowserNavigator.open(new StackFrameWatchSeed(
				getGUIManager(),
				"frame",
				ObjectWatchPanel.this,
				theRefEvent));

	}
	
	public void openWatch(WatchSeed aSeed)
	{
		itsBrowserNavigator.open(aSeed);
	}
	
	/**
	 * Shows the watch data obtained from the specified provider.
	 */
	public <E> void showWatch(AbstractWatchProvider aProvider)
	{
		itsProvider = aProvider;
		getJobScheduler().cancelAll();
				
		JPanel theEntriesPanel = new ScrollablePanel(GUIUtils.createStackLayout());
		theEntriesPanel.setOpaque(false);
		
		theEntriesPanel.add(aProvider.buildTitleComponent(getJobScheduler()));
		
		ObjectId theCurrentObject = aProvider.getCurrentObject();
		if (theCurrentObject != null)
		{
			theEntriesPanel.add(buildCurrentObjectLine(theCurrentObject));
		}
				
		theEntriesPanel.add(new AsyncPanel(getJobScheduler(), JobPriority.AUTO)
		{
			@Override
			protected void runJob()
			{
				itsEntries = itsProvider.getEntries();
			}

			@Override
			protected void updateSuccess()
			{
				setLayout(GUIUtils.createStackLayout());
				if (itsEntries != null) for (Entry theEntry : itsEntries)
				{
					if ("this".equals(theEntry.getName())) continue;
					
					add(new WatchEntryNode(
							getGUIManager(),
							getJobScheduler(),
							ObjectWatchPanel.this,
							itsProvider,
							theEntry));
				}
			}
		});

		itsScrollPane.setViewportView(theEntriesPanel);
	}
	
	private JComponent buildCurrentObjectLine(ObjectId aCurrentObject)
	{
		JPanel theContainer = new JPanel(GUIUtils.createSequenceLayout());
		theContainer.setOpaque(false);
		
		theContainer.add(GUIUtils.createLabel("this = "));
		
		theContainer.add(Hyperlinks.object(
				getGUIManager(), 
				Hyperlinks.SWING, 
				getJobScheduler(),
				aCurrentObject,
				itsProvider.getRefEvent(),
				true));
		
		return theContainer;		
		
	}
	
	private class WatchBrowserNavigator extends BrowserNavigator<WatchSeed>
	{
		public WatchBrowserNavigator(IJobScheduler aJobScheduler)
		{
			super(aJobScheduler);
		}

		@Override
		public void open(WatchSeed aSeed)
		{
			super.open(aSeed);
		}
		
		@Override
		protected void setSeed(WatchSeed aSeed)
		{
			super.setSeed(aSeed);
			showWatch(aSeed.createProvider());
		}
		
	}

}
