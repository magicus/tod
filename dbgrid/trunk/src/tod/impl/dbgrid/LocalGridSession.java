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

import javax.swing.JComponent;

import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.session.AbstractSession;
import tod.core.session.ISessionMonitor;
import tod.impl.dbgrid.aggregator.GridEventBrowser;
import tod.impl.dbgrid.gui.GridConsole;

/**
 * A single-process grid session, running in a separate vm.
 * This class handles the creation of the database process.
 * @author gpothier
 */
public class LocalGridSession extends AbstractSession
{
	private DBProcessManager itsProcessManager = DBProcessManager.getDefault();
	private RIGridMaster itsMaster;
	private ILogBrowser itsLogBrowser;
	private ISessionMonitor itsMonitor;
	
	public LocalGridSession(URI aUri, TODConfig aConfig)
	{
		super(aUri, aConfig);
		init();
	}
	
	protected void init()
	{
		// Load POM-synced classes (hack to avoid timeout)
		System.out.println(GridLogBrowser.class);
		System.out.println(GridEventBrowser.class);

		itsProcessManager.stop();
		itsProcessManager.setConfig(getConfig());
		itsProcessManager.start();
		
		try
		{
			itsMaster = itsProcessManager.getMaster();
			itsLogBrowser = GridLogBrowser.createRemote(itsMaster);
			itsMonitor = Scheduler.get(itsLogBrowser);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void disconnect()
	{
		itsProcessManager.stop();
	}


	public JComponent createConsole()
	{
		return new GridConsole(itsMaster);
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
		return itsLogBrowser;
	}

	public ISessionMonitor getMonitor()
	{
		return itsMonitor;
	}

	public boolean isAlive()
	{
		return itsProcessManager.isAlive();
	}
	
}
