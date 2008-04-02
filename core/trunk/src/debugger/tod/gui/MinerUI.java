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
package tod.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tod.core.DebugFlags;
import tod.core.config.TODConfig;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.ObjectId;
import tod.core.session.ISession;
import tod.core.session.ISessionMonitor;
import tod.gui.kit.Bus;
import tod.gui.kit.BusOwnerPanel;
import tod.gui.kit.IBusListener;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.kit.messages.ShowObjectHistoryMsg;
import tod.gui.kit.messages.EventSelectedMsg.SM_ShowNextForLine;
import tod.gui.kit.messages.EventSelectedMsg.SM_ShowPreviousForLine;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.DynamicCrosscuttingSeed;
import tod.gui.seed.FilterSeed;
import tod.gui.seed.FormattersSeed;
import tod.gui.seed.LogViewSeed;
import tod.gui.seed.LogViewSeedFactory;
import tod.gui.seed.ObjectHistorySeed;
import tod.gui.seed.StringSearchSeed;
import tod.gui.seed.StructureSeed;
import tod.gui.seed.ThreadsSeed;
import tod.gui.settings.GUISettings;
import tod.gui.view.IEventListView;
import tod.gui.view.LogView;
import tod.gui.view.controlflow.CFlowView;
import tod.utils.TODUtils;
import zz.utils.SimpleAction;
import zz.utils.ui.StackLayout;
import zz.utils.ui.UniversalRenderer;

/**
 * Main GUI window.
 * @author gpothier
 */
