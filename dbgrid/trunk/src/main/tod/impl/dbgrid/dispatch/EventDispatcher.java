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

import static tod.impl.dbgrid.DebuggerGridConfig.DISPATCH_BATCH_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.LOAD_BALANCING;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

import tod.agent.DebugFlags;
import tod.agent.transport.MessageType;
import tod.core.transport.LogReceiver;
import tod.impl.database.structure.standard.HostInfo;
import tod.impl.database.structure.standard.ThreadInfo;
import tod.impl.dbgrid.GridMaster;

public class EventDispatcher extends AbstractEventDispatcher
implements RIDispatcher
{
	public EventDispatcher() throws RemoteException
	{
		super(true);
	}

	@Override
	protected DispatchNodeProxy createProxy(
			RIDispatchNode aConnectable,
			InputStream aInputStream,
			OutputStream aOutputStream,
			String aId)
	{
		try
		{
			DataOutputStream theOutStream = new DataOutputStream(aOutputStream);

			// LogReceiver expects host name
			theOutStream.writeUTF("InternalEventDispatcher"); 

			return new DispatcherProxy(aConnectable, aInputStream, aOutputStream, aId);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public LogReceiver createLogReceiver(
			HostInfo aHostInfo, 
			GridMaster aMaster, 
			InputStream aInStream,
			OutputStream aOutStream, 
			boolean aStartImmediately)
	{
		return new ForwardingLogReceiver(aMaster, aHostInfo, aInStream, aOutStream, aStartImmediately);
	}

	@Override
	protected void connectToDispatcher(Socket aSocket)
	{
		try
		{
			createLogReceiver(
					null, 
					null, 
					new BufferedInputStream(aSocket.getInputStream()), 
					new BufferedOutputStream(aSocket.getOutputStream()), 
					true);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private class ForwardingLogReceiver extends LogReceiver
	{
		/**
		 * Grid master, if we are the root dispatcher. In this case we forward
		 * thread registration to the master.
		 */
		private GridMaster itsMaster;
		
		/**
		 * Remaining packets to process before changing to another child
		 */
		private int itsPacketsBeforeChange = 0;
//		private int itsCurrentChild = 0;
		
		private DispatcherProxy itsCurrentChild;
		private int itsCurrentChildIndex;

		public ForwardingLogReceiver(
				GridMaster aMaster,
				HostInfo aHostInfo,
				InputStream aInStream,
				OutputStream aOutStream,
				boolean aStart)
		{
			super(aHostInfo, aInStream, aOutStream, false);
			itsMaster = aMaster;
			if (aStart) start();
		}
		
		private DispatcherProxy getNextChild()
		{
			if (itsPacketsBeforeChange == 0)
			{
				if (LOAD_BALANCING)
				{
					int theMinChild = Integer.MAX_VALUE;
					itsCurrentChild = null;
					for(int i=0;i<getChildrenCount();i++)
					{
						DispatcherProxy theProxy = (DispatcherProxy) getChild((i+itsCurrentChildIndex) % getChildrenCount());
						int theSize = theProxy.getQueueSize();
						if (theSize < theMinChild)
						{
							itsCurrentChild = theProxy;
							theMinChild = theSize;
							if (theSize == 0) break; // If the child is empty there is no need to continue searching
						}
					}
					// We need a bit of round robin in order to properly balance the
					// dispatch under light load.
					itsCurrentChildIndex++; 
				}
				else
				{
					itsCurrentChild = (DispatcherProxy) getChild(itsCurrentChildIndex);
					itsCurrentChildIndex = (itsCurrentChildIndex+1) % getChildrenCount();
				}
				
				itsPacketsBeforeChange = DISPATCH_BATCH_SIZE;
			}
			
			itsPacketsBeforeChange --;
			return itsCurrentChild;
		}
		
		@Override
		protected int flush()
		{
			int theTotal = 0;
			for (DispatchNodeProxy theProxy : getChildren()) 
			{
				theTotal += theProxy.flush();
			}
			
			return theTotal;
		}
		
		@Override
		protected void clear()
		{
			for (DispatchNodeProxy theProxy : getChildren()) 
			{
				theProxy.clear();
			}
		}

		@Override
		protected void readPacket(DataInputStream aStream, MessageType aType) throws IOException
		{
			checkNodeException();
			switch (aType)
			{
			case INSTANTIATION:
			case SUPER_CALL:
			case METHOD_CALL:
			case BEHAVIOR_EXIT:
			case FIELD_WRITE:
			case ARRAY_WRITE:
			case LOCAL_VARIABLE_WRITE:
			case OUTPUT:
			case EXCEPTION:
			case REGISTERED:
				DispatcherProxy theProxy = getNextChild();
				theProxy.forwardPacket(aType, aStream);
				if (DebugFlags.DISPATCH_FAKE_1) theProxy.getOutStream().flush();
				break;
				
			case REGISTER_THREAD:
				// Thread registration is not forwarded.
				readThread(aStream);
				break;

			default:
				throw new RuntimeException("Not handled: "+aType);
			}
			
		}

		private void readThread(DataInputStream aStream) throws IOException
		{
			int theSize = aStream.readInt();
			int theId = aStream.readInt();
			long theJVMId = aStream.readLong();
			String theName = aStream.readUTF();

			if (itsMaster != null)
			{
				ThreadInfo theThreadInfo = new ThreadInfo(getHostInfo(), theId, theJVMId, theName);
				itsMaster.registerThread(theThreadInfo);
			}
		}
	}

}
