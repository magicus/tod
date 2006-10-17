/*
 * Created on Oct 15, 2006
 */
package tod.gui;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.JFrame;

import tod.core.database.event.ILogEvent;
import tod.core.session.ISession;
import tod.impl.dbgrid.LocalGridSession;
import tod.impl.dbgrid.RemoteGridSession;

public class StandaloneUI extends MinerUI
{
	private ISession itsSession;

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
	}

	@Override
	protected ISession getSession()
	{
		return itsSession;
	}

	public void gotoEvent(ILogEvent aEvent)
	{
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
