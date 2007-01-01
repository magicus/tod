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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JComponent;

import tod.core.LocationRegisterer;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.session.AbstractSession;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.dbgrid.gridimpl.GridImpl;

/**
 * A single-process grid session.
 * @author gpothier
 */
public class LocalGridSession extends AbstractSession
{
	private GridMaster itsMaster;
	private GridLogBrowser itsBrowser;
	
	public LocalGridSession(TODConfig aConfig) throws RemoteException
	{
		super(null);

		LocationRegisterer theRegistrer = new LocationRegisterer();
		
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(
				aConfig,
				theRegistrer);

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theDebuggerConfig);
		
		itsMaster = new GridMaster(aConfig, theRegistrer, theInstrumenter, 0);
		
		itsBrowser = new GridLogBrowser(itsMaster);
	}
	
	public void disconnect()
	{
		itsMaster.disconnect();
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

	public static LocalGridSession create(TODConfig aConfig) 
	{
		try
		{
			Registry theRegistry = LocateRegistry.createRegistry(1099);
					
			LocalGridSession theSession = new LocalGridSession(aConfig);
			theRegistry.bind(GridMaster.RMI_ID, theSession.itsMaster);
			
			GridImpl.getFactory(aConfig).createNode(true);
			
			return theSession;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
