/*
 * Created on Nov 3, 2004
 */
package reflex.lib.logging.miner.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import reflex.lib.logging.miner.gui.seed.Seed;
import reflex.lib.logging.miner.gui.seed.SeedFactory;
import reflex.lib.logging.miner.gui.seed.ThreadsSeed;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.LocationInfo;
import tod.core.model.trace.IEventTrace;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * @author gpothier
 */
public class MinerUI extends JPanel 
implements ILocationSelectionListener, IGUIManager
{
//	private static Queries itsQueries;

	public static void main(String[] args) throws Exception
	{
		JFrame theFrame = new JFrame("lib.logging - miner");
		
//		ISQLBackend theBackend = new PostgreSQLBackend();

//		if (args != null && args.length > 0 && "-i".equals(args[0])) DatabaseInit.init(theBackend);
//		else
//		{
//			itsQueries = new Queries(theBackend);
//	
//	//		IBrowsableLog theBrowsableLog = LogMiner.createLocalLogServer(4012);
//			IBrowsableLog theBrowsableLog = LogMiner.createDBLogServer(theBackend, 4012);
//			
//			theFrame.setContentPane(new MinerUI(theBrowsableLog));
//			theFrame.pack();
//			theFrame.setVisible(true);
//		}
	}
	
	private IEventTrace itsCollector;
	private LocationSelector itsLocationSelector;
	
	private BrowserNavigator itsNavigator = new BrowserNavigator();
	
	public MinerUI(IEventTrace aCollector)
	{
		itsCollector = aCollector;
		createUI();
	}

	private void createUI()
	{
		setLayout(new BorderLayout());
		itsLocationSelector = new LocationSelector(itsCollector.getLocationTrace());
		itsLocationSelector.addSelectionListener(this);
		add (itsLocationSelector, BorderLayout.WEST);
		
		JPanel theCenterPanel = new JPanel (new BorderLayout());
		
		JPanel theNavButtonsPanel = new JPanel();
		
		theNavButtonsPanel.add (new JButton (itsNavigator.getBackwardAction()));
		theNavButtonsPanel.add (new JButton (itsNavigator.getForwardAction()));
		
		theCenterPanel.add (itsNavigator.getViewContainer(), BorderLayout.CENTER);
		theCenterPanel.add (theNavButtonsPanel, BorderLayout.NORTH);
		
		add (theCenterPanel, BorderLayout.CENTER);
		add (createToolbar(), BorderLayout.NORTH);
	}

	private JComponent createToolbar()
	{
		JPanel theToolbar = new JPanel();
		JButton theClearDbButton = new JButton ("Clear db");
		theClearDbButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
//				try
//				{
//					itsQueries.dbInit.init();
//				}
//				catch (SQLException e1)
//				{
//					e1.printStackTrace();
//				}
			}
		});

		theToolbar.add (theClearDbButton);
		
		JButton theThreadsViewButton = new JButton("View threads");
		theThreadsViewButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent aE)
					{
						itsNavigator.open(new ThreadsSeed(MinerUI.this, itsCollector));
					}
				});
		
		theToolbar.add(theThreadsViewButton);

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
	
	
	public void openSeed(Seed aSeed, boolean aNewTab)
	{
		itsNavigator.open(aSeed);
	}

	public void gotoEvent(ILogEvent aEvent)
	{
	}
}
