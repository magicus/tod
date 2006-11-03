/*
 * Created on Aug 28, 2006
 */
package tod.impl.local;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.config.GeneralConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.HostInfo;
import tod.core.server.ICollectorFactory;
import tod.core.server.TODServer;
import tod.core.session.AbstractSession;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;

public class LocalSession extends AbstractSession
{
	private TODServer itsServer;
	private LocationRegistrer itsLocationRegistrer;
	
	private LocalBrowser itsBrowser;
	private List<LocalCollector> itsCollectors = new ArrayList<LocalCollector>();
	
	public LocalSession(URI aUri)
	{
		super(aUri);
		itsLocationRegistrer = new LocationRegistrer();
		itsBrowser = new LocalBrowser(itsLocationRegistrer);
		
		ASMDebuggerConfig theConfig = new ASMDebuggerConfig(
				itsLocationRegistrer,
				new File(GeneralConfig.LOCATIONS_FILE), 
				"[-tod.** -remotebci.** +tod.test.** +tod.demo.**]",
				"[-java.** -javax.** -sun.** -com.sun.**]");

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theConfig);
		
		itsServer = new TODServer(
				new MyCollectorFactory(),
				theInstrumenter);
	}
	
	public void disconnect()
	{
		itsServer.disconnect();
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
		return null;
	}

	private class MyCollectorFactory implements ICollectorFactory
	{
		private int itsHostId = 1;

		public ILogCollector create()
		{
			LocalCollector theCollector = new LocalCollector(
					itsBrowser,
					new HostInfo(itsHostId++));
			
			itsCollectors.add(theCollector);
			return theCollector;
		}
		
		public void flushAll()
		{
			for (LocalCollector theCollector : itsCollectors)
			{
			}
		}
	}
}
