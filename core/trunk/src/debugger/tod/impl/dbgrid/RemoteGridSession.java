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
package tod.impl.dbgrid;

import java.net.URI;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JComponent;

import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.session.AbstractSession;
import tod.impl.dbgrid.gui.GridConsole;

public class RemoteGridSession extends AbstractSession
{
	public static final String TOD_GRID_SCHEME = "tod-grid";
	private RIGridMaster itsMaster;
	private GridLogBrowser itsBrowser;
	
	public RemoteGridSession(URI aUri, TODConfig aConfig)
	{
		super(aUri, aConfig);
		init();
	}
	
	private void init() 
	{
		try
		{
			String theHost = getConfig().get(TODConfig.COLLECTOR_HOST);
			
			Registry theRegistry = LocateRegistry.getRegistry(theHost);
			itsMaster = (RIGridMaster) theRegistry.lookup(GridMaster.RMI_ID);
			itsMaster.setConfig(getConfig());
			itsMaster.clear();
			itsBrowser = new GridLogBrowser(itsMaster);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void setConfig(TODConfig aConfig)
	{
		super.setConfig(aConfig);
		init();
	}
	
	public void disconnect()
	{
		try
		{
			itsMaster.disconnect();
			itsMaster.flush();
			itsMaster.clear();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void flush()
	{
		try
		{
			itsMaster.flush();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public String getCachedClassesPath()
	{
		return null;
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
