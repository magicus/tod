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
 * Non-static methods are not thread-safe, but {@link SocketCollector}
 * maintains one {@link CollectorPacketWriter} per thread.
 */
public class CollectorPacketWriter
{
	private final MyBuffer itsBuffer = new MyBuffer();
	private final DataOutputStream itsStream;
	
	RegisteredObjectsStack itsRegisteredObjectsStack = new RegisteredObjectsStack();
	DeferredObjectsStack itsDeferredObjectsStack = new DeferredObjectsStack();
	
	public CollectorPacketWriter(DataOutputStream aStream)
	{
		itsStream = aStream;
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
		
		if (aMessageType == MessageType.INSTANTIATION && shouldSendByValue(aTarget))
		{
			// Ensure that the sending of the object's value is deferred:
			// otherwise we serialize an object that is not completely
			// initialized (eg. new String(byteArray, 5, 10)).
			sendValue(itsBuffer, aTarget, aTimestamp);
		}
		else
		{
			sendValue(itsBuffer, aTarget);
		}
		
		sendArguments(itsBuffer, aArguments);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
	}
	
	/**
	 * Determines if the given object should be sent by value.
	 */
	private boolean shouldSendByValue(Object aObject)
	{
		return (aObject instanceof String) || (aObject instanceof Throwable);
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
		
		if (itsDeferredObjectsStack.isAvailable(aParentTimestamp))
		{
			DeferredObjectEntry theEntry = itsDeferredObjectsStack.pop();
			System.out.println("Sending deferred object: "+theEntry.id);
			sendRegisteredObject(theEntry.id, theEntry.object);
		}
		
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
		sendValue(aStream, aValue, -1);
	}
	
	private void sendValue (DataOutputStream aStream, Object aValue, long aDefer) throws IOException
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
		else if (shouldSendByValue(aValue))
		{
			sendObject(aStream, aValue, aDefer);
		}
		else
		{
			long theObjectId = ObjectIdentity.get(aValue);
			sendMessageType(aStream, MessageType.OBJECT_UID);
			aStream.writeLong(Math.abs(theObjectId));
		}
	}
	
	
	/**
	 * Sends an object by value. This method checks if the object already had an id.
	 * If it didn't, it is placed on the registered objects stack so that
	 * its value is sent when {@link #sendRegisteredObjects()} is called.
	 * In any case, the id of the object is sent.
	 * @param aDefer If positive, and if the object didn't have an id, the object is placed
	 * on a defered objects stack instead of the registered objects stack. The value of this
	 * parameter is the timestamp of the event that "request" the deferring.
	 */
	private void sendObject(DataOutputStream aStream, Object aObject, long aDefer) throws IOException
	{
		long theObjectId = ObjectIdentity.get(aObject);
		
		assert theObjectId != 0;
		if (theObjectId < 0)
		{
			// First time this object appears, register it.
			theObjectId = -theObjectId;
			if (aDefer == -1)
			{
				itsRegisteredObjectsStack.push(theObjectId, aObject);
				System.out.println("Registering: "+aObject+", id: "+theObjectId);
			}
			else
			{
				itsDeferredObjectsStack.push(aDefer, theObjectId, aObject);
				System.out.println("Deferring: "+aObject+", id: "+theObjectId+", p.ts: "+aDefer);
			}
		}
		
		sendMessageType(aStream, MessageType.OBJECT_UID);
		aStream.writeLong(theObjectId);
	}
	
	/**
	 * Sends all pending registered objects.
	 */
	private void sendRegisteredObjects() throws IOException
	{
		while (! itsRegisteredObjectsStack.isEmpty())
		{
			// Note: remember that this is thread-safe because SocketCollector has one
			// CollectorPacketWriter per thread.
			ObjectEntry theEntry = itsRegisteredObjectsStack.pop();
			sendRegisteredObject(theEntry.id, theEntry.object);
		}
	}
	
	private void sendRegisteredObject(long aId, Object aObject) throws IOException
	{
		sendMessageType(itsStream, MessageType.REGISTERED);
		itsBuffer.writeLong(aId);
		MyObjectOutputStream theStream = new MyObjectOutputStream(itsBuffer);
		theStream.writeObject(aObject);
		theStream.drain();
		
		System.out.println("Sent: "+aObject+", id: "+aId);
		
		itsBuffer.writeTo(itsStream);
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
	
	/**
	 * A stack of objects pending to be sent.
	 * @author gpothier
	 */
	private static class RegisteredObjectsStack
	{
		/**
		 * List of registered objects that must be sent.
		 * Note: There is space for a hard-coded number of entries that
		 * should "be enough for everybody". 
		 * Note that if only exception events are enabled this array
		 * will overflow.
		 */
		private final ObjectEntry[] itsObjects = new ObjectEntry[1024];
		
		/**
		 * Number of entries in {@link #itsObjects}.
		 */
		private int itsSize = 0;

		public RegisteredObjectsStack()
		{
			for (int i = 0; i < itsObjects.length; i++)
			{
				itsObjects[i] = new ObjectEntry();
			}
		}
		
		public void push(long aId, Object aObject)
		{
			itsObjects[itsSize++].set(aId, aObject);
		}
		
		public boolean isEmpty()
		{
			return itsSize == 0;
		}
		
		public ObjectEntry pop()
		{
			return itsObjects[--itsSize];
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

	/**
	 * A stack of objects whose registration is deferred.
	 * When we detect the instantiation of an object that is sent by value,
	 * 
	 * @author gpothier
	 */
	private static class DeferredObjectsStack
	{
		/**
		 * List of registered objects that must be sent.
		 * Note: There is space for a hard-coded number of entries that
		 * should "be enough for everybody". 
		 * Note that if only exception events are enabled this array
		 * will overflow.
		 */
		private final DeferredObjectEntry[] itsObjects = new DeferredObjectEntry[1024];
		
		/**
		 * Number of entries in {@link #itsObjects}.
		 */
		private int itsSize = 0;

		public DeferredObjectsStack()
		{
			for (int i = 0; i < itsObjects.length; i++)
			{
				itsObjects[i] = new DeferredObjectEntry();
			}
		}
		
		public void push(long aParentTimestamp, long aId, Object aObject)
		{
			itsObjects[itsSize++].set(aParentTimestamp, aId, aObject);
		}
		
		public boolean isEmpty()
		{
			return itsSize == 0;
		}
		
		/**
		 * Determines if the element at the top of the stack has the specified parent timestamp.
		 */
		public boolean isAvailable(long aParentTimestamp)
		{
			if (isEmpty()) return false;
			return itsObjects[itsSize-1].parentTimestamp == aParentTimestamp;
		}
		
		public DeferredObjectEntry pop()
		{
			return itsObjects[--itsSize];
		}
	}
	
	private static class DeferredObjectEntry
	{
		public long parentTimestamp;
		public long id;
		public Object object;
		
		public void set(long aParentTimestamp, long aId, Object aObject)
		{
			parentTimestamp = aParentTimestamp;
			id = aId;
			object = aObject;
		}
	}

}
