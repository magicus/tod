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
package tod.core.transport;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import tod.core.ObjectIdentity;
import tod.core.Output;

/**
 * Provides the methods used to encode streamed log data.
 * Non-static methods are not thread-safe
 */
public class CollectorPacketWriter
{
	private final MyBuffer itsBuffer = new MyBuffer();
	private final DataOutputStream itsStream;
	
	/**
	 * List of registered objects that must be sent.
	 * Note: There is space for a hard-coded number of entries that
	 * should "be enough for everybody". 
	 * Note that if only exception events are enabled this array
	 * will overflow.
	 */
	private final ObjectEntry[] itsRegisteredObjects = new ObjectEntry[1024];
	
	/**
	 * Number of entries in {@link #itsRegisteredObjects}.
	 */
	private int itsRegisteredObjectsCount = 0;
	
	public CollectorPacketWriter(DataOutputStream aStream)
	{
		itsStream = aStream;
		for (int i = 0; i < itsRegisteredObjects.length; i++)
		{
			itsRegisteredObjects[i] = new ObjectEntry();
		}
	}

	private void sendMethodCall(
			MessageType aMessageType,
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			long aOperationLocation,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments) throws IOException
	{
		sendMessageType(itsStream, aMessageType);

		sendStd(itsBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		sendOperationLocation(itsBuffer, aOperationLocation);
		itsBuffer.writeBoolean(aDirectParent);
		itsBuffer.writeInt(aCalledBehavior);
		itsBuffer.writeInt(aExecutedBehavior);
		sendValue(itsBuffer, aTarget);
		sendArguments(itsBuffer, aArguments);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
	}
	
	public void sendMethodCall(
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			long aOperationLocation,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments) throws IOException
	{
		sendMethodCall(
				MessageType.METHOD_CALL,
				aThreadId, 
				aParentTimestamp,
				aDepth,
				aTimestamp, 
				aOperationLocation,
				aDirectParent, 
				aCalledBehavior,
				aExecutedBehavior, 
				aTarget,
				aArguments);
	}
	
	public void sendInstantiation(
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			long aOperationLocation,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments) throws IOException
	{
		sendMethodCall(
				MessageType.INSTANTIATION,
				aThreadId, 
				aParentTimestamp,
				aDepth,
				aTimestamp, 
				aOperationLocation,
				aDirectParent, 
				aCalledBehavior,
				aExecutedBehavior, 
				aTarget,
				aArguments);
	}
	
	public void sendSuperCall(
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			long aOperationLocation,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments) throws IOException
	{
		sendMethodCall(
				MessageType.SUPER_CALL,
				aThreadId, 
				aParentTimestamp,
				aDepth,
				aTimestamp, 
				aOperationLocation,
				aDirectParent, 
				aCalledBehavior,
				aExecutedBehavior, 
				aTarget,
				aArguments);
	}
	
	public void sendBehaviorExit(
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			long aOperationLocation,
			int aBehaviorId,
			boolean aHasThrown,
			Object aResult) throws IOException
	{
		sendMessageType(itsStream, MessageType.BEHAVIOR_EXIT);

		sendStd(itsBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		sendOperationLocation(itsBuffer, aOperationLocation);
		itsBuffer.writeInt(aBehaviorId);
		itsBuffer.writeBoolean(aHasThrown);
		sendValue(itsBuffer, aResult);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
	}
	
	public void sendFieldWrite(
			int aThreadId,
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			long aOperationLocation,
			int aFieldLocationId,
			Object aTarget,
			Object aValue) throws IOException
	{
		sendMessageType(itsStream, MessageType.FIELD_WRITE);

		sendStd(itsBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		sendOperationLocation(itsBuffer, aOperationLocation);
		itsBuffer.writeInt(aFieldLocationId);
		sendValue(itsBuffer, aTarget);
		sendValue(itsBuffer, aValue);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
	}
	
	public void sendNewArray(
			int aThreadId,
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			long aOperationLocation,
			Object aTarget,
			int aBaseTypeId,
			int aSize) throws IOException
	{
		sendMessageType(itsStream, MessageType.NEW_ARRAY);

		sendStd(itsBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		sendOperationLocation(itsBuffer, aOperationLocation);
		sendValue(itsBuffer, aTarget);
		itsBuffer.writeInt(aBaseTypeId);
		itsBuffer.writeInt(aSize);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
	}
	
	public void sendArrayWrite(
			int aThreadId,
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			long aOperationLocation,
			Object aTarget,
			int aIndex,
			Object aValue) throws IOException
			{
		sendMessageType(itsStream, MessageType.ARRAY_WRITE);
		
		sendStd(itsBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		sendOperationLocation(itsBuffer, aOperationLocation);
		sendValue(itsBuffer, aTarget);
		itsBuffer.writeInt(aIndex);
		sendValue(itsBuffer, aValue);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
			}
	
	public void sendLocalWrite(
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp,
			long aOperationLocation,
			int aVariableId,
			Object aValue) throws IOException
	{
		sendMessageType(itsStream, MessageType.LOCAL_VARIABLE_WRITE);

		sendStd(itsBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		sendOperationLocation(itsBuffer, aOperationLocation);
		itsBuffer.writeInt(aVariableId);
		sendValue(itsBuffer, aValue);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
	}
	
	public void sendException(
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp,
			String aMethodName, 
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex,
			Object aException) throws IOException
	{
		sendMessageType(itsStream, MessageType.EXCEPTION);

		sendStd(itsBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		itsBuffer.writeUTF(aMethodName);
		itsBuffer.writeUTF(aMethodSignature);
		itsBuffer.writeUTF(aMethodDeclaringClassSignature);
		itsBuffer.writeShort(aOperationBytecodeIndex);
		sendValue(itsBuffer, aException);
		
		itsBuffer.writeTo(itsStream);
		
		// We don't send registered objects here because it seems to cause
		// a bad interaction with the native side.
//		sendRegisteredObjects();
	}
	
	public void sendOutput(
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp,
			Output aOutput,
			byte[] aData) throws IOException
	{
		sendMessageType(itsStream, MessageType.OUTPUT);

		sendStd(itsBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		itsBuffer.writeByte((byte) aOutput.ordinal());
		itsBuffer.writeInt(aData.length);
		itsBuffer.write(aData);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
	}
	
	public void sendThread(
			int aThreadId, 
			long aJVMThreadId,
			String aName) throws IOException
	{
		sendMessageType(itsStream, MessageType.REGISTER_THREAD);

		itsBuffer.writeInt(aThreadId);
		itsBuffer.writeLong(aJVMThreadId);
		itsBuffer.writeUTF(aName);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
	}
	
	public void sendClear() throws IOException
	{
		itsStream.writeByte(MessageType.CMD_CLEAR);
	}

	
	private void sendStd(
			DataOutputStream aStream,
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp) throws IOException
	{
		aStream.writeInt(aThreadId);
		aStream.writeLong(aParentTimestamp);
		aStream.writeShort(aDepth);
		aStream.writeLong(aTimestamp);
	}
	
    /**
	 * Sends an argument to the socket. This method handles arrays, single
	 * objects or null values.
	 */
	private void sendArguments(
			DataOutputStream aStream,
			Object[] aArguments) throws IOException
	{
		aStream.writeInt(aArguments != null ? aArguments.length : 0);

		if (aArguments != null) for (Object theArgument : aArguments)
			sendValue(aStream, theArgument);
	}
    

	
	private void sendValue (DataOutputStream aStream, Object aValue) throws IOException
	{
		if (aValue == null)
		{
			sendMessageType(aStream, MessageType.NULL);
		}
		else if (aValue instanceof Boolean)
		{
			Boolean theBoolean = (Boolean) aValue;
			sendMessageType(aStream, MessageType.BOOLEAN);
			aStream.writeByte(theBoolean.booleanValue() ? 1 : 0);
		}
		else if (aValue instanceof Byte)
		{
			Byte theByte = (Byte) aValue;
			sendMessageType(aStream, MessageType.BYTE);
			aStream.writeByte(theByte.byteValue());
		}
		else if (aValue instanceof Character)
		{
			Character theCharacter = (Character) aValue;
			sendMessageType(aStream, MessageType.CHAR);
			aStream.writeChar(theCharacter.charValue());
		}
		else if (aValue instanceof Integer)
		{
			Integer theInteger = (Integer) aValue;
			sendMessageType(aStream, MessageType.INT);
			aStream.writeInt(theInteger.intValue());
		}
		else if (aValue instanceof Long)
		{
			Long theLong = (Long) aValue;
			sendMessageType(aStream, MessageType.LONG);
			aStream.writeLong(theLong.longValue());
		}
		else if (aValue instanceof Float)
		{
			Float theFloat = (Float) aValue;
			sendMessageType(aStream, MessageType.FLOAT);
			aStream.writeFloat(theFloat.floatValue());
		}
		else if (aValue instanceof Double)
		{
			Double theDouble = (Double) aValue;
			sendMessageType(aStream, MessageType.DOUBLE);
			aStream.writeDouble(theDouble.doubleValue());
		}
		else if ((aValue instanceof String)
				|| (aValue instanceof Throwable))
		{
			sendObject(aStream, aValue);
		}
		else
		{
			long theObjectId = ObjectIdentity.get(aValue);
			sendMessageType(aStream, MessageType.OBJECT_UID);
			aStream.writeLong(Math.abs(theObjectId));
		}
//		else
//		{
//			int theHash = aValue.hashCode();
//			sendMessageType(aStream, MessageType.OBJECT_HASH);
//			aStream.writeInt(theHash);
//		}
	}
	
	
	
	private void sendObject(DataOutputStream aStream, Object aObject) throws IOException
	{
		long theObjectId = ObjectIdentity.get(aObject);
		
		assert theObjectId != 0;
		if (theObjectId < 0)
		{
			// First time this object appears, register it.
			theObjectId = -theObjectId;
			itsRegisteredObjects[itsRegisteredObjectsCount++].set(theObjectId, aObject);
		}
		
		sendMessageType(aStream, MessageType.OBJECT_UID);
		aStream.writeLong(theObjectId);
	}
	
	/**
	 * Sends all pending registered objects.
	 * @throws IOException
	 */
	private void sendRegisteredObjects() throws IOException
	{
		while (itsRegisteredObjectsCount > 0)
		{
			ObjectEntry theEntry = itsRegisteredObjects[--itsRegisteredObjectsCount];

			sendMessageType(itsStream, MessageType.REGISTERED);
			itsBuffer.writeLong(theEntry.id);
			MyObjectOutputStream theStream = new MyObjectOutputStream(itsBuffer);
			theStream.writeObject(theEntry.object);
			theStream.drain();
			
			itsBuffer.writeTo(itsStream);
		}
	}

	private static void sendMessageType (DataOutputStream aStream, MessageType aMessageType) throws IOException
	{
		aStream.writeByte(aMessageType.ordinal());	
	}
	
	private void sendOperationLocation(
			DataOutputStream aStream,
			long aOperationLocation) throws IOException
	{
		aStream.writeInt((int) (aOperationLocation >>> 16)); // Behavior id
		aStream.writeShort((short) (aOperationLocation & 0xffff)); // bytecode indexBehavior
	}
	

	
	private static class MyObjectOutputStream extends ObjectOutputStream
	{
		public MyObjectOutputStream(OutputStream aOut) throws IOException
		{
			super(aOut);
		}

		@Override
		public void drain() throws IOException
		{
			super.drain();
		}
	}
	
	/**
	 * Per-thread byte buffer for preparing packets.
	 * @author gpothier
	 */
	private static class MyBuffer extends DataOutputStream
	{
		public MyBuffer()
		{
			super(new ByteArrayOutputStream());
		}

		/**
		 * Writes the size of the buffer and its content to the
		 * given stream.
		 */
		public void writeTo(DataOutputStream aStream) throws IOException
		{
			flush();
			ByteArrayOutputStream theByteOut = (ByteArrayOutputStream) out;
			aStream.writeInt(theByteOut.size());
			theByteOut.writeTo(aStream);
			theByteOut.reset();
		}
	}
	
	private static class ObjectEntry
	{
		public long id;
		public Object object;
		
		public void set(long aId, Object aObject)
		{
			id = aId;
			object = aObject;
		}
	}
}