public abstract class MinerUI extends BusOwnerPanel
implements ILocationSelectionListener, IGUIManager
{
	static
	{
		try
		{
//			UIManager.setLookAndFeel("org.jdesktop.swingx.plaf.nimbus.NimbusLookAndFeel");
		}
		catch (Exception e)
		{
			System.out.println("Could not set L&F ("+e.getMessage()+")");
		}
	}
	
	
	private LogViewBrowserNavigator itsNavigator = new LogViewBrowserNavigator()
	{
		@Override
		protected void viewChanged(LogView aTheView)
		{
			MinerUI.this.viewChanged(aTheView);
		}
	};
	
	private IBusListener<ShowObjectHistoryMsg> itsShowObjectHistoryListener = new IBusListener<ShowObjectHistoryMsg>()
	{
		public boolean processMessage(ShowObjectHistoryMsg aMessage)
		{
			ObjectId theObject = aMessage.getObjectId();
			ObjectHistorySeed theSeed = new ObjectHistorySeed(
					MinerUI.this, 
					getLogBrowser(), 
					theObject);
			
			openSeed(theSeed, false);
			return true;
		}
	};
	
	private IBusListener<ShowCFlowMsg> itsShowCFlowListener = new IBusListener<ShowCFlowMsg>()
	{
		public boolean processMessage(ShowCFlowMsg aMessage)
		{
			CFlowSeed theSeed = new CFlowSeed(
					MinerUI.this, 
					getLogBrowser(),
					aMessage.getEvent());
			
			openSeed(theSeed, false);
			return true;
		}
	};

	private JobProcessor itsJobProcessor = new JobProcessor();
	private BookmarkPanel itsBookmarkPanel = new BookmarkPanel();
	
	private GUISettings itsGUISettings = new GUISettings(this);
	
	/**
	 * The currently used debugging session.
	 */
	private ISession itsSession;

	private Action itsStringSearchAction;
	
	/**
	 * We keep a list of all actions so that we can enable/disable all of them. 
	 */
	private List<Action> itsActions = new ArrayList<Action>();

	private SessionMonitorUpdater itsSchedulerMonitor;

	private ActionToolbar itsToolbar;

	private ActionCombo itsActionCombo;
	
	public MinerUI()
	{
		createUI();
	}
	
	protected ILogBrowser getLogBrowser()
	{
		return getSession().getLogBrowser();
	}

	public JobProcessor getJobProcessor()
	{
		return itsJobProcessor;
	}
	

	private void createUI()
	{
		setLayout(new BorderLayout());
		
		JPanel theCenterPanel = new JPanel (new BorderLayout());
		
		JPanel theNavButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		
		itsSchedulerMonitor = new SessionMonitorUpdater();
		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			theNavButtonsPanel.add(itsSchedulerMonitor);
		}
		
		theNavButtonsPanel.add (new JButton (itsNavigator.getBackwardAction()));
		theNavButtonsPanel.add (new JButton (itsNavigator.getForwardAction()));
		
		theCenterPanel.add (itsNavigator.getViewContainer(), BorderLayout.CENTER);
		theCenterPanel.add (theNavButtonsPanel, BorderLayout.NORTH);
		
		add (theCenterPanel, BorderLayout.CENTER);
		theNavButtonsPanel.add (createToolbar());
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		Bus.get(this).subscribe(ShowObjectHistoryMsg.ID, itsShowObjectHistoryListener);
		Bus.get(this).subscribe(ShowCFlowMsg.ID, itsShowCFlowListener);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		Bus.get(this).unsubscribe(ShowObjectHistoryMsg.ID, itsShowObjectHistoryListener);
		Bus.get(this).unsubscribe(ShowCFlowMsg.ID, itsShowCFlowListener);
		itsGUISettings.save();
	}
	
	public GUISettings getSettings()
	{
		return itsGUISettings;
	}
	
	protected void viewChanged(LogView aView)
	{
		itsBookmarkPanel.setView(aView);
		itsGUISettings.save();
	}
		
	private JComponent createToolbar()
	{
		itsToolbar = new ActionToolbar();
		itsActionCombo = new ActionCombo();
		
		createActions(itsToolbar, itsActionCombo);
		
		itsToolbar.add(itsActionCombo);
		return itsToolbar;
	}

	/**
	 * Creates all the actions available to the user in the toolbar
	 * @param aToolbar The main toolbar to which actions should be added
	 * @param aActionCombo An action combo for extra actions
	 */
	protected void createActions(ActionToolbar aToolbar, ActionCombo aActionCombo)
	{
		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			aToolbar.add(itsBookmarkPanel);
		}

		// Add a button that permits to jump to the threads view.
		aToolbar.add(new MyAction(
				"Threads",
				"<html>" +
				"<b>Threads view.</b> This view presents an overview <br>" +
				"of the activity of all the threads in the captured <br>" +
				"execution trace.")
		{
			public void actionPerformed(ActionEvent aE)
			{
				showThreads();
			}
			
		});

		// Add a button that permits to jump to the exceptions view.
		aToolbar.add(new MyAction(
				"Exceptions",
				"<html>" +
				"<b>Exceptions view.</b> This view shows a list <br>" +
				"of all the exceptions that occurred during the execution <br>" +
				"of the program. Note that many of the exceptions shown <br>" +
				"here are catched during the normal operation of the <br>" +
				"program and therefore do not appear in the console.")
		{
			public void actionPerformed(ActionEvent aE)
			{
				ILogBrowser theLogBrowser = getSession().getLogBrowser();
				
				FilterSeed theSeed = new FilterSeed(
						MinerUI.this,
						theLogBrowser,
						"All exceptions",
						theLogBrowser.createExceptionGeneratedFilter());
				
				openSeed(theSeed, false);			
			}
		});
		
		// the string search action should not be registered as other actions
		itsStringSearchAction = new SimpleAction(
				"Search string",
				"<html>" +
				"<b>Search in strings.</b> Search text in recorded strings. <br>" +
				"The <em>index strings</em> options must be enabled in the current <br>" +
				"session for this option to be available")
		{
			public void actionPerformed(ActionEvent aE)
			{
				ILogBrowser theLogBrowser = getSession().getLogBrowser();
				
				StringSearchSeed theSeed = new StringSearchSeed(
						MinerUI.this,
						theLogBrowser);
				
				openSeed(theSeed, false);			
			}
		};
		
