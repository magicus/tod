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

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ILocationInfo;
import tod.gui.BrowserNavigator;
import tod.gui.IGUIManager;
import tod.gui.seed.Seed;
import tod.gui.seed.SeedFactory;
import tod.gui.seed.ThreadsSeed;
import tod.plugin.DebuggingSession;
import tod.plugin.TODPluginUtils;
import tod.plugin.TODSessionManager;
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
		
		TODSessionManager.getInstance().pCurrentSession().addHardListener(new PropertyListener<DebuggingSession>()
				{
					public void propertyChanged(IProperty<DebuggingSession> aProperty, DebuggingSession aOldValue, DebuggingSession aNewValue)
					{
						reset();
					}
				});
	}

	private DebuggingSession getSession()
	{
		return TODSessionManager.getInstance().pCurrentSession().get();
	}
	
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
	
	private void reset()
	{
		openSeed(new ThreadsSeed(EventViewer.this, getSession().getLogBrowser()), false);
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
	
	public void showElement (IJavaElement aElement)
	{
		ILocationInfo theLocationInfo = TODPluginUtils.getLocationInfo(getSession(), aElement);
		Seed theSeed = SeedFactory.getDefaultSeed(this, getSession().getLogBrowser(), theLocationInfo);
		openSeed(theSeed, false);
	}
	
	public void openSeed(Seed aSeed, boolean aNewTab)
	{
		itsNavigator.open(aSeed);
	}

	public void gotoEvent(ILogEvent aEvent)
	{
	    itsTraceNavigatorView.gotoEvent(getSession(), aEvent);
	}
	
	

}
