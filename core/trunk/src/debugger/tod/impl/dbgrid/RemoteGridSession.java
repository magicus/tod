/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
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