//		aToolbar.add(itsStringSearchAction);
		
		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			aActionCombo.add(new MyAction(
					"(all events)",
					"<html>" +
					"<b>Show all events.</b> Shows a list of all the events <br>" +
					"that were recorded. This is used for debugging TOD itself.")
			{
				public void actionPerformed(ActionEvent aE)
				{
					ILogBrowser theLogBrowser = getSession().getLogBrowser();
					
					FilterSeed theSeed = new FilterSeed(
							MinerUI.this, 
							theLogBrowser,
							"All events",
							null);
					
					openSeed(theSeed, false);			
				}
			});
		}
		
		// Add a button that shows the structure view
		aToolbar.add(new MyAction(
				"View classes",
				"<html>" +
				"<b>Structure view.</b> This view presents the structure <br>" +
				"database.")
		{
			public void actionPerformed(ActionEvent aE)
			{
				ILogBrowser theLogBrowser = getSession().getLogBrowser();
				StructureSeed theSeed = new StructureSeed(MinerUI.this, theLogBrowser);
				openSeed(theSeed, false);			
			}
			
		});

		// Show formatters action
		aActionCombo.add(new MyAction(
				"Edit formatters",
				"<html>" +
				"<b>Edit formatters.</b> Permits to create and manage custom object formatters <br>" +
				"database.")
		{
			public void actionPerformed(ActionEvent aE)
			{
				ILogBrowser theLogBrowser = getSession().getLogBrowser();
				FormattersSeed theSeed = new FormattersSeed(MinerUI.this, theLogBrowser);
				openSeed(theSeed, false);			
			}
			
		});
		
		// Show formatters action
		aActionCombo.add(new MyAction(
				"Dynamic crosscutting",
				"<html>" +
				"<b>Dynamic crosscutting.</b> Shows the crosscutting of aspects/advices with base code.")
		{
			public void actionPerformed(ActionEvent aE)
			{
				ILogBrowser theLogBrowser = getSession().getLogBrowser();
				LogViewSeed theSeed = new DynamicCrosscuttingSeed(MinerUI.this, theLogBrowser);
				openSeed(theSeed, false);			
			}
		});
		

		
		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			// Adds a button that permits to flush buffers
			aActionCombo.add(new MyAction(
					"Flush",
					"<html>" +
					"<b>Flush buffered events.</b> Ensures that all buffered <br>" +
					"events are sent to the database. Use this to start <br>" +
					"debugging before the program terminates.")
			{
				public void actionPerformed(ActionEvent aE)
				{
					getSession().flush();
				}
			});
		}
	}
	
	/**
	 * Register an action to be automatically enabled/disabled when a session
	 * is available/unavailable.
	 */
	protected void registerAction(Action aAction)
	{
		itsActions.add(aAction);
	}
	
	protected void setSession(ISession aSession)
	{
		itsSession = aSession;
		itsNavigator.clear();
		itsGUISettings.getIntimacySettings().clear();
		showThreads();
		
		if (itsSession == null)
		{
			itsStringSearchAction.setEnabled(false);
			itsActionCombo.setEnabled(false);
			for (Action theAction : itsActions) theAction.setEnabled(false);
			
			itsSchedulerMonitor.setScheduler(null);
		}
		else
		{
			itsStringSearchAction.setEnabled(itsSession.getConfig().get(TODConfig.INDEX_STRINGS));
			itsActionCombo.setEnabled(true);
			for (Action theAction : itsActions) theAction.setEnabled(true);
			
			itsSchedulerMonitor.setScheduler(itsSession.getMonitor());
		}
	}
	
	public ISession getSession()
	{
		return itsSession;
	}
	
	protected void showThreads()
	{
		if (itsSession != null) openSeed(new ThreadsSeed(this, itsSession.getLogBrowser()), false);
		else openSeed(null, false);
	}


	public void selectionChanged(List/*<LocationInfo>*/ aSelectedLocations)
	{
		LogViewSeed theSeed = null;
		if (aSelectedLocations.size() == 1)
		{
			ILocationInfo theInfo = (ILocationInfo) aSelectedLocations.get(0);
			theSeed = LogViewSeedFactory.getDefaultSeed(this, getSession().getLogBrowser(), theInfo);
		}

		openSeed(theSeed, false);
	}
	
	public void openSeed(LogViewSeed aSeed, boolean aNewTab)
	{
		getJobProcessor().cancelAll();
		itsNavigator.open(aSeed);
	}

	/**
	 * Shows a list of all the events that occurred at the specified line.
	 */
	public void showEventsForLine(IBehaviorInfo aBehavior, int aLine, IEventFilter aFilter)
	{
		ILogBrowser theLogBrowser = getLogBrowser();
		
		IEventFilter theFilter = TODUtils.getLocationFilter(
				theLogBrowser, 
				aBehavior, 
				aLine);
	
		if (theFilter != null)
		{
			if (aFilter != null)
			{
				theFilter = theLogBrowser.createIntersectionFilter(theFilter, aFilter);
			}
			TODUtils.logf(0,"Trying to show events for filter %s",theFilter.getClass());
			LogViewSeed theSeed = new FilterSeed(
					this, 
					theLogBrowser, 
					"Events on line "+aLine+" of "+aBehavior.getName(),
					theFilter);
			
			openSeed(theSeed, false);				
		} else TODUtils.logf(0,"No filter created for line %s",aLine);
	}
	
	/**
	 * Returns the current view if it is a {@link CFlowView}, or null otherwise.
	 * @return
	 */
	private IEventListView getEventListView()
	{
		LogViewSeed theCurrentSeed = itsNavigator.getCurrentSeed();
		if (theCurrentSeed == null) return null;
		
		LogView theCurrentView = theCurrentSeed.getComponent();
		if (theCurrentView instanceof IEventListView)
		{
			IEventListView theView = (IEventListView) theCurrentView;
			return theView;
		}
		else return null;
	}
	
	/**
	 * Creates an event browser over all the event that are accessible
	 * to a base browser and that additionally occured at the specified
	 * location.
	 */
	private IEventBrowser createLocationBrowser(
			IEventBrowser aBaseBrowser,
			IBehaviorInfo aBehavior, 
			int aLine)
	{
		IEventFilter theFilter = TODUtils.getLocationFilter(
				aBaseBrowser.getLogBrowser(), 
				aBehavior, 
				aLine);
		
		return aBaseBrowser.createIntersection(theFilter);
	}
	
	public void showNextEventForLine(IBehaviorInfo aBehavior, int aLine)
	{
		IEventListView theView = getEventListView();
		if (theView == null) return;

		IEventBrowser theBrowser = createLocationBrowser(
				theView.getEventBrowser(),
				aBehavior, 
				aLine);
		
		ILogEvent theSelectedEvent = theView.getSelectedEvent();
		if (theSelectedEvent != null) theBrowser.setPreviousEvent(theSelectedEvent);
		
		if (theBrowser.hasNext())
		{
			ILogEvent theEvent = theBrowser.next();
			theView.selectEvent(theEvent, new SM_ShowNextForLine(aBehavior, aLine));
		}
	}
	
	public void showPreviousEventForLine(IBehaviorInfo aBehavior, int aLine)
	{
		IEventListView theView = getEventListView();
		if (theView == null) return;

		IEventBrowser theBrowser = createLocationBrowser(
				theView.getEventBrowser(),
				aBehavior, 
				aLine);
		
		ILogEvent theSelectedEvent = theView.getSelectedEvent();
		if (theSelectedEvent != null) theBrowser.setNextEvent(theSelectedEvent);
		
		if (theBrowser.hasPrevious())
		{
			ILogEvent theEvent = theBrowser.previous();
			theView.selectEvent(theEvent, new SM_ShowPreviousForLine(aBehavior, aLine));
		}
	}
	
	/**
	 * Whether the "Show next event for line" action should be enabled.
	 */
	public boolean canShowNextEventForLine()
	{
		return getEventListView() != null;
	}

	/**
	 * Whether the "Show previous event for line" action should be enabled.
	 */
	public boolean canShowPreviousEventForLine()
	{
		return getEventListView() != null;
	}
	
	/**
	 * A monitor component for a scheduler, that gives the user an indication
	 * about the load of the underlying log browser.
	 * @author gpothier
	 */
	private static class SessionMonitorUpdater extends JPanel
	implements Runnable
	{
		private ISessionMonitor itsMonitor;
		private Thread itsThread;
		private JLabel itsLabel;

		public SessionMonitorUpdater()
		{
			itsThread = new Thread(this);
			createUI();
		}
		
		private void createUI()
		{
			itsLabel = new JLabel("mon.");
			setLayout(new StackLayout());
			add(itsLabel);
		}
		
		public void setScheduler(ISessionMonitor aScheduler)
		{
			itsMonitor = aScheduler;
		}
		
		@Override
		public void addNotify()
		{
			super.addNotify();
			itsThread.start();
		}
		
		@Override
		public void removeNotify()
		{
			super.removeNotify();
			itsThread.interrupt();
		}
		
		public void run()
		{
			while(true)
			{
				if (itsMonitor == null)
				{
					itsLabel.setText("mon.");
				}
				else
				{
					int theQueueSize = itsMonitor.getQueueSize();
					itsLabel.setText(""+theQueueSize);
				}
				
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
					break;
				}
			}
		}	
	}

	/**
	 * A kind of combo box that permits to access additional actions.
	 * @author gpothier
	 */
	protected static class ActionCombo extends JPanel implements ActionListener
	{
		private JComboBox itsComboBox;
		private DefaultComboBoxModel itsModel;
		private Action itsTitleAction;

		public ActionCombo()
		{
			createUI();
		}

		private void createUI()
		{
			itsModel = new DefaultComboBoxModel();
			
			itsTitleAction = new SimpleAction("More...")
			{
				public void actionPerformed(ActionEvent aE)
				{
				}
			};
			
			itsModel.addElement(itsTitleAction);
			
			itsComboBox = new JComboBox(itsModel);
			itsComboBox.addActionListener(this);
			
			itsComboBox.setRenderer(new UniversalRenderer<Action>()
					{
						@Override
						protected String getName(Action aAction)
						{
							return (String) aAction.getValue(Action.NAME);
						}
					});
			
			setLayout(new StackLayout());
			add(itsComboBox);
		}
		
		public void add(Action aAction)
		{
			itsModel.addElement(aAction);
		}
		
		@Override
		public void setEnabled(boolean aEnabled)
		{
			itsComboBox.setEnabled(aEnabled);
		}

		public void actionPerformed(ActionEvent aE)
		{
			Action theAction = (Action) itsModel.getSelectedItem();
			theAction.actionPerformed(aE);
			itsModel.setSelectedItem(itsTitleAction);
		}
	}
	
	protected static class ActionToolbar extends JPanel
	{
		public ActionToolbar()
		{
			super(new FlowLayout(FlowLayout.LEFT));
		}
		
		public void add(Action aAction)
		{
			add(new JButton(aAction));
		}
	}
	
	/**
	 * A custom subclass of {@link SimpleAction} that registers itself to the UI.
	 * @author gpothier
	 */
	protected abstract class MyAction extends SimpleAction
	{
		public MyAction(Icon aIcon, String aDescription)
		{
			super(aIcon, aDescription);
			registerAction(this);
		}

		public MyAction(String aTitle, Icon aIcon, String aDescription)
		{
			super(aTitle, aIcon, aDescription);
			registerAction(this);
		}

		public MyAction(String aTitle, String aDescription)
		{
			super(aTitle, aDescription);
			registerAction(this);
		}

		public MyAction(String aTitle)
		{
			super(aTitle);
			registerAction(this);
		}
	}
}
