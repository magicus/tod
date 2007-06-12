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
package tod.gui.controlflow.watch;

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
import tod.gui.IGUIManager;
import tod.gui.JobProcessor;
import tod.gui.controlflow.CFlowView;
import tod.gui.controlflow.watch.AbstractWatchProvider.Entry;
import tod.gui.kit.AsyncPanel;
import tod.gui.kit.Bus;
import tod.gui.kit.IBusListener;
import tod.gui.kit.messages.ShowObjectMsg;
import zz.utils.SimpleAction;
import zz.utils.ui.ScrollablePanel;

/**
 * A panel that shows the contents of a stack frame or of an object.
 * @author gpothier
 */
public class WatchPanel extends JPanel
{
	private CFlowView itsView;
	private WatchBrowserNavigator itsBrowserNavigator;
	private JobProcessor itsJobProcessor;
	private JScrollPane itsScrollPane;
	private AbstractWatchProvider itsProvider;
	private List<Entry> itsEntries;
	
	private IBusListener<ShowObjectMsg> itsShowObjectListener = new IBusListener<ShowObjectMsg>()
	{
		public boolean processMessage(ShowObjectMsg aMessage)
		{
			openWatch(new ObjectWatchSeed(
					aMessage.getTitle(),
					WatchPanel.this, 
					getView().getLogBrowser(), 
					aMessage.getRefEvent(), 
					aMessage.getObjectId()));
			
			return true;
		}
	};
	
	public WatchPanel(CFlowView aView)
	{
		itsView = aView;
		itsJobProcessor = new JobProcessor(getGUIManager().getJobProcessor());
		itsBrowserNavigator = new WatchBrowserNavigator();
	}
	
	/**
	 * Whether package names should be displayed.
	 */
	protected boolean showPackageNames()
	{
		return getView().showPackageNames();
	}
	
	public CFlowView getView()
	{
		return itsView;
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
		
		showStackFrame();
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		if (itsJobProcessor == null) 
			itsJobProcessor = new JobProcessor(getGUIManager().getJobProcessor());
		
		Bus.getBus(this).subscribe(ShowObjectMsg.ID, itsShowObjectListener);
		
		createUI();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsJobProcessor.detach();
		itsJobProcessor = null;
		Bus.getBus(this).unsubscribe(ShowObjectMsg.ID, itsShowObjectListener);
	}
	
	public IGUIManager getGUIManager()
	{
		return itsView.getGUIManager();
	}
	
	/**
	 * Returns a job processor that only contains jobs for this watch
	 * panel.
	 */
	public JobProcessor getJobProcessor()
	{
		return itsJobProcessor;
	}

	public void showStackFrame()
	{
		ILogEvent theRefEvent = itsView.getSeed().pSelectedEvent().get();
		if (theRefEvent == null) return;
		
		itsBrowserNavigator.open(new StackFrameWatchSeed(
				"frame",
				WatchPanel.this,
				itsView.getLogBrowser(),
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
		getJobProcessor().cancelAll();
				
		JPanel theEntriesPanel = new ScrollablePanel(GUIUtils.createStackLayout());
		theEntriesPanel.setOpaque(false);
		
		theEntriesPanel.add(aProvider.buildTitleComponent(getJobProcessor()));
		
		ObjectId theCurrentObject = aProvider.getCurrentObject();
		if (theCurrentObject != null)
		{
			theEntriesPanel.add(buildCurrentObjectLine(theCurrentObject));
		}
				
		theEntriesPanel.add(new AsyncPanel(getJobProcessor())
		{
			@Override
			protected void runJob()
			{
				itsEntries = itsProvider.getEntries();
			}

			@Override
			protected void update()
			{
				setLayout(GUIUtils.createStackLayout());
				if (itsEntries != null) for (Entry theEntry : itsEntries)
				{
					if ("this".equals(theEntry.getName())) continue;
					
					add(new WatchEntryNode(
							itsView.getLogBrowser(),
							getJobProcessor(),
							WatchPanel.this,
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
				Hyperlinks.SWING, 
				itsView.getLogBrowser(), 
				getJobProcessor(),
				null,
				aCurrentObject,
				itsProvider.getRefEvent(),
				showPackageNames()));
		
		return theContainer;		
		
	}
	
	private class WatchBrowserNavigator extends BrowserNavigator<WatchSeed>
	{
		@Override
		protected void setSeed(WatchSeed aSeed)
		{
			super.setSeed(aSeed);
			showWatch(aSeed.createProvider());
		}
		
	}

}
