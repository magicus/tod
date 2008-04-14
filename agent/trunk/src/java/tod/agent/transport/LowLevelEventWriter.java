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
package tod.agent.transport;

import static tod.agent.transport.LowLevelEventType.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;

import tod.agent.BehaviorCallType;
import tod.agent.EventCollector;
import tod.agent.FixedIntStack;
import tod.agent.ObjectIdentity;
import tod.agent.Output;

/**
 * Provides the methods used to encode streamed log data. Non-static methods are
 * not thread-safe, but {@link EventCollector} maintains one
 * {@link LowLevelEventWriter} per thread.
 */
public class LowLevelEventWriter
{
	private final MyBuffer itsBuffer = new MyBuffer();
	private final DataOutputStream itsStream;
	
	private RegisteredObjectsStack itsRegisteredObjectsStack = new RegisteredObjectsStack();
	private DeferredObjectsStack itsDeferredObjectsStack = new DeferredObjectsStack();
	
	/**
	 * A stack of behavior ids. Each time a "before call" event requests a deferred
	 * value send, the id of the called behavior is pushed onto this stack.
	 */
	private FixedIntStack itsDeferredRequestorsStack = new FixedIntStack(10);
	
	public LowLevelEventWriter(DataOutputStream aStream)
	{
		itsStream = aStream;
	}

	private static void sendEventType(DataOutputStream aStream, LowLevelEventType aType) throws IOException
	{
		aStream.writeByte(aType.ordinal());
	}

	private static void sendValueType(DataOutputStream aStream, ValueType aType) throws IOException
	{
		aStream.writeByte(aType.ordinal());
	}
	
	private static void sendCommand(DataOutputStream aStream, Commands aCommands) throws IOException
	{
		aStream.writeByte(aCommands.ordinal() + Commands.BASE);
	}
	
	private static void sendCallType(DataOutputStream aStream, BehaviorCallType aType) throws IOException
	{
		aStream.writeByte(aType.ordinal());
	}
	
	private void sendStd(
			LowLevelEventType aType,
			int aThreadId, 
			long aTimestamp) throws IOException
	{
		sendEventType(itsStream, aType);

		itsBuffer.writeInt(aThreadId);
		itsBuffer.writeLong(aTimestamp);
	}

	
	
	
	
	
	public void sendClInitEnter(
			int aThreadId,
			long aTimestamp,
			int aBehaviorId, 
			BehaviorCallType aCallType) throws IOException
	{
		sendStd(CLINIT_ENTER, aThreadId, aTimestamp);

		itsBuffer.writeInt(aBehaviorId);
		sendCallType(itsBuffer, aCallType);

		itsBuffer.writeTo(itsStream);

		sendRegisteredObjects();
	}
	
	public void sendBehaviorEnter(
			int aThreadId,
			long aTimestamp,
			int aBehaviorId, 
			BehaviorCallType aCallType,
			Object aTarget, 
			Object[] aArguments) throws IOException
	{
		sendStd(BEHAVIOR_ENTER, aThreadId, aTimestamp);

		itsBuffer.writeInt(aBehaviorId);
		sendCallType(itsBuffer, aCallType);
		sendValue(itsBuffer, aTarget, aTimestamp);
		sendArguments(itsBuffer, aArguments, aTimestamp);

		itsBuffer.writeTo(itsStream);

		sendRegisteredObjects();

	}

	public void sendClInitExit(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId) throws IOException
	{
		sendStd(CLINIT_EXIT, aThreadId, aTimestamp);

		itsBuffer.writeInt(aProbeId);
		itsBuffer.writeInt(aBehaviorId);

		itsBuffer.writeTo(itsStream);

		sendRegisteredObjects();
	}
	
	public void sendBehaviorExit(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId,
			Object aResult) throws IOException
	{
		sendStd(BEHAVIOR_EXIT, aThreadId, aTimestamp);

		itsBuffer.writeInt(aProbeId);
		itsBuffer.writeInt(aBehaviorId);
		sendValue(itsBuffer, aResult, aTimestamp);

		itsBuffer.writeTo(itsStream);

		sendRegisteredObjects();
	}
	
