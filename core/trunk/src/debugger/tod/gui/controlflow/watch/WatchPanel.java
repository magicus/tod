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

import static tod.gui.FontConfig.STD_FONT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.BrowserNavigator;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import tod.gui.JobProcessor;
import tod.gui.Hyperlinks.ISeedFactory;
import tod.gui.controlflow.CFlowView;
import tod.gui.seed.LogViewSeedFactory;
import tod.gui.seed.Seed;
import zz.utils.SimpleAction;
import zz.utils.ui.GridStackLayout;
import zz.utils.ui.ZLabel;

/**
 * A panel that shows the contents of a stack frame or of an object.
 * @author gpothier
 */
public class WatchPanel extends JPanel
{
	private CFlowView itsView;
	private MySeedFactory itsSeedFactory = new MySeedFactory();
	private WatchBrowserNavigator itsBrowserNavigator;
	private JobProcessor itsJobProcessor;
	private JScrollPane itsScrollPane;
	
	public WatchPanel(CFlowView aView)
	{
		itsView = aView;
		itsJobProcessor = new JobProcessor(getGUIManager().getJobProcessor());
		itsBrowserNavigator = new WatchBrowserNavigator();
		createUI();
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
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsJobProcessor.detach();
		itsJobProcessor = null;
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
				WatchPanel.this,
				itsView.getLogBrowser(),
				theRefEvent));

	}
	
	public LogViewSeedFactory getLogViewSeedFactory()
	{
		return itsView.getLogViewSeedFactory();
	}
	
	public ISeedFactory getWatchSeedFactory()
	{
		return itsSeedFactory;
	}
	
	/**
	 * Shows the watch data obtained from the specified provider.
	 */
	public <E> void showWatch(final IWatchProvider<E> aProvider)
	{
		getJobProcessor().cancelAll();
				
		final JPanel theContainer = new JPanel(new GridStackLayout(1, 0, 0, true, false));
		theContainer.setOpaque(false);
		
		theContainer.add(aProvider.buildTitle(getJobProcessor()));
		
		ObjectId theCurrentObject = aProvider.getCurrentObject();
		if (theCurrentObject != null)
		{
			theContainer.add(buildCurrentObjectLine(theCurrentObject));
		}
				
		getJobProcessor().submit(new JobProcessor.Job<Object>()
		{
			@Override
			public Object run()
			{
				List<E> theEntries = aProvider.getEntries();
				if (theEntries == null) return null;
				
				for (E theEntry : theEntries)
				{
					if ("this".equals(aProvider.getEntryName(theEntry))) continue;
					
					theContainer.add(new WatchEntryNode<E>(
							itsSeedFactory,
							itsView.getLogBrowser(),
							getJobProcessor(),
							aProvider,
							theEntry));
				}
				
				theContainer.revalidate();
				theContainer.repaint();
				return null;
			}
		});
		
		itsScrollPane.setViewportView(theContainer);
	}
	
	private JComponent buildCurrentObjectLine(ObjectId aCurrentObject)
	{
		JPanel theContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		theContainer.setOpaque(false);
		
		theContainer.add(ZLabel.create("this = ", STD_FONT, Color.BLACK));
		
		theContainer.add(Hyperlinks.object(
				itsSeedFactory,
				itsView.getLogBrowser(), 
				getJobProcessor(),
				null,
				aCurrentObject,
				STD_FONT));
		
		return theContainer;		
		
	}
	
	private class MySeedFactory implements ISeedFactory
	{
		public Seed behaviorSeed(IBehaviorInfo aBehavior)
		{
			return getLogViewSeedFactory().behaviorSeed(aBehavior);
		}

		public Seed cflowSeed(final ILogEvent aEvent)
		{
			return new Seed()
			{
				@Override
				public void open()
				{
					itsView.getSeed().pSelectedEvent().set(aEvent);
				}
			};
		}

		public Seed objectSeed(final ObjectId aObjectId)
		{
			return new ObjectWatchSeed(
					WatchPanel.this,
					itsView.getLogBrowser(),
					itsView.getSeed().pSelectedEvent().get(),
					aObjectId);
		}

		public Seed typeSeed(ITypeInfo aType)
		{
			return getLogViewSeedFactory().typeSeed(aType);
		}
		
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
