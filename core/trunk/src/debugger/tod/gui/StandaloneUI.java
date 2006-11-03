/*
 * Created on Oct 15, 2006
 */
package tod.gui;

import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import tod.core.database.event.ILogEvent;
import tod.core.session.ISession;
import tod.impl.dbgrid.LocalGridSession;
import tod.impl.dbgrid.RemoteGridSession;
import zz.utils.ui.StackLayout;

public class StandaloneUI extends JPanel
{
	private ISession itsSession;
	private MyTraceView itsTraceView;

	public StandaloneUI(URI aUri)
	{
		String theScheme = aUri != null ? aUri.getScheme() : null;
		if (RemoteGridSession.TOD_GRID_SCHEME.equals(theScheme))
		{
			try
			{
				itsSession = new RemoteGridSession(aUri);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			itsSession = LocalGridSession.create();
		}
		
		createUI();
	}

	private void createUI()
	{
		setLayout(new StackLayout());
		JTabbedPane theTabbedPane = new JTabbedPane();
		add (theTabbedPane);
		
		itsTraceView = new MyTraceView();
		theTabbedPane.addTab("Trace view", itsTraceView);
		
		JComponent theConsole = itsSession.createConsole();
		if (theConsole != null) theTabbedPane.addTab("Console", theConsole);
	}

	private class MyTraceView extends MinerUI
	{
		@Override
		protected ISession getSession()
		{
			return itsSession;
		}

		public void gotoEvent(ILogEvent aEvent)
		{
		}
	}
	
	public static void main(String[] args)
	{
		URI theUri = args.length > 0 ? URI.create(args[0]) : null;
		
		JFrame theFrame = new JFrame("TOD");
		theFrame.setContentPane(new StandaloneUI(theUri));
		theFrame.pack();
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setVisible(true);
	}
}