	public void sendBehaviorExitWithException(
			int aThreadId,
			long aTimestamp,
			int aBehaviorId, 
			Object aException) throws IOException
	{
		sendStd(BEHAVIOR_EXIT_EXCEPTION, aThreadId, aTimestamp);

		itsBuffer.writeInt(aBehaviorId);
		sendValue(itsBuffer, aException, aTimestamp);

		itsBuffer.writeTo(itsStream);

		sendRegisteredObjects();
	}
	
	public void sendExceptionGenerated(
			int aThreadId,
			long aTimestamp,
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature, 
			int aOperationBytecodeIndex,
			Object aException) throws IOException
	{
		sendStd(EXCEPTION_GENERATED, aThreadId, aTimestamp);

		itsBuffer.writeUTF(aMethodName);
		itsBuffer.writeUTF(aMethodSignature);
		itsBuffer.writeUTF(aMethodDeclaringClassSignature);
		itsBuffer.writeShort(aOperationBytecodeIndex);
		sendValue(itsBuffer, aException, aTimestamp);

		itsBuffer.writeTo(itsStream);

		// We don't send registered objects here because it seems to cause
		// a bad interaction with the native side.
		// sendRegisteredObjects();
	}
	
	public void sendFieldWrite(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			int aFieldId,
			Object aTarget, 
			Object aValue) throws IOException
	{
		sendStd(FIELD_WRITE, aThreadId, aTimestamp);

		itsBuffer.writeInt(aProbeId);
		itsBuffer.writeInt(aFieldId);
		sendValue(itsBuffer, aTarget, aTimestamp);
		sendValue(itsBuffer, aValue, aTimestamp);

		itsBuffer.writeTo(itsStream);

		sendRegisteredObjects();
	}
	
	public void sendNewArray(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			Object aTarget,
			int aBaseTypeId,
			int aSize) throws IOException
	{
		sendStd(NEW_ARRAY, aThreadId, aTimestamp);

		itsBuffer.writeInt(aProbeId);
		sendValue(itsBuffer, aTarget, aTimestamp);
		itsBuffer.writeInt(aBaseTypeId);
		itsBuffer.writeInt(aSize);

		itsBuffer.writeTo(itsStream);

		sendRegisteredObjects();
	}
	

	public void sendArrayWrite(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			Object aTarget,
			int aIndex,
			Object aValue) throws IOException
	{
		sendStd(ARRAY_WRITE, aThreadId, aTimestamp);

		itsBuffer.writeInt(aProbeId);
		sendValue(itsBuffer, aTarget, aTimestamp);
		itsBuffer.writeInt(aIndex);
		sendValue(itsBuffer, aValue, aTimestamp);

		itsBuffer.writeTo(itsStream);

		sendRegisteredObjects();
	}
	
	public void sendInstanceOf(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			Object aObject,
			int aTypeId,
			boolean aResult) throws IOException
	{
		sendStd(INSTANCEOF, aThreadId, aTimestamp);
		
		itsBuffer.writeInt(aProbeId);
		sendValue(itsBuffer, aObject, aTimestamp);
		itsBuffer.writeInt(aTypeId);
		itsBuffer.writeBoolean(aResult);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
	}
	
	public void sendLocalVariableWrite(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			int aVariableId,
			Object aValue) throws IOException
	{
		sendStd(LOCAL_VARIABLE_WRITE, aThreadId, aTimestamp);

		itsBuffer.writeInt(aProbeId);
		itsBuffer.writeInt(aVariableId);
		sendValue(itsBuffer, aValue, aTimestamp);

		itsBuffer.writeTo(itsStream);

		sendRegisteredObjects();
	}
	
	public void sendBeforeBehaviorCallDry(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId,
			BehaviorCallType aCallType) throws IOException
	{
		sendStd(BEFORE_CALL_DRY, aThreadId, aTimestamp);

		itsBuffer.writeInt(aProbeId);
		itsBuffer.writeInt(aBehaviorId);
		sendCallType(itsBuffer, aCallType);

		itsBuffer.writeTo(itsStream);
	}
	
