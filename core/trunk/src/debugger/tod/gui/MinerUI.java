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
package tod.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

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
import tod.gui.kit.IOptionsOwner;
import tod.gui.kit.Options;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.kit.messages.ShowObjectHistoryMsg;
import tod.gui.kit.messages.EventSelectedMsg.SM_ShowNextForLine;
import tod.gui.kit.messages.EventSelectedMsg.SM_ShowPreviousForLine;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.FilterSeed;
import tod.gui.seed.LogViewSeed;
import tod.gui.seed.LogViewSeedFactory;
import tod.gui.seed.ObjectHistorySeed;
import tod.gui.seed.StringSearchSeed;
import tod.gui.seed.StructureSeed;
import tod.gui.seed.ThreadsSeed;
import tod.gui.view.IEventListView;
import tod.gui.view.LogView;
import tod.gui.view.controlflow.CFlowView;
import tod.utils.TODUtils;
import zz.utils.Base64;
import zz.utils.SimpleAction;
import zz.utils.ui.StackLayout;

/**
 * Main GUI window.
 * @author gpothier
 */
public abstract class MinerUI extends BusOwnerPanel
implements ILocationSelectionListener, IGUIManager, IOptionsOwner
{
	static
	{
		try
		{
			UIManager.setLookAndFeel("org.jdesktop.swingx.plaf.nimbus.NimbusLookAndFeel");
			System.out.println("Set Nimbus L&F");
		}
		catch (Exception e)
		{
			System.out.println("Could not set Nimbus L&F ("+e.getMessage()+")");
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
	
	private Options itsRootOptions = new Options(this, "root", null);
	
	private Properties itsProperties = new Properties();

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
	
	public MinerUI()
	{
		loadProperties(itsProperties);
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
	}

	public Options getOptions()
	{
		return itsRootOptions;
	}
	
	protected void viewChanged(LogView aView)
	{
		itsBookmarkPanel.setView(aView);
		saveProperties(itsProperties);
	}
		
	protected JComponent createToolbar()
	{
		JPanel theToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			theToolbar.add(itsBookmarkPanel);
		}

		// Add a button that permits to jump to the threads view.
		Action theThreadsViewAction = new SimpleAction(
				"View threads",
				"<html>" +
				"<b>Threads view.</b> This view presents an overview <br>" +
				"of the activity of all the threads in the captured <br>" +
				"execution trace.")
		{
			public void actionPerformed(ActionEvent aE)
			{
				showThreads();
			}
			
		};
		
		theToolbar.add(new JButton(theThreadsViewAction));
		registerAction(theThreadsViewAction);

		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			// Add a button that permits to jump to the exceptions view.
			Action theExceptionsViewAction = new SimpleAction(
					"View exceptions",
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
			};
			
			theToolbar.add(new JButton(theExceptionsViewAction));
			registerAction(theExceptionsViewAction);
		}
		
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
		
		theToolbar.add(new JButton(itsStringSearchAction));
		
		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			Action theShowAllEventsAction = new SimpleAction(
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
			};
			
			theToolbar.add(new JButton(theShowAllEventsAction));
			registerAction(theShowAllEventsAction);
		}
		
		// Add a button that shows the structure view
		Action theStructureViewAction = new SimpleAction(
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
			
		};
		
		theToolbar.add(new JButton(theStructureViewAction));
		registerAction(theStructureViewAction);

		
		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			// Adds a button that permits to flush buffers
			Action theFlushAction = new SimpleAction(
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
			};
			
			theToolbar.add(new JButton(theFlushAction));
			registerAction(theFlushAction);
		}
		
		return theToolbar;
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
		showThreads();
		
		if (itsSession == null)
		{
			itsStringSearchAction.setEnabled(false);
			for (Action theAction : itsActions) theAction.setEnabled(false);
			
			itsSchedulerMonitor.setScheduler(null);
		}
		else
		{
			itsStringSearchAction.setEnabled(itsSession.getConfig().get(TODConfig.INDEX_STRINGS));
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
	 * Loads stored properties and place them in the given properties map.
	 */
	public static void loadProperties(Properties aProperties)
	{
		try
		{
			File theFile = new File("tod-properties.txt");
			if (theFile.exists()) aProperties.load(new FileInputStream(theFile));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the given properties map.
	 */
	public static void saveProperties(Properties aProperties)
	{
		try
		{
			File theFile = new File("tod-properties.txt");
			aProperties.store(new FileOutputStream(theFile), "TOD configuration");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String getProperty(String aKey)
	{
		return itsProperties.getProperty(aKey);
	}

	public void setProperty(String aKey, String aValue)
	{
		itsProperties.setProperty(aKey, aValue);
	}

	/**
	 * Utility method for {@link #getProperty(String)}
	 */
	public static boolean getBooleanProperty (IGUIManager aGUIManager, String aPropertyName, boolean aDefault)
	{
		String theString = aGUIManager.getProperty(aPropertyName);
		return theString != null ? Boolean.parseBoolean(theString) : aDefault;
	}
	
	/**
	 * Utility method for {@link #getProperty(String)}
	 */
	public static int getIntProperty (IGUIManager aGUIManager, String aPropertyName, int aDefault)
	{
		String theString = aGUIManager.getProperty(aPropertyName);
		return theString != null ? Integer.parseInt(theString) : aDefault;
	}
	
	/**
	 * Utility method for {@link #getProperty(String)}
	 */
	public static String getStringProperty (IGUIManager aGUIManager, String aPropertyName, String aDefault)
	{
		String theString = aGUIManager.getProperty(aPropertyName);
		return theString != null ? theString : aDefault;
	}
	
	/**
	 * Retrieves a serialized object.
	 */
	public static Object getObjectProperty(IGUIManager aManager, String aPropertyName, Object aDefault)
	{
		try
		{
			String theString = aManager.getProperty(aPropertyName);
			if (theString == null) return aDefault;
			
			byte[] theByteArray = Base64.decode(theString);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(theByteArray));
			return ois.readObject();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Saves a serialized object into a property.
	 */
	public static void setObjectProperty(IGUIManager aManager, String aPropertyName, Object aValue)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(aValue);
			oos.flush();
			
			byte[] theByteArray = baos.toByteArray();
			String theString = Base64.encodeBytes(theByteArray);
			
			aManager.setProperty(aPropertyName, theString);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
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

}
