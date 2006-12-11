/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.core.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.LocationRegistrer;
import tod.core.bci.IInstrumenter;
import tod.core.bci.NativeAgentPeer;
import tod.core.config.TODConfig;
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
	private final TODConfig itsConfig;
	
	private ICollectorFactory itsCollectorFactory;
	private IInstrumenter itsInstrumenter;
	
	private LocationRegistrer itsLocationRegistrer;
	
	private List<NativeAgentPeer> itsNativePeers = new ArrayList<NativeAgentPeer>();
	private Map<String, LogReceiver> itsReceivers = new HashMap<String, LogReceiver>();
	
	private LogReceiverServer itsReceiverServer;
	private NativePeerServer itsNativePeerServer;

	public TODServer(TODConfig aConfig, ICollectorFactory aCollectorFactory, IInstrumenter aInstrumenter)
	{
		itsConfig = aConfig;
		itsCollectorFactory = aCollectorFactory;
		itsInstrumenter = aInstrumenter;
		
		itsLocationRegistrer = new LocationRegistrer();
		
		itsReceiverServer = new LogReceiverServer();
		itsNativePeerServer = new NativePeerServer();
	}
	
	/**
	 * Causes this server to stop accepting connections.
	 */
	public void disconnect()
	{
		System.out.println("Server disconnecting...");
		itsReceiverServer.disconnect();
		itsNativePeerServer.disconnect();
		System.out.println("Server disconnected.");
	}

	/**
	 * This method is called when the connection with the target VM is lost.
	 *
	 */
	protected void disconnected()
	{
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
				aSocket)
		{
			@Override
			protected void disconnected()
			{
				super.disconnected();
				TODServer.this.disconnected();
			}
		};
		
		String theHostName = theReceiver.waitHostName();
		itsReceivers.put(theHostName, theReceiver);
		
		System.out.println("Accepted (java) connection from "+theHostName);
	}
	
	protected void acceptNativeConnection(Socket aSocket)
	{
		NativeAgentPeer thePeer = new MyNativePeer(aSocket)
		{
			@Override
			protected void disconnected()
			{
				super.disconnected();
				TODServer.this.disconnected();
			}
		};
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
		public MyNativePeer(Socket aSocket)
		{
			super(itsConfig, aSocket, null, new SynchronizedInstrumenter(itsInstrumenter));
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