	public void sendAfterBehaviorCallDry(
			int aThreadId,
			long aTimestamp) throws IOException
	{
		sendStd(AFTER_CALL_DRY, aThreadId, aTimestamp);

		itsBuffer.writeTo(itsStream);
	}
	


	/**
	 * Sends the target object of a call event.
	 * Ensures that the sending of the object's value is deferred in 
	 * the case of instantiations:
	 * otherwise we serialize an object that is not completely
	 * initialized (eg. new String(byteArray, 5, 10)).
	 */
	private void sendTarget(
			long aTimestamp,
			int aDeferRequestorId,
			BehaviorCallType aCallType, 
			Object aTarget) throws IOException
	{
		if (aCallType == BehaviorCallType.INSTANTIATION && shouldSendByValue(aTarget))
		{
			// Ensure that the sending of the object's value is deferred:
			// otherwise we serialize an object that is not completely
			// initialized (eg. new String(byteArray, 5, 10)).
			sendValue(itsBuffer, aTarget, aTimestamp, aDeferRequestorId);
		}
		else
		{
			sendValue(itsBuffer, aTarget, aTimestamp);
		}
	}

	/**
	 * For exit/after events, check if there is a deferred entry corresponding to
	 * the given parent timestamp.
	 */
	private void checkDeferred(int aDeferRequestorId) throws IOException
	{
		if (itsDeferredObjectsStack.isAvailable(aDeferRequestorId))
		{
			DeferredObjectEntry theEntry = itsDeferredObjectsStack.pop();
			// System.out.println("Sending deferred object: "+theEntry.id);
			sendRegisteredObject(theEntry.id, theEntry.object, theEntry.timestamp);
		}
	}

	
	public void sendBeforeBehaviorCall(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId,
			BehaviorCallType aCallType,
			Object aTarget, 
			Object[] aArguments) throws IOException
	{
		sendStd(BEFORE_CALL, aThreadId, aTimestamp);

		itsBuffer.writeInt(aProbeId);
		itsBuffer.writeInt(aBehaviorId);
		sendCallType(itsBuffer, aCallType);
		sendTarget(aTimestamp, aBehaviorId, aCallType, aTarget);
		sendArguments(itsBuffer, aArguments, aTimestamp);
		
		itsBuffer.writeTo(itsStream);
		
		sendRegisteredObjects();
	}

	public void sendAfterBehaviorCall(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId, 
			Object aTarget,
			Object aResult) throws IOException
	{
		sendStd(AFTER_CALL, aThreadId, aTimestamp);

		itsBuffer.writeInt(aProbeId);
		itsBuffer.writeInt(aBehaviorId);
		sendValue(itsBuffer, aTarget, aTimestamp);
		sendValue(itsBuffer, aResult, aTimestamp);

		itsBuffer.writeTo(itsStream);
		
		checkDeferred(aBehaviorId);
		sendRegisteredObjects();
	}
	
	public void sendAfterBehaviorCallWithException(
			int aThreadId,
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId, 
			Object aTarget, 
			Object aException) throws IOException
	{
		sendStd(AFTER_CALL_EXCEPTION, aThreadId, aTimestamp);

		itsBuffer.writeInt(aProbeId);
		itsBuffer.writeInt(aBehaviorId);
		sendValue(itsBuffer, aTarget, aTimestamp);
		sendValue(itsBuffer, aException, aTimestamp);

		itsBuffer.writeTo(itsStream);
		
		checkDeferred(aBehaviorId);
		sendRegisteredObjects();

	}
	
	public void sendOutput(
			int aThreadId,
			long aTimestamp,
			Output aOutput, 
			byte[] aData) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Determines if the given object should be sent by value.
	 */
	private boolean shouldSendByValue(Object aObject)
	{
		return (aObject instanceof String) || (aObject instanceof Throwable);
	}

