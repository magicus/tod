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

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JComponent;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.server.ICollectorFactory;
import tod.core.server.TODServer;
import tod.core.session.AbstractSession;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.dbgrid.dbnode.DatabaseNode;

/**
 * A single-process grid session.
 * @author gpothier
 */
public class LocalGridSession extends AbstractSession
{
	private GridMaster itsMaster;
	private TODServer itsServer;
	private GridLogBrowser itsBrowser;
	
	public LocalGridSession(TODConfig aConfig) throws RemoteException
	{
		super(null);

		LocationRegistrer theRegistrer = new LocationRegistrer();
		itsMaster = new GridMaster(theRegistrer, 0);
		
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(
				aConfig,
				theRegistrer);

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theDebuggerConfig);
		
		itsServer = new TODServer(
				aConfig,
				new MyCollectorFactory(),
				theInstrumenter);
		
		itsBrowser = new GridLogBrowser(itsMaster);
	}
	
	public void disconnect()
	{
		itsServer.disconnect();
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
		return null;
	}

	private class MyCollectorFactory implements ICollectorFactory
	{
		private int itsHostId = 1;

		public ILogCollector create()
		{
			return itsMaster.createCollector(itsHostId++);
		}
		
		public void flushAll()
		{
			itsMaster.flush();
		}
	}
	
	public static LocalGridSession create(TODConfig aConfig) 
	{
		try
		{
			Registry theRegistry = LocateRegistry.createRegistry(1099);
					
			LocalGridSession theSession = new LocalGridSession(aConfig);
			theRegistry.bind(GridMaster.RMI_ID, theSession.itsMaster);
			
			new DatabaseNode(true);
			
			return theSession;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
