/*
 * Created on Aug 15, 2005
 */
package tod.plugin.views;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.eclipse.jdt.core.IJavaElement;

import reflex.lib.logging.miner.gui.BrowserNavigator;
import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.seed.Seed;
import reflex.lib.logging.miner.gui.seed.SeedFactory;
import reflex.lib.logging.miner.gui.seed.ThreadsSeed;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.LocationInfo;
import tod.plugin.TODPluginUtils;
import tod.plugin.TODSessionManager;
import tod.session.ISession;
import zz.utils.properties.IProperty;
import zz.utils.properties.PropertyListener;

public class EventViewer extends JPanel implements IGUIManager
{
	private BrowserNavigator itsNavigator = new BrowserNavigator();
	private final TraceNavigatorView itsTraceNavigatorView;

	public EventViewer(TraceNavigatorView aTraceNavigatorView)
	{
		itsTraceNavigatorView = aTraceNavigatorView;
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
		
		TODSessionManager.getInstance().pCurrentSession().addHardListener(new PropertyListener<ISession>()
				{
					public void propertyChanged(IProperty<ISession> aProperty, ISession aOldValue, ISession aNewValue)
					{
						reset();
					}
				});
	}

	private ISession getSession()
	{
		return TODSessionManager.getInstance().pCurrentSession().get();
	}
	
	private JComponent createToolbar()
	{
		JPanel theToolbar = new JPanel();

		JButton theThreadsViewButton = new JButton("View threads");
		theThreadsViewButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent aE)
					{
						reset();
					}
				});
		
		theToolbar.add(theThreadsViewButton);

		return theToolbar;
	}
	
	private void reset()
	{
		openSeed(new ThreadsSeed(EventViewer.this, getSession().getEventTrace()), false);
	}


	public void selectionChanged(List/*<LocationInfo>*/ aSelectedLocations)
	{
		Seed theSeed = null;
		if (aSelectedLocations.size() == 1)
		{
			LocationInfo theInfo = (LocationInfo) aSelectedLocations.get(0);
			theSeed = SeedFactory.getDefaultSeed(this, getSession().getEventTrace(), theInfo);
		}

		itsNavigator.open(theSeed);
	}
	
	public void showElement (IJavaElement aElement)
	{
		LocationInfo theLocationInfo = TODPluginUtils.getLocationInfo(getSession(), aElement);
		Seed theSeed = SeedFactory.getDefaultSeed(this, getSession().getEventTrace(), theLocationInfo);
		openSeed(theSeed, false);
	}
	
	public void openSeed(Seed aSeed, boolean aNewTab)
	{
		itsNavigator.open(aSeed);
	}

	public void gotoEvent(ILogEvent aEvent)
	{
	    itsTraceNavigatorView.gotoEvent(aEvent);
	}
	
	

}