	public void sendThread(
			int aThreadId, 
			long aJVMThreadId,
			String aName) throws IOException
	{
		sendEventType(itsStream, REGISTER_THREAD);

		itsBuffer.writeInt(aThreadId);
		itsBuffer.writeLong(aJVMThreadId);
		itsBuffer.writeUTF(aName);

		itsBuffer.writeTo(itsStream);

		sendRegisteredObjects();
	}

	public void sendClear() throws IOException
	{
		sendCommand(itsStream, Commands.CMD_CLEAR);
	}

	/**
	 * Sends an argument to the socket. This method handles arrays, single
	 * objects or null values.
	 */
	private void sendArguments(
			DataOutputStream aStream, 
			Object[] aArguments, 
			long aTimestamp) throws IOException
	{
		aStream.writeInt(aArguments != null ? aArguments.length : 0);

		if (aArguments != null) for (Object theArgument : aArguments)
			sendValue(aStream, theArgument, aTimestamp);
	}

	private void sendValue(DataOutputStream aStream, Object aValue, long aTimestamp) throws IOException
	{
		sendValue(aStream, aValue, aTimestamp, -1);
	}

	private void sendValue(DataOutputStream aStream, Object aValue, long aTimestamp, int aDeferRequestor) throws IOException
	{
		if (aValue == null)
		{
			sendValueType(aStream, ValueType.NULL);
		}
		else if (aValue instanceof Boolean)
		{
			Boolean theBoolean = (Boolean) aValue;
			sendValueType(aStream, ValueType.BOOLEAN);
			aStream.writeByte(theBoolean.booleanValue() ? 1 : 0);
		}
		else if (aValue instanceof Byte)
		{
			Byte theByte = (Byte) aValue;
			sendValueType(aStream, ValueType.BYTE);
			aStream.writeByte(theByte.byteValue());
		}
		else if (aValue instanceof Character)
		{
			Character theCharacter = (Character) aValue;
			sendValueType(aStream, ValueType.CHAR);
			aStream.writeChar(theCharacter.charValue());
		}
		else if (aValue instanceof Integer)
		{
			Integer theInteger = (Integer) aValue;
			sendValueType(aStream, ValueType.INT);
			aStream.writeInt(theInteger.intValue());
		}
		else if (aValue instanceof Long)
		{
			Long theLong = (Long) aValue;
			sendValueType(aStream, ValueType.LONG);
			aStream.writeLong(theLong.longValue());
		}
		else if (aValue instanceof Float)
		{
			Float theFloat = (Float) aValue;
			sendValueType(aStream, ValueType.FLOAT);
			aStream.writeFloat(theFloat.floatValue());
		}
		else if (aValue instanceof Double)
		{
			Double theDouble = (Double) aValue;
			sendValueType(aStream, ValueType.DOUBLE);
			aStream.writeDouble(theDouble.doubleValue());
		}
		else if (shouldSendByValue(aValue))
		{
			sendObject(aStream, aValue, aTimestamp, aDeferRequestor);
		}
		else
		{
			long theObjectId = ObjectIdentity.get(aValue);
			sendValueType(aStream, ValueType.OBJECT_UID);
			aStream.writeLong(Math.abs(theObjectId));
		}
	}

	/**
	 * Sends an object by value. This method checks if the object already had an
	 * id. If it didn't, it is placed on the registered objects stack so that
	 * its value is sent when {@link #sendRegisteredObjects()} is called. In any
	 * case, the id of the object is sent.
	 * 
	 * @param aDeferRequestor
	 *            If positive, and if the object didn't have an id, the object
	 *            is placed on a defered objects stack instead of the registered
	 *            objects stack. The value of this parameter is the behavior id of
	 *            the event that "request" the deferring.
	 */
	private void sendObject(DataOutputStream aStream, Object aObject, long aTimestamp, int aDeferRequestor) throws IOException
	{
		long theObjectId = ObjectIdentity.get(aObject);

		assert theObjectId != 0;
		if (theObjectId < 0)
		{
			// First time this object appears, register it.
			theObjectId = -theObjectId;
			if (aDeferRequestor == -1)
			{
				// add the time stamp for flushing purpose in ObjectDatabase
				itsRegisteredObjectsStack.push(theObjectId, aObject, aTimestamp);
				// System.out.println("Registering: "+aObject+", id:
				// "+theObjectId);
			}
			else
			{
				// add the time stamp for flushing purpose in ObjectDatabase
				itsDeferredObjectsStack.push(aDeferRequestor, theObjectId, aObject, aTimestamp);
				// System.out.println("Deferring: "+aObject+", id:
				// "+theObjectId+", p.ts: "+aDefer);
			}
		}

		sendValueType(aStream, ValueType.OBJECT_UID);
		aStream.writeLong(theObjectId);
	}

