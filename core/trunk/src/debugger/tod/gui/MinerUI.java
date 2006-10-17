/*
 * Created on Nov 3, 2004
 */
package tod.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.ILocationInfo;
import tod.core.session.ISession;
import tod.gui.seed.Seed;
import tod.gui.seed.SeedFactory;
import tod.gui.seed.ThreadsSeed;

/**
 * @author gpothier
 */
public abstract class MinerUI extends JPanel 
implements ILocationSelectionListener, IGUIManager
{
	private BrowserNavigator itsNavigator = new BrowserNavigator();
	
	public MinerUI()
	{
		createUI();
	}
	
	protected ILogBrowser getBrowser()
	{
		return getSession().getLogBrowser();
	}

	private void createUI()
	{
		setLayout(new BorderLayout());
		
		JPanel theCenterPanel = new JPanel (new BorderLayout());
		
		JPanel theNavButtonsPanel = new JPanel();
		
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
		Seed theSeed = null;
		if (aSelectedLocations.size() == 1)
		{
			ILocationInfo theInfo = (ILocationInfo) aSelectedLocations.get(0);
			theSeed = SeedFactory.getDefaultSeed(this, getSession().getLogBrowser(), theInfo);
		}

		itsNavigator.open(theSeed);
	}
	
	public void openSeed(Seed aSeed, boolean aNewTab)
	{
		itsNavigator.open(aSeed);
	}


}
