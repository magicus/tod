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
package tod.impl.dbgrid.dispatcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.Output;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ObjectId;
import tod.core.database.structure.ThreadInfo;
import tod.core.transport.LogReceiver;
import tod.core.transport.MessageType;
import tod.impl.dbgrid.GridMaster;
import zz.utils.net.Server.ServerAdress;

public class InternalEventDispatcher extends AbstractEventDispatcher
{
	/**
	 * Current child in the round-robin scheme.
	 */
	private int itsCurrentChild = 0;
	
	public InternalEventDispatcher(boolean aConnectToMaster) throws RemoteException
	{
		super(aConnectToMaster);
	}
	
	@Override
	protected void acceptChild(Socket aSocket)
	{
		try
		{
			DataInputStream theInStream = new DataInputStream(aSocket.getInputStream());
			DataOutputStream theOutStream = new DataOutputStream(aSocket.getOutputStream());
			
			theOutStream.writeUTF("InternalEventDispatcher"); // LogReceiver expects host name
			
			DispatcherProxy theProxy = new DispatcherProxy(aSocket, -1);
			addChild(theProxy);
			System.out.println("Internal dispatcher accepted child.");
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public LogReceiver createLogReceiver(
			IHostInfo aHostInfo,
			GridMaster aMaster, 
			LocationRegistrer aRegistrer,
			InputStream aInStream, 
			OutputStream aOutStream,
			boolean aStartImmediately)
	{
		return new MyLogReceiver(aMaster, aHostInfo, aInStream, aOutStream, aStartImmediately);
	}

	public void connectToDispatcher(ServerAdress aAdress) 
	{
		try
		{
			Socket theSocket = aAdress.connect();
			createLogReceiver(
					null, 
					null, 
					null, 
					theSocket.getInputStream(), 
					theSocket.getOutputStream(), 
					true);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private class MyLogReceiver extends LogReceiver
	{
		/**
		 * Grid master, if we are the root dispatcher.
		 * In this case we forward thread registration to the master. 
		 */
		private GridMaster itsMaster; 
		
		private IHostInfo itsHostInfo;
		
		private byte[] itsBuffer = new byte[2048];
		
		public MyLogReceiver(
				GridMaster aMaster,
				IHostInfo aHostInfo,
				InputStream aInStream, 
				OutputStream aOutStream, 
				boolean aStart)
		{
			super(aInStream, aOutStream, false);
			itsMaster = aMaster;
			itsHostInfo = aHostInfo;
			if (aStart) start();
		}

		@Override
		protected void readPacket(DataInputStream aStream, MessageType aType) throws IOException
		{
			DispatcherProxy theProxy = (DispatcherProxy) getChild(itsCurrentChild++);
			theProxy.pushByte((byte) aType.ordinal());
			
			switch (aType)
			{
				case INSTANTIATION:
	                readInstantiation(aStream, theProxy);
	                break;
	                
				case SUPER_CALL:
					readSuperCall(aStream, theProxy);
					break;
					
				case METHOD_CALL:
	                readMethodCall(aStream, theProxy);
	                break;
	                
				case BEHAVIOR_EXIT:
	                readBehaviorExit(aStream, theProxy);
	                break;
	                
				case FIELD_WRITE:
	                readFieldWrite(aStream, theProxy);
	                break;
	                
				case ARRAY_WRITE:
					readArrayWrite(aStream, theProxy);
					break;
					
				case LOCAL_VARIABLE_WRITE:
					readLocalWrite(aStream, theProxy);
					break;
					
				case OUTPUT:
					readOutput(aStream, theProxy);
					break;
					
				case EXCEPTION:
					readException(aStream, theProxy);
					break;
					
				case REGISTER_THREAD:
					readThread(aStream, theProxy);
					break;
					
			}
		}
		
		private MessageType readMessageType (DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			byte theByte = aStream.readByte();
			aProxy.pushByte(theByte);
			return MessageType.values()[theByte];
		}
		

	    private void readArguments(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
	    {
	        int theCount = aStream.readInt();
	        aProxy.pushInt(theCount);
	        
	        for (int i=0;i<theCount;i++)
	        {
	            readValue(aStream, aProxy);
	        }
	    }
	    
	    private void readUTF(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
	    {
	    	short theSize = aStream.readShort();
	    	aProxy.pushShort(theSize);
	    	aProxy.pipe(aStream, theSize);
	    }
	    
	    private void readBytes(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
	    {
	        int theLength = aStream.readInt();
	        aProxy.pushInt(theLength);
	        aProxy.pipe(aStream, theLength);
	    }

	    
		private void readValue (DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			MessageType theType = readMessageType(aStream, aProxy);
			aProxy.pushByte((byte) theType.ordinal());

			switch (theType)
			{
				case NULL:
					break;
					
				case BOOLEAN:
					aProxy.pipe(aStream, 1);
					break;
					
				case BYTE:
					aProxy.pipe(aStream, 1);
					break;
					
				case CHAR:
					aProxy.pipe(aStream, 2);
					break;
					
				case INT:
					aProxy.pipe(aStream, 4);
					break;
					
				case LONG:
					aProxy.pipe(aStream, 8);
					break;
					
				case FLOAT:
					aProxy.pipe(aStream, 4);
					break;
					
				case DOUBLE:
					aProxy.pipe(aStream, 8);
					break;
					
				case REGISTERED:
					long theObjectId = aStream.readLong();
					ObjectInputStream theStream = new ObjectInputStream(aStream);
					Object theObject;
					try
					{
						theObject = theStream.readObject();
					}
					catch (ClassNotFoundException e)
					{
						System.err.println("Warning - class no found: "+e.getMessage());
						theObject = "Unknown ("+e.getMessage()+")";
					}

					throw new UnsupportedOperationException();
//					break;
					
				case OBJECT_UID:
					aProxy.pipe(aStream, 8);
					break;
					
				case OBJECT_HASH:
					aProxy.pipe(aStream, 4);
					break;
					
				default:
					throw new RuntimeException("Unexpected message: "+theType);
			}
		}
		
		
		private void readMethodCall(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			aProxy.pipe(aStream, 35);
			readValue(aStream, aProxy);
			readArguments(aStream, aProxy);
		}
		
		private void readInstantiation(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			aProxy.pipe(aStream, 35);
			readValue(aStream, aProxy);
			readArguments(aStream, aProxy);
		}
		
		private void readSuperCall(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			aProxy.pipe(aStream, 35);
			readValue(aStream, aProxy);
			readArguments(aStream, aProxy);
		}
		
		private void readBehaviorExit(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			aProxy.pipe(aStream, 31);
			readValue(aStream, aProxy);
		}
		
		private void readFieldWrite(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			aProxy.pipe(aStream, 30);
			readValue(aStream, aProxy);
			readValue(aStream, aProxy);
		}
		
		private void readArrayWrite(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			aProxy.pipe(aStream, 26);
			readValue(aStream, aProxy);
			aProxy.pipe(aStream, 4);
			readValue(aStream, aProxy);
		}
		
		private void readLocalWrite(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			aProxy.pipe(aStream, 30);
			readValue(aStream, aProxy);
		}
		
		private void readException(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			aProxy.pipe(aStream, 22);
			readUTF(aStream, aProxy);
			readUTF(aStream, aProxy);
			readUTF(aStream, aProxy);
			aProxy.pipe(aStream, 4);
			readValue(aStream, aProxy);
		}
		
		private void readOutput(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
			aProxy.pipe(aStream, 23);
			readBytes(aStream, aProxy);
		}
		
		private void readThread(DataInputStream aStream, DispatcherProxy aProxy) throws IOException
		{
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
	
}
