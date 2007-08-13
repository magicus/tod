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
package tod.impl.local;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import tod.agent.DebugFlags;
import tod.core.ILogCollector;
import tod.core.LocationRegisterer;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.server.CollectorTODServer;
import tod.core.server.ICollectorFactory;
import tod.core.server.TODServer;
import tod.core.session.AbstractSession;
import tod.core.session.ConnectionInfo;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.database.structure.standard.HostInfo;
import tod.utils.PrintThroughCollector;

public class LocalSession extends AbstractSession
{
	private TODServer itsServer;
	private LocationRegisterer itsLocationRegistrer;
	
	private LocalBrowser itsBrowser;
	private List<ILogCollector> itsCollectors = new ArrayList<ILogCollector>();
	
	public LocalSession(URI aUri, TODConfig aConfig)
	{
		super(aUri, aConfig);
		itsLocationRegistrer = new LocationRegisterer();
		itsBrowser = new LocalBrowser(itsLocationRegistrer);
		
		ASMDebuggerConfig theConfig = new ASMDebuggerConfig(
				aConfig,
				itsLocationRegistrer);

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theConfig);
		
		itsServer = new CollectorTODServer(
				aConfig,
				theInstrumenter,
				new LocationRegisterer(),
				new MyCollectorFactory());
	}
	
	public void disconnect()
	{
		itsServer.stop();
	}
	
	public void flush()
	{
		// Nothing to do here.
	}

	public ILogBrowser getLogBrowser()
	{
		return itsBrowser;
	}
	
	public JComponent createConsole()
	{
		return null;
	}
	
	public boolean isAlive()
	{
		return true;
	}

	private class MyCollectorFactory implements ICollectorFactory
	{
		private int itsHostId = 1;

		public ILogCollector create()
		{
			HostInfo theHost = new HostInfo(itsHostId++);
			ILogCollector theCollector = new LocalCollector(
					itsBrowser,
					theHost);
			
			if (DebugFlags.COLLECTOR_LOG) theCollector = new PrintThroughCollector(
					theHost,
					theCollector,
					getLogBrowser().getLocationsRepository());
			
			itsCollectors.add(theCollector);
			return theCollector;
		}
	}
}
