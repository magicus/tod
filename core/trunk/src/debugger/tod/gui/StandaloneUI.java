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

import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import tod.ReflexRiver;
import tod.core.config.TODConfig;
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
		TODConfig theConfig = new TODConfig();
		String theScheme = aUri != null ? aUri.getScheme() : null;
		try
		{
			if (RemoteGridSession.TOD_GRID_SCHEME.equals(theScheme))
			{
				theConfig.set(TODConfig.COLLECTOR_HOST, aUri.getHost());
				itsSession = new RemoteGridSession(aUri, theConfig, true);
			}
			else
			{
				itsSession = new LocalGridSession(aUri, theConfig);
			}
			
			createUI();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private void createUI()
	{
		setLayout(new StackLayout());
		JTabbedPane theTabbedPane = new JTabbedPane();
		add (theTabbedPane);
		
		itsTraceView = new MyTraceView();
		itsTraceView.setSession(itsSession);
		theTabbedPane.addTab("Trace view", itsTraceView);
		
		JComponent theConsole = itsSession.createConsole();
		if (theConsole != null) theTabbedPane.addTab("Console", theConsole);
	}

	private class MyTraceView extends MinerUI
	{
		public void gotoEvent(ILogEvent aEvent)
		{
		}
	}
	
	public static void main(String[] args)
	{
		ReflexRiver.setup();
		URI theUri = args.length > 0 ? URI.create(args[0]) : null;
		
		JFrame theFrame = new JFrame("TOD");
		theFrame.setContentPane(new StandaloneUI(theUri));
		theFrame.pack();
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setVisible(true);
	}
}
