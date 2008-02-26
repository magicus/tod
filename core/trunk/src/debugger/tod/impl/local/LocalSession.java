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

import tod.core.DebugFlags;
import tod.core.ILogCollector;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.server.CollectorTODServer;
import tod.core.server.ICollectorFactory;
import tod.core.server.TODServer;
import tod.core.session.AbstractSession;
import tod.core.session.ISessionMonitor;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.database.structure.standard.HostInfo;
import tod.impl.database.structure.standard.StructureDatabase;
import tod.utils.PrintThroughCollector;

public class LocalSession extends AbstractSession implements ISessionMonitor
{
	private TODServer itsServer;
	private IMutableStructureDatabase itsStructureDatabase;
	
	private LocalBrowser itsBrowser;
	private List<ILogCollector> itsCollectors = new ArrayList<ILogCollector>();
	
	public LocalSession(URI aUri, TODConfig aConfig)
	{
		super(aUri, aConfig);
		itsStructureDatabase = StructureDatabase.create(aConfig, "bouh");
		itsBrowser = new LocalBrowser(this, itsStructureDatabase);
		
		ASMDebuggerConfig theConfig = new ASMDebuggerConfig(aConfig);
		ASMInstrumenter theInstrumenter = new ASMInstrumenter(itsStructureDatabase, theConfig);
		
		itsServer = new CollectorTODServer(
				aConfig,
				theInstrumenter,
				itsStructureDatabase,
				new MyCollectorFactory());
	}
	
	public void disconnect()
	{
		itsServer.close();
	}
	
	public void flush()
	{
		// Nothing to do here.
	}

	public ILogBrowser getLogBrowser()
	{
		return itsBrowser;
	}
	
	public ISessionMonitor getMonitor()
	{
		return this;
	}
	
	public int getQueueSize()
	{
		return 0;
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
					getLogBrowser().getStructureDatabase());
			
			itsCollectors.add(theCollector);
			return theCollector;
		}
	}
}
