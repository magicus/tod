/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid;

import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import tod.Util;
import tod.core.config.TODConfig;
import tod.gui.IGUIManager;

public class RemoteGridSession extends AbstractGridSession
{
	public static final String TOD_GRID_SCHEME = "tod-grid";
	
	/**
	 * If false the remote master is cleared before use.
	 */
	private boolean itsUseExisting;
	
	public RemoteGridSession(IGUIManager aGUIManager, URI aUri, TODConfig aConfig)
	{
		this(aGUIManager, aUri, aConfig, false);
	}
	
	public RemoteGridSession(IGUIManager aGUIManager, URI aUri, TODConfig aConfig, boolean aUseExisting)
	{
		super(aGUIManager, aUri, aConfig);
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
			RIGridMaster theMaster = (RIGridMaster) theRegistry.lookup(GridMaster.getRMIId(getConfig()));
			theMaster.setConfig(getConfig());
			if (! itsUseExisting) theMaster.clear();
			
			setMaster(theMaster);
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
			getMaster().disconnect();
			getMaster().flush();
			getMaster().clear();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	protected void reset()
	{
		setMaster(null);
	}
	
	public boolean isAlive()
	{
		if (getMaster() == null || getLogBrowser() == null) return false;
		
		try
		{
			getMaster().keepAlive();
			return true;
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