	/**
	 * Sends all pending registered objects.
	 */
	private void sendRegisteredObjects() throws IOException
	{
		while (!itsRegisteredObjectsStack.isEmpty())
		{
			// Note: remember that this is thread-safe because SocketCollector has one
			// CollectorPacketWriter per thread.
			ObjectEntry theEntry = itsRegisteredObjectsStack.pop();
			sendRegisteredObject(theEntry.id, theEntry.object, theEntry.timestamp);
		}
	}

	private void sendRegisteredObject(long aId, Object aObject, long aTimestamp) throws IOException
	{
		sendCommand(itsStream, Commands.CMD_REGISTER);
		itsBuffer.writeLong(aId);
		itsBuffer.writeLong(aTimestamp);
		MyObjectOutputStream theStream = new MyObjectOutputStream(itsBuffer);
		theStream.writeObject(ObjectValue.ensurePortable(aObject));
		theStream.drain();
		// System.out.println("Sent: "+aObject+", id: "+aId);
		itsBuffer.writeTo(itsStream);
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
	 * 
	 * @author gpothier
	 */
	private static class MyBuffer extends DataOutputStream
	{
		public MyBuffer()
		{
			super(new ByteArrayOutputStream());
		}

		/**
		 * Writes the size of the buffer and its content to the given stream.
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
	 * 
	 * @author gpothier
	 */
	private static class RegisteredObjectsStack
	{
		/**
		 * List of registered objects that must be sent. Note: There is space
		 * for a hard-coded number of entries that should "be enough for
		 * everybody". Note that if only exception events are enabled this array
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

		public void push(long aId, Object aObject, long aTimestamp)
		{
			//TODO remove this 
			if (itsSize>= itsObjects.length) {
				System.out.println("---------WARNING");
				for (int theI = 0; theI < itsObjects.length; theI++)
					System.out.print(itsObjects[theI].object.getClass() +" ");
			}
			
			itsObjects[itsSize++].set(aId, aObject, aTimestamp);
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
		public long timestamp;

		public void set(long aId, Object aObject, long aTimestamp)
		{
			id = aId;
			object = aObject;
			timestamp = aTimestamp;
		}
	}

	/**
	 * A stack of objects whose registration is deferred. When we detect the
	 * instantiation of an object that is sent by value,
	 * 
	 * @author gpothier
	 */
	private static class DeferredObjectsStack
	{
		/**
		 * List of registered objects that must be sent. Note: There is space
		 * for a hard-coded number of entries that should "be enough for
		 * everybody". Note that if only exception events are enabled this array
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

		public void push(int aRequestorId, long aId, Object aObject, long aTimestamp)
		{
			itsObjects[itsSize++].set(aRequestorId, aId, aObject, aTimestamp);
		}

		public boolean isEmpty()
		{
			return itsSize == 0;
		}

		/**
		 * Determines if the element at the top of the stack has the specified
		 * requestor id.
		 */
		public boolean isAvailable(int aRequestorId)
		{
			if (isEmpty()) return false;
			return itsObjects[itsSize - 1].requestorId == aRequestorId;
		}

		public DeferredObjectEntry pop()
		{
			return itsObjects[--itsSize];
		}
	}

	private static class DeferredObjectEntry
	{
		public int requestorId;
		public long id;
		public Object object;
		public long timestamp;

		public void set(int aRequestorId, long aId, Object aObject, long aTimestamp)
		{
			requestorId = aRequestorId;
			id = aId;
			object = aObject;
			timestamp = aTimestamp;
		}
	}

}
