/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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
