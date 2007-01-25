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
package tod.impl.dbgrid.dispatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

import tod.impl.dbgrid.NodeException;

public abstract class DatabaseNode extends AbstractDispatchNode
implements RIDatabaseNode
{
	private DispatcherConnection itsMasterConnection;
	
	private long itsEventsCount = 0;
	private long itsFirstTimestamp = 0;
	private long itsLastTimestamp = 0;


	
	public DatabaseNode() throws RemoteException
	{
	}
	
	@Override
	protected void connectedToMaster()
	{
		super.connectedToMaster();
		clear();
	}

	/**
	 * Subclasses should call this method whenever an event is stored
	 * by this node in order to update statistics
	 * @param aTimestamp
	 */
	protected void eventStored(long aTimestamp)
	{
		itsEventsCount++;
		
		// The following code is a bit faster than using min & max
		// (Pentium M 2ghz)
		if (itsFirstTimestamp == 0) itsFirstTimestamp = aTimestamp;
		if (itsLastTimestamp < aTimestamp) itsLastTimestamp = aTimestamp;
	}
	
	public long getEventsCount()
	{
		return itsEventsCount;
	}

	public long getFirstTimestamp()
	{
		return itsFirstTimestamp;
	}

	public long getLastTimestamp()
	{
		return itsLastTimestamp;
	}

	public void connectToLocalDispatcher(
			InputStream aInputStream,
			OutputStream aOutputStream)
	{
		itsMasterConnection = new LocalDispatcherConnection(
				new DataInputStream(aInputStream),
				new DataOutputStream(aOutputStream));
	}
	
	@Override
	protected void connectToDispatcher(Socket aSocket)
	{
		try
		{
			itsMasterConnection = new SocketDispatcherConnection(aSocket);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Processes a command sent by the master.
	 * @param aCommand Command id
	 * @throws IOException 
	 */
	protected void processCommand(
			byte aCommand, 
			DataInputStream aInStream, 
			DataOutputStream aOutStream) throws IOException
	{
		switch (aCommand)
		{
		case DispatchNodeProxy.CMD_FLUSH:
			int theCount = flush();
			aOutStream.writeInt(theCount);
			aOutStream.flush();
			break;
			
		case DispatchNodeProxy.CMD_CLEAR:
			clear();
			aOutStream.writeInt(1);
			aOutStream.flush();
			break;
			
		default:
			throw new RuntimeException("Not handled: "+aCommand);
				
		}

	}
	
	/**
	 * The socket thread that handles the connection with the dispatcher.
	 * @author gpothier
	 */
	private abstract class DispatcherConnection extends Thread
	{
		private final DataInputStream itsInputStream;
		private final DataOutputStream itsOutputStream;

		
		public DispatcherConnection(
				DataInputStream aInputStream, 
				DataOutputStream aOutputStream)
		{
			itsInputStream = aInputStream;
			itsOutputStream = aOutputStream;

			start();
		}
		
		/**
		 * Whether the connection is still valid.
		 */
		protected abstract boolean isConnected();
		
		@Override
		public void run()
		{
			waitConnectedToMaster();
			
			try
			{
				while (isConnected())
				{
					try
					{
						byte theCommand = itsInputStream.readByte();
						processCommand(theCommand, itsInputStream, itsOutputStream);
					}
					catch (EOFException e)
					{
						break;
					}
				}
			}
			catch (Throwable e)
			{
				try
				{
					getMaster().nodeException(new NodeException(getNodeId(), e));
				}
				catch (RemoteException e1)
				{
					throw new RuntimeException(e1);
				}
				e.printStackTrace();
			}
		}
		
	}

	private class SocketDispatcherConnection extends DispatcherConnection
	{
		private final Socket itsSocket;

		public SocketDispatcherConnection(Socket aSocket) throws IOException
		{
			super(
					new DataInputStream(new BufferedInputStream(aSocket.getInputStream())),
					new DataOutputStream(new BufferedOutputStream(aSocket.getOutputStream())));
			
			itsSocket = aSocket;
		}
		
		@Override
		protected boolean isConnected()
		{
			return itsSocket.isConnected();
		}
	}
	
	private class LocalDispatcherConnection extends DispatcherConnection
	{
		public LocalDispatcherConnection(DataInputStream aInputStream, DataOutputStream aOutputStream)
		{
			super(aInputStream, aOutputStream);
		}

		@Override
		protected boolean isConnected()
		{
			return true;
		}
	}

}
