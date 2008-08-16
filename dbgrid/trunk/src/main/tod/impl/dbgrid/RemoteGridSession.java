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
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JComponent;

import tod.Util;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.session.AbstractSession;
import tod.impl.dbgrid.gui.GridConsole;

public class RemoteGridSession extends AbstractSession
{
	public static final String TOD_GRID_SCHEME = "tod-grid";
	private RIGridMaster itsMaster;
	private ILogBrowser itsBrowser;
	
	/**
	 * If false the remote master is cleared before use.
	 */
	private boolean itsUseExisting;
	
	public RemoteGridSession(URI aUri, TODConfig aConfig)
	{
		this(aUri, aConfig, false);
	}
	
	public RemoteGridSession(URI aUri, TODConfig aConfig, boolean aUseExisting)
	{
		super(aUri, aConfig);
		itsUseExisting = aUseExisting;
		init();
	}
	
	/**
	 * Returns the host to connect to.
	 * By default, the host specified in the config by {@link TODConfig#COLLECTOR_HOST}.
	 */
	protected String getHost()
	{
		return getConfig().get(TODConfig.COLLECTOR_HOST);
	}
	
	protected void init() 
	{
		try
		{
			String theHost = getHost();
			
			Registry theRegistry = LocateRegistry.getRegistry(theHost, Util.TOD_REGISTRY_PORT);
			itsMaster = (RIGridMaster) theRegistry.lookup(GridMaster.getRMIId(getConfig()));
			itsMaster.setConfig(getConfig());
			if (! itsUseExisting) itsMaster.clear();
			
//			itsBrowser = (ILogBrowser) ReflexBridge.create(
//					"tod.impl.dbgrid.GridLogBrowser", 
//					new Class[] {RIGridMaster.class},
//					itsMaster);
		
			itsBrowser = DebuggerGridConfig.createRemoteLogBrowser(this, itsMaster);
		}
		catch (UnknownHostException e)
		{
			throw new RuntimeException("Unknown host: "+e.getCause().getMessage());
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

	public ILogBrowser getLogBrowser()
	{
		return itsBrowser;
	}
	
	public JComponent createConsole()
	{
		return new GridConsole(itsMaster);
	}
	
	protected RIGridMaster getMaster()
	{
		return itsMaster;
	}

	protected void reset()
	{
		itsMaster = null;
		itsBrowser = null;
	}
	
	public boolean isAlive()
	{
		if (itsMaster == null || itsBrowser == null) return false;
		
		try
		{
			itsMaster.keepAlive();
			return true;
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
