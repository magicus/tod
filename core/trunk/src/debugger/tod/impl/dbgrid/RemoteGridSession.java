/*
 * Created on Oct 16, 2006
 */
package tod.impl.dbgrid;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JComponent;

import tod.core.database.browser.ILogBrowser;
import tod.core.session.AbstractSession;
import tod.impl.dbgrid.gui.GridConsole;

public class RemoteGridSession extends AbstractSession
{
	public static final String TOD_GRID_SCHEME = "tod-grid";
	private RIGridMaster itsMaster;
	private GridLogBrowser itsBrowser;
	
	public RemoteGridSession(URI aUri) throws RemoteException, NotBoundException
	{
		super(aUri);
		
		if (! TOD_GRID_SCHEME.equals(aUri.getScheme())) 
			throw new IllegalArgumentException("Invalid URI: "+aUri);
		
		String theHost = aUri.getHost();
		int thePort = aUri.getPort();
		
		Registry theRegistry = LocateRegistry.getRegistry(theHost, thePort);
		itsMaster = (RIGridMaster) theRegistry.lookup(GridMaster.RMI_ID);
		itsBrowser = new GridLogBrowser(itsMaster);
	}
	
	public void disconnect()
	{
	}

	public String getCachedClassesPath()
	{
		throw new UnsupportedOperationException();
	}

	public ILogBrowser getLogBrowser()
	{
		return itsBrowser;
	}

	public JComponent createConsole()
	{
		return new GridConsole(itsBrowser);
	}
	
	
}
