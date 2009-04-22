/*
 * Created on Aug 25, 2006
 */
package tod.core.server;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.LocationRegistrer;
import tod.core.bci.IInstrumenter;
import tod.core.bci.NativeAgentPeer;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.transport.LogReceiver;
import zz.utils.net.Server;

/**
 * A TOD server accepts connections from debugged VMs and process instrumentation
 * requests as well as logged events.
 * The actual implementation of the instrumenter and database are left
 * to delegates.
 * @author gpothier
 */
public class TODServer
{
	private ICollectorFactory itsCollectorFactory;
	private IInstrumenter itsInstrumenter;
	
	private LocationRegistrer itsLocationRegistrer;
	
	private List<NativeAgentPeer> itsNativePeers = new ArrayList<NativeAgentPeer>();
	private Map<String, LogReceiver> itsReceivers = new HashMap<String, LogReceiver>();
	
	private LogReceiverServer itsReceiverServer;
	private NativePeerServer itsNativePeerServer;

	public TODServer(ICollectorFactory aCollectorFactory, IInstrumenter aInstrumenter)
	{
		itsCollectorFactory = aCollectorFactory;
		itsInstrumenter = aInstrumenter;
		
		itsLocationRegistrer = new LocationRegistrer();
		
		itsReceiverServer = new LogReceiverServer();
		itsNativePeerServer = new NativePeerServer();
	}

	private LogReceiver getReceiver(String aHostName)
	{
		return itsReceivers.get(aHostName);
	}
	
	protected void acceptJavaConnection(Socket aSocket)
	{
		LogReceiver theReceiver = new LogReceiver(
				itsCollectorFactory.create(),
				itsLocationRegistrer.getSynchronizedRegistrer(),
				aSocket);
		
		String theHostName = theReceiver.waitHostName();
		itsReceivers.put(theHostName, theReceiver);
		
		System.out.println("Accepted (java) connection from "+theHostName);
	}
	
	protected void acceptNativeConnection(Socket aSocket)
	{
		NativeAgentPeer thePeer = new MyNativePeer(aSocket);
		itsNativePeers.add(thePeer);
		
		System.out.println("Accepted (native) connection from "+thePeer.waitHostName());
	}
	
	/**
	 * A server that creates a {@link LogReceiver} whenever a client
	 * connects.
	 */
	private class LogReceiverServer extends Server
	{
		public LogReceiverServer()
		{
			super(8058);
		}

		@Override
		protected void accepted(Socket aSocket)
		{
			acceptJavaConnection(aSocket);
		}
		
	}
	
	/**
	 * A server that creates a {@link NativePeerServer} whenever a client
	 * connects.
	 * @author gpothier
	 */
	private class NativePeerServer extends Server
	{
		public NativePeerServer()
		{
			super(8059);
		}

		@Override
		protected void accepted(Socket aSocket)
		{
			acceptNativeConnection(aSocket);
		}
	}
	
	private class MyNativePeer extends NativeAgentPeer
	{
		private LogReceiver itsReceiver;

		public MyNativePeer(Socket aSocket)
		{
			super(aSocket, null, new SynchronizedInstrumenter(itsInstrumenter));
		}
		
		private LogReceiver getReceiver()
		{
			if (itsReceiver == null)
			{
				itsReceiver = TODServer.this.getReceiver(getHostName());
			}
			return itsReceiver;
		}
		
		@Override
		protected void processExceptionGenerated(
				long aTimestamp, 
				long aThreadId, 
				String aClassName,
				String aMethodName,
				String aMethodSignature,
				int aBytecodeIndex, 
				Object aException)
		{
			ITypeInfo theType = itsLocationRegistrer.getType(aClassName);
			IBehaviorInfo theBehavior = itsLocationRegistrer.getBehavior(
					theType, 
					aMethodName, 
					aMethodSignature,
					false);
			
			getReceiver().getCollector().logExceptionGenerated(
					aTimestamp, 
					aThreadId, 
					theBehavior.getId(), 
					aBytecodeIndex, 
					aException);

		}

		@Override
		protected void processFlush()
		{
			System.out.println("Flushing...");
			for(LogReceiver theReceiver : itsReceivers.values())
			{
				try
				{
					theReceiver.interrupt();
					theReceiver.join();
				}
				catch (InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}
			itsCollectorFactory.flushAll();
			System.out.println("done.");
		}
		
	}
	
	private static class SynchronizedInstrumenter implements IInstrumenter
	{
		private IInstrumenter itsDelegate;

		public SynchronizedInstrumenter(IInstrumenter aDelegate)
		{
			itsDelegate = aDelegate;
		}

		public synchronized byte[] instrumentClass(String aClassName, byte[] aBytecode)
		{
			return itsDelegate.instrumentClass(aClassName, aBytecode);
		}
	}

}
