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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

import tod.core.BehaviourKind;
import tod.core.ILocationRegistrer;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.ThreadInfo;
import tod.core.transport.CollectorPacketReader;
import tod.core.transport.CollectorPacketWriter;
import tod.core.transport.LogReceiver;
import tod.core.transport.MessageType;
import tod.impl.dbgrid.GridMaster;

public class InternalEventDispatcher extends AbstractEventDispatcher
implements RIInternalDispatcher
{
	/**
	 * Current child in the round-robin scheme.
	 */
	private int itsCurrentChild = 0;

	private ILocationRegistrer itsForwardingRegistrer = new ForwardingRegistrer();

	public InternalEventDispatcher(boolean aConnectToMaster) throws RemoteException
	{
		super(aConnectToMaster);
	}

	/**
	 * Forwards the given location infos to the children. This method is used by
	 * the master if this dispatcher is the root.
	 */
	public void forwardLocations(Iterable<ILocationInfo> aLocations)
	{
		for (ILocationInfo theLocation : aLocations)
		{
			theLocation.register(itsForwardingRegistrer);
		}
	}

	@Override
	protected DispatchNodeProxy createProxy(
			RIDispatchNode aConnectable,
			Socket aSocket, 
			String aId)
	{
		try
		{
			DataOutputStream theOutStream = new DataOutputStream(aSocket.getOutputStream());

			// LogReceiver expects host name
			theOutStream.writeUTF("InternalEventDispatcher"); 

			return new DispatcherProxy(aConnectable, aSocket, aId);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public LogReceiver createLogReceiver(IHostInfo aHostInfo, GridMaster aMaster, InputStream aInStream,
			OutputStream aOutStream, boolean aStartImmediately)
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
					aSocket.getInputStream(), 
					aSocket.getOutputStream(), 
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

		private IHostInfo itsHostInfo;

		private byte[] itsBuffer = new byte[2048];

		public ForwardingLogReceiver(GridMaster aMaster, IHostInfo aHostInfo, InputStream aInStream,
				OutputStream aOutStream, boolean aStart)
		{
			super(aInStream, aOutStream, false);
			itsMaster = aMaster;
			itsHostInfo = aHostInfo;
			if (aStart) start();
		}

		@Override
		protected void readPacket(DataInputStream aStream, MessageType aType) throws IOException
		{
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
				DispatcherProxy theProxy = (DispatcherProxy) getChild(itsCurrentChild);
				itsCurrentChild = (itsCurrentChild + 1) % getChildrenCount();

				theProxy.forwardPacket(aType, aStream);
				theProxy.getOutStream().flush();
				break;
				
			case REGISTER_THREAD:
				// Thread registration is not forwarded.
				readThread(aStream);
				break;

			default:
				CollectorPacketReader.readPacket(aStream, itsForwardingRegistrer, aType);
				break;

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
				ThreadInfo theThreadInfo = new ThreadInfo(itsHostInfo, theId, theJVMId, theName);
				itsMaster.registerThread(theThreadInfo);
			}
		}
	}

	/**
	 * A wrapper for the location registrer provided to the grid master. It
	 * forwards registrations to the original registerer as well as to the root
	 * dispatcher's registrer.
	 * 
	 * @see AbstractEventDispatcher#getLocationRegistrer()
	 * @author gpothier
	 */
	private class ForwardingRegistrer implements ILocationRegistrer
	{
		public void registerBehavior(BehaviourKind aBehaviourType, int aBehaviourId, int aTypeId,
				String aBehaviourName, String aSignature)
		{
			try
			{
				for (DispatchNodeProxy theProxy : getChildren())
				{
					CollectorPacketWriter.sendRegisterBehavior(
							theProxy.getOutStream(), 
							aBehaviourType,
							aBehaviourId,
							aTypeId,
							aBehaviourName, 
							aSignature);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void registerBehaviorAttributes(int aBehaviourId, LineNumberInfo[] aLineNumberTable,
				LocalVariableInfo[] aLocalVariableTable)
		{
			try
			{
				for (DispatchNodeProxy theProxy : getChildren())
				{
					CollectorPacketWriter.sendRegisterBehaviorAttributes(
							theProxy.getOutStream(),
							aBehaviourId,
							aLineNumberTable, 
							aLocalVariableTable);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void registerField(int aFieldId, int aTypeId, String aFieldName)
		{
			try
			{
				for (DispatchNodeProxy theProxy : getChildren())
				{
					CollectorPacketWriter.sendRegisterField(
							theProxy.getOutStream(),
							aFieldId,
							aTypeId, 
							aFieldName);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void registerFile(int aFileId, String aFileName)
		{
			try
			{
				for (DispatchNodeProxy theProxy : getChildren())
				{
					CollectorPacketWriter.sendRegisterFile(
							theProxy.getOutStream(),
							aFileId, 
							aFileName);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
		{
			try
			{
				for (DispatchNodeProxy theProxy : getChildren())
				{
					CollectorPacketWriter.sendRegisterType(
							theProxy.getOutStream(), 
							aTypeId, 
							aTypeName,
							aSupertypeId,
							aInterfaceIds);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

	}

}
