/*
 * Created on Aug 28, 2006
 */
package tod.impl.dbgrid;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JComponent;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.config.GeneralConfig;
import tod.core.database.browser.ILocationsRepository;
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
	
	public LocalGridSession() throws RemoteException
	{
		super(null);

		LocationRegistrer theRegistrer = new LocationRegistrer();
		itsMaster = new GridMaster(theRegistrer, 0);
		
		ASMDebuggerConfig theConfig = new ASMDebuggerConfig(
				theRegistrer,
				new File(GeneralConfig.LOCATIONS_FILE), 
				"[-tod.** -remotebci.** +tod.test.** +tod.demo.**]",
				"[-java.** -javax.** -sun.** -com.sun.**]");

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theConfig);
		
		itsServer = new TODServer(
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
	
	public static LocalGridSession create() 
	{
		try
		{
			Registry theRegistry = LocateRegistry.createRegistry(1099);
					
			LocalGridSession theSession = new LocalGridSession();
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
