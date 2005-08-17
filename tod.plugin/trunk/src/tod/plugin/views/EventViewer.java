/*
 * Created on Aug 15, 2005
 */
package tod.plugin.views;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.eclipse.jdt.core.IJavaElement;

import reflex.lib.logging.core.api.collector.LocationInfo;
import reflex.lib.logging.miner.api.IBrowsableLog;
import reflex.lib.logging.miner.gui.BrowserNavigator;
import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.seed.Seed;
import reflex.lib.logging.miner.gui.seed.SeedFactory;
import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.backend.PostgreSQLBackend;
import tod.plugin.TODPlugin;
import tod.plugin.TODPluginUtils;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

public class EventViewer extends JPanel implements IGUIManager
{
	private IBrowsableLog itsCollector;
	private BrowserNavigator itsNavigator = new BrowserNavigator();
	private Queries itsQueries; 

	private IRWProperty<Long> pTimestamp = new SimpleRWProperty<Long>(this);

	public EventViewer(IBrowsableLog aCollector)
	{
		itsCollector = aCollector;
		
		try
		{
			itsQueries = new Queries(new PostgreSQLBackend());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		createUI();
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

	private JComponent createToolbar()
	{
		JPanel theToolbar = new JPanel();
		JButton theClearDbButton = new JButton ("Clear db");
		theClearDbButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					itsQueries.dbInit.init();
				}
				catch (SQLException e1)
				{
					e1.printStackTrace();
				}
			}
		});

		theToolbar.add (theClearDbButton);

		return theToolbar;
	}


	public void selectionChanged(List/*<LocationInfo>*/ aSelectedLocations)
	{
		Seed theSeed = null;
		if (aSelectedLocations.size() == 1)
		{
			LocationInfo theInfo = (LocationInfo) aSelectedLocations.get(0);
			theSeed = SeedFactory.getDefaultSeed(this, itsCollector, theInfo);
		}

		itsNavigator.open(theSeed);
	}
	
	public void showElement (IJavaElement aElement)
	{
		LocationInfo theLocationInfo = TODPluginUtils.getLocationInfo(TODPlugin.getDefault().getSession(), aElement);
		Seed theSeed = SeedFactory.getDefaultSeed(this, itsCollector, theLocationInfo);
		openSeed(theSeed, false);
	}
	
	public void openSeed(Seed aSeed, boolean aNewTab)
	{
		itsNavigator.open(aSeed);
	}
	
	
	public IRWProperty<Long> pTimestamp()
	{
		return pTimestamp;
	}

}
