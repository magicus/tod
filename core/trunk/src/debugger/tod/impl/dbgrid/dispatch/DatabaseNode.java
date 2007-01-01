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
import java.net.Socket;
import java.rmi.RemoteException;

import tod.impl.dbgrid.NodeException;

public abstract class DatabaseNode extends AbstractDispatchNode
implements RIDatabaseNode
{
	private MasterConnection itsMasterConnection;
	
	public DatabaseNode() throws RemoteException
	{
	}
	
	@Override
	protected void connectedToMaster()
	{
		super.connectedToMaster();
		clear();
	}
	
	@Override
	protected void connectToDispatcher(Socket aSocket)
	{
		itsMasterConnection = new MasterConnection(aSocket);
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
	 * The socket thread that handles the connection with the grid master.
	 * @author gpothier
	 */
	private class MasterConnection extends Thread
	{
		private final Socket itsSocket;

		public MasterConnection(Socket aSocket)
		{
			itsSocket = aSocket;
			start();
		}
		
		@Override
		public void run()
		{
			waitConnectedToMaster();
			
			try
			{
				DataInputStream theInStream = new DataInputStream(
						new BufferedInputStream(itsSocket.getInputStream()));
				
				DataOutputStream theOutStream = new DataOutputStream(
						new BufferedOutputStream(itsSocket.getOutputStream()));
				
				while (itsSocket.isConnected())
				{
					try
					{
						byte theCommand = theInStream.readByte();
						processCommand(theCommand, theInStream, theOutStream);
					}
					catch (EOFException e)
					{
						break;
					}
				}
				
				System.exit(0);
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
				System.exit(1);
			}
		}
		
	}

}
