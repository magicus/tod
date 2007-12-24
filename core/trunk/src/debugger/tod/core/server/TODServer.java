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
package tod.core.server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import tod.core.bci.IInstrumenter;
import tod.core.bci.NativeAgentPeer;
import tod.core.config.TODConfig;
import tod.core.database.structure.IStructureDatabase;
import tod.core.transport.LogReceiver;
import zz.utils.net.Server;

/**
 * A TOD server accepts connections from debugged VMs and process instrumentation
 * requests as well as logged events.
 * The actual implementation of the instrumenter and database are left
 * to delegates.
 * @author gpothier
 */
public abstract class TODServer
{
	private TODConfig itsConfig;
	
	private IInstrumenter itsInstrumenter;
	
	private IStructureDatabase itsStructureDatabase;
	
	private Map<String, ClientConnection> itsConnections = 
		new HashMap<String, ClientConnection>();
	
	private LogReceiverServer itsReceiverServer;
	private NativePeerServer itsNativePeerServer;
	
	private int itsCurrentHostId = 1;

	public TODServer(
			TODConfig aConfig, 
			IInstrumenter aInstrumenter,
			IStructureDatabase aStructureDatabase)
	{
		itsConfig = aConfig;
		itsInstrumenter = aInstrumenter;
		
		itsStructureDatabase = aStructureDatabase;
		
		itsReceiverServer = new LogReceiverServer();
		itsNativePeerServer = new NativePeerServer();
	}
	
	public void setConfig(TODConfig aConfig)
	{
		itsConfig = aConfig;
	}
	
	public IStructureDatabase getStructureDatabase()
	{
		return itsStructureDatabase;
	}

	/**
	 * Causes this server to stop accepting connections.
	 */
	public void stop()
	{
		System.out.println("Server disconnecting...");
		itsReceiverServer.disconnect();
		itsNativePeerServer.disconnect();
		System.out.println("Server disconnected.");
	}
	
	/**
	 * Disconnects from all currently connected VMs.
	 */
	public synchronized void disconnect()
	{
		for(ClientConnection theConnection : itsConnections.values())
		{
			theConnection.getLogReceiver().disconnect();
		}
		itsConnections.clear();
		disconnected();
	}

	/**
	 * This method is called when target VMs are disconnected.
	 */
	protected void disconnected()
	{
		itsCurrentHostId = 1;
	}

	/**
	 * This method is called when a client connects to the server when there
	 * are no open connections (ie, when the server passes from the "no client
	 * connected" state to the "client(s) connected" state).
	 */
	protected void connected()
	{
	}

	/**
	 * Creates a receiver for a host.
	 */
	protected abstract LogReceiver createReceiver(Socket aSocket);
	
	/**
	 * Disconnects from the given host
	 */
	protected synchronized void disconnect(String aHostname)
	{
		ClientConnection theConnection = itsConnections.get(aHostname);

		// The connection can be null if only the native agent
		// was connected.
		if (theConnection != null)
		{
			LogReceiver theReceiver = theConnection.getLogReceiver();
			theReceiver.disconnect();
			itsConnections.remove(aHostname);
			if (itsConnections.size() == 0) disconnected();
		}
	}
	
	protected synchronized void acceptJavaConnection(Socket aSocket)
	{
		LogReceiver theReceiver = createReceiver(aSocket);
		
		String theHostName = theReceiver.waitHostName();
		if (itsConnections.containsKey(theHostName))
		{
			try
			{
				aSocket.close();
				throw new RuntimeException("Host already connected: "+theHostName);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		itsConnections.put(theHostName, new ClientConnection(theReceiver));
		System.out.println("Accepted (java) connection from "+theHostName);

		notifyAll();
	}
	
	protected synchronized void acceptNativeConnection(Socket aSocket)
	{
		try
		{
			NativeAgentPeer thePeer = new MyNativePeer(aSocket, itsCurrentHostId++);
			thePeer.waitConfigured();
			String theHostName = thePeer.getHostName();
			
			while(! aSocket.isClosed())
			{
				ClientConnection theConnection = itsConnections.get(theHostName);
				if (theConnection == null) wait(1000);
				else
				{
					theConnection.setNativeAgentPeer(thePeer);
					break;
				}
			}
			
			System.out.println("Accepted (native) connection from "+theHostName);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * A server that creates a {@link LogReceiver} whenever a client
	 * connects.
	 */
	private class LogReceiverServer extends Server
	{
		public LogReceiverServer()
		{
			super(itsConfig.get(TODConfig.COLLECTOR_JAVA_PORT));
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
			super(itsConfig.get(TODConfig.COLLECTOR_NATIVE_PORT));
		}

		@Override
		protected void accepted(Socket aSocket)
		{
			acceptNativeConnection(aSocket);
		}
	}
	
	private class MyNativePeer extends NativeAgentPeer
	{
		public MyNativePeer(Socket aSocket, int aHostId)
		{
			super(
					itsConfig, 
					aSocket, 
					itsStructureDatabase.getId(), 
					new SynchronizedInstrumenter(itsInstrumenter),
					aHostId);
		}
		
		@Override
		protected void processFlush()
		{
			disconnected();
		}
		
		@Override
		protected void disconnected()
		{
			if (getHostName() != null)
			{
				TODServer.this.disconnect(getHostName());
				super.disconnected();
			}
		}
	}
	
	private static class SynchronizedInstrumenter implements IInstrumenter
	{
		private IInstrumenter itsDelegate;

		public SynchronizedInstrumenter(IInstrumenter aDelegate)
		{
			itsDelegate = aDelegate;
		}

		public synchronized InstrumentedClass instrumentClass(String aClassName, byte[] aBytecode)
		{
			return itsDelegate.instrumentClass(aClassName, aBytecode);
		}

		public synchronized void setGlobalWorkingSet(String aWorkingSet)
		{
			itsDelegate.setGlobalWorkingSet(aWorkingSet);
		}

		public synchronized void setTraceWorkingSet(String aWorkingSet)
		{
			itsDelegate.setTraceWorkingSet(aWorkingSet);
		}
	}
	
	/**
	 * Groups both connection entities of a client (log receiver
	 * and native peer).
	 * @author gpothier
	 */
	private static class ClientConnection
	{
		private LogReceiver itsLogReceiver;
		private NativeAgentPeer itsNativeAgentPeer;
		
		public ClientConnection(LogReceiver aLogReceiver)
		{
			itsLogReceiver = aLogReceiver;
		}

		public NativeAgentPeer getNativeAgentPeer()
		{
			return itsNativeAgentPeer;
		}

		public void setNativeAgentPeer(NativeAgentPeer aNativeAgentPeer)
		{
			itsNativeAgentPeer = aNativeAgentPeer;
		}

		public LogReceiver getLogReceiver()
		{
			return itsLogReceiver;
		}

	}

}
