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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.ILocationInfo;
import tod.core.session.ISession;
import tod.gui.seed.FilterSeed;
import tod.gui.seed.LogViewSeed;
import tod.gui.seed.LogViewSeedFactory;
import tod.gui.seed.ThreadsSeed;

/**
 * @author gpothier
 */
public abstract class MinerUI extends JPanel 
implements ILocationSelectionListener, IGUIManager
{
	private LogViewBrowserNavigator itsNavigator = new LogViewBrowserNavigator();
	private JobProcessor itsJobProcessor = new JobProcessor();
	
	public MinerUI()
	{
		createUI();
	}
	
	protected ILogBrowser getBrowser()
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

	protected abstract ISession getSession();
	
	private JComponent createToolbar()
	{
		JPanel theToolbar = new JPanel();

		// Add a button that permits to jump to the threads view.
		JButton theThreadsViewButton = new JButton("View threads");
		theThreadsViewButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent aE)
					{
						reset();
					}
				});
		
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
		
		theToolbar.add(theExceptionsViewButton);
		
		JButton theShowAllEventsButton = new JButton("events");
		theShowAllEventsButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				ILogBrowser theLogBrowser = getSession().getLogBrowser();
				
				FilterSeed theSeed = new FilterSeed(
						MinerUI.this,
						theLogBrowser,
						theLogBrowser.createIntersectionFilter());
				
				openSeed(theSeed, false);			
			}
		});
		
		theToolbar.add(theShowAllEventsButton);
		
		// Adds a button that permits to disconnect the current session
		JButton theKillSessionButton = new JButton("Kill session");
		theKillSessionButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent aE)
					{
						getSession().disconnect();
					}
				});
		
		theToolbar.add(theKillSessionButton);
		
		return theToolbar;
	}
	
	protected void reset()
	{
		openSeed(new ThreadsSeed(this, getSession().getLogBrowser()), false);
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


}
