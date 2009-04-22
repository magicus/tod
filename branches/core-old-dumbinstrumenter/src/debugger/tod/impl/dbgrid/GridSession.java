/*
 * Created on Aug 28, 2006
 */
package tod.impl.dbgrid;

import java.io.File;
import java.lang.reflect.Method;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.server.ICollectorFactory;
import tod.core.server.TODServer;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.dbgrid.dbnode.DatabaseNode;

public class GridSession
{
	private GridMaster itsMaster;
	private TODServer itsServer;
	
	private LocationRegistrer itsLocationRegistrer;
	
	public GridSession() throws RemoteException
	{
		itsLocationRegistrer = new LocationRegistrer();
		itsMaster = new GridMaster(itsLocationRegistrer);
		
		ASMDebuggerConfig theConfig = new ASMDebuggerConfig(
				itsLocationRegistrer,
				new File("/home/gpothier/tmp/tod"), 
				"[-tod.** -remotebci.** +tod.test.** +tod.demo.**]",
				"[-java.** -javax.** -sun.** -com.sun.**]");

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theConfig);
		
		itsServer = new TODServer(
				new MyCollectorFactory(),
				theInstrumenter);
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

	public static void main(String[] args) throws RemoteException, AlreadyBoundException
	{
		Registry theRegistry = LocateRegistry.createRegistry(1099);
				
		GridSession theSession = new GridSession();
		theRegistry.bind(GridMaster.RMI_ID, theSession.itsMaster);
		
		if (args.length == 0) new DatabaseNode(true);
	}
}
