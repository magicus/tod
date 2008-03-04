/*
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
package tod.core.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import tod.agent.AgentConfig;
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
public abstract class TODServer extends Server
{
	private TODConfig itsConfig;
	
	private IInstrumenter itsInstrumenter;
	
	private IStructureDatabase itsStructureDatabase;
	
	private Map<String, ClientConnection> itsConnections = 
		new HashMap<String, ClientConnection>();
	
	private int itsCurrentHostId = 1;

	public TODServer(
			TODConfig aConfig, 
			IInstrumenter aInstrumenter,
			IStructureDatabase aStructureDatabase)
	{
		super(aConfig.get(TODConfig.COLLECTOR_PORT));

		itsConfig = aConfig;
		itsInstrumenter = aInstrumenter;
		
		itsStructureDatabase = aStructureDatabase;
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
	@Override
	public void close()
	{
		System.out.println("Server disconnecting...");
		super.close();
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
	
	@Override
	protected void accepted(Socket aSocket)
	{
		try
		{
			DataInputStream theStream = new DataInputStream(aSocket.getInputStream());
			int theSignature = theStream.readInt();
			if (theSignature == AgentConfig.CNX_NATIVE) acceptNativeConnection(aSocket);
			else if (theSignature == AgentConfig.CNX_JAVA) acceptJavaConnection(aSocket);
			else throw new RuntimeException("Bad signature: 0x"+Integer.toHexString(theSignature));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
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
	
	protected synchronized void acceptNativeConnection(final Socket aSocket)
	{
		Thread theThread = new Thread("Connection peering")
		{
			@Override
			public synchronized void run()
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
		};
		theThread.setDaemon(true);
		theThread.start();
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
