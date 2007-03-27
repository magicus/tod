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
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.session.ISession;
import tod.gui.controlflow.CFlowView;
import tod.gui.kit.Bus;
import tod.gui.kit.BusOwnerPanel;
import tod.gui.kit.IBusListener;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.kit.messages.ShowObjectHistoryMsg;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.FilterSeed;
import tod.gui.seed.LogViewSeed;
import tod.gui.seed.LogViewSeedFactory;
import tod.gui.seed.Seed;
import tod.gui.seed.StringSearchSeed;
import tod.gui.seed.ThreadsSeed;
import tod.gui.view.IEventListView;
import tod.gui.view.LogView;
import tod.utils.TODUtils;

/**
 * @author gpothier
 */
public abstract class MinerUI extends BusOwnerPanel
implements ILocationSelectionListener, IGUIManager
{
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
			FilterSeed theSeed = new FilterSeed(
					MinerUI.this, 
					getLogBrowser(), 
					getLogBrowser().createObjectFilter(aMessage.getObjectId()));
			
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
	
	private Properties itsProperties = new Properties();

	
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
		
		JPanel theNavButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
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
		Bus.getBus(this).subscribe(ShowObjectHistoryMsg.ID, itsShowObjectHistoryListener);
		Bus.getBus(this).subscribe(ShowCFlowMsg.ID, itsShowCFlowListener);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		Bus.getBus(this).unsubscribe(ShowObjectHistoryMsg.ID, itsShowObjectHistoryListener);
		Bus.getBus(this).unsubscribe(ShowCFlowMsg.ID, itsShowCFlowListener);
	}

	protected void viewChanged(LogView aView)
	{
		itsBookmarkPanel.setView(aView);
		saveProperties(itsProperties);
	}
	
	protected abstract ISession getSession();
	
	protected JComponent createToolbar()
	{
		JPanel theToolbar = new JPanel();
		
		theToolbar.add(itsBookmarkPanel);

		// Add a button that permits to jump to the threads view.
		JButton theThreadsViewButton = new JButton("View threads");
		theThreadsViewButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent aE)
					{
						reset();
					}
				});
		theThreadsViewButton.setToolTipText(
				"<html>" +
				"<b>Threads view.</b> This view presents an overview <br>" +
				"of the activity of all the threads in the captured <br>" +
				"execution trace.");
		
		theToolbar.add(theThreadsViewButton);

		// Add a button that permits to jump to the exceptions view.
		JButton theExceptionsViewButton = new JButton("View exceptions");
		theExceptionsViewButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				ILogBrowser theLogBrowser = getSession().getLogBrowser();
				
				FilterSeed theSeed = new FilterSeed(
						MinerUI.this,
						theLogBrowser,
						theLogBrowser.createExceptionGeneratedFilter());
				
				openSeed(theSeed, false);			
			}
		});
		theExceptionsViewButton.setToolTipText(
				"<html>" +
				"<b>Exceptions view.</b> This view shows a list <br>" +
				"of all the exceptions that occurred during the execution <br>" +
				"of the program. Note that many of the exceptions shown <br>" +
				"here are catched during the normal operation of the <br>" +
				"program and therefore do not appear in the console.");
		
		theToolbar.add(theExceptionsViewButton);
		
		// String search button
		JButton theStringSearchButton = new JButton("Search string");
		theStringSearchButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				ILogBrowser theLogBrowser = getSession().getLogBrowser();
				
				StringSearchSeed theSeed = new StringSearchSeed(
						MinerUI.this,
						theLogBrowser);
				
				openSeed(theSeed, false);			
			}
		});
		theStringSearchButton.setToolTipText(
				"<html>" +
				"<b>Search in strings.</b> Search text in recorded strings.");
		
		theToolbar.add(theStringSearchButton);
		
		JButton theShowAllEventsButton = new JButton("(all events)");
		theShowAllEventsButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				ILogBrowser theLogBrowser = getSession().getLogBrowser();
				
				FilterSeed theSeed = new FilterSeed(MinerUI.this, theLogBrowser, null);
				
				openSeed(theSeed, false);			
			}
		});
		
		theShowAllEventsButton.setToolTipText(
				"<html>" +
				"<b>Show all events.</b> Shows a list of all the events <br>" +
				"that were recorded. This is used for debugging TOD itself.");
		
		theToolbar.add(theShowAllEventsButton);
		
		// Adds a button that permits to flush buffers
		JButton theFlushButton = new JButton("Flush");
		theFlushButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent aE)
					{
						getSession().flush();
					}
				});
		
		theFlushButton.setToolTipText(
				"<html>" +
				"<b>Flush buffered events.</b> Ensures that all buffered <br>" +
				"events are sent to the database. Use this to start <br>" +
				"debugging before the program terminates.");
		
		theToolbar.add(theFlushButton);
		
		return theToolbar;
	}
	
	protected void reset()
	{
		ISession theSession = getSession();
		if (theSession != null) openSeed(new ThreadsSeed(this, theSession.getLogBrowser()), false);
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
	public void showEventsForLine(IBehaviorInfo aBehavior, int aLine)
	{
		IEventFilter theFilter = TODUtils.getLocationFilter(
				getLogBrowser(), 
				aBehavior, 
				aLine);
		
		if (theFilter != null)
		{
			LogViewSeed theSeed = new FilterSeed(this, getLogBrowser(), theFilter);
			openSeed(theSeed, false);				
		}
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
			theView.selectEvent(theEvent);
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
			theView.selectEvent(theEvent);
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
	protected void loadProperties(Properties aProperties)
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
	protected void saveProperties(Properties aProperties)
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
	public static boolean getBooleanProperty (
			IGUIManager aGUIManager, 
			String aPropertyName, 
			boolean aDefault)
	{
		String theString = aGUIManager.getProperty(aPropertyName);
		return theString != null ? Boolean.parseBoolean(theString) : aDefault;
	}
	
	/**
	 * Utility method for {@link #getProperty(String)}
	 */
	public static int getIntProperty (
			IGUIManager aGUIManager, 
			String aPropertyName, 
			int aDefault)
	{
		String theString = aGUIManager.getProperty(aPropertyName);
		return theString != null ? Integer.parseInt(theString) : aDefault;
	}
	
	/**
	 * Utility method for {@link #getProperty(String)}
	 */
	public static String getStringProperty (
			IGUIManager aGUIManager, 
			String aPropertyName, 
			String aDefault)
	{
		String theString = aGUIManager.getProperty(aPropertyName);
		return theString != null ? theString : aDefault;
	}
	
}
