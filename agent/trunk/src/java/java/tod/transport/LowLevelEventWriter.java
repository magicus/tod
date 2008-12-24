/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package java.tod.transport;

import static java.tod._LowLevelEventType.AFTER_CALL;
import static java.tod._LowLevelEventType.AFTER_CALL_DRY;
import static java.tod._LowLevelEventType.AFTER_CALL_EXCEPTION;
import static java.tod._LowLevelEventType.ARRAY_WRITE;
import static java.tod._LowLevelEventType.BEFORE_CALL;
import static java.tod._LowLevelEventType.BEFORE_CALL_DRY;
import static java.tod._LowLevelEventType.BEHAVIOR_ENTER;
import static java.tod._LowLevelEventType.BEHAVIOR_EXIT;
import static java.tod._LowLevelEventType.BEHAVIOR_EXIT_EXCEPTION;
import static java.tod._LowLevelEventType.CLINIT_ENTER;
import static java.tod._LowLevelEventType.CLINIT_EXIT;
import static java.tod._LowLevelEventType.EXCEPTION_GENERATED;
import static java.tod._LowLevelEventType.FIELD_WRITE;
import static java.tod._LowLevelEventType.INSTANCEOF;
import static java.tod._LowLevelEventType.LOCAL_VARIABLE_WRITE;
import static java.tod._LowLevelEventType.NEW_ARRAY;
import static java.tod._LowLevelEventType.REGISTER_OBJECT;
import static java.tod._LowLevelEventType.REGISTER_THREAD;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.tod.EventCollector;
import java.tod.ExceptionGeneratedReceiver;
import java.tod.ObjectIdentity;
import java.tod.ObjectValueFactory;
import java.tod._BehaviorCallType;
import java.tod._LowLevelEventType;
import java.tod._Output;
import java.tod._ValueType;
import java.tod.transport.PacketBufferSender.PacketBuffer;

import tod.agent.Command;
import tod.agent.ObjectValue;

/**
 * Provides the methods used to encode streamed log data. Non-static methods are
 * not thread-safe, but {@link EventCollector} maintains one
 * {@link LowLevelEventWriter} per thread.
 */
public class LowLevelEventWriter
{
	private static final byte TRUE = (byte) 1;
	private static final byte FALSE = (byte) 0;
	
	/**
	 * The buffer to which packets must be sent. 
	 */
	private final PacketBuffer itsStream;
	
	/**
	 * Backing array for {@link #itsBuffer}
	 */
	private final byte[] itsBufferStore;
	
	/**
	 * Buffer to prepare (small) packets
	 */
	private final ByteBuffer itsBuffer;
	
	private RegisteredObjectsStack itsRegisteredObjectsStack = new RegisteredObjectsStack();
	private DeferredObjectsStack itsDeferredObjectsStack = new DeferredObjectsStack();
	private RegisteredRefObjectsStack itsRegisteredRefObjectsStack = new RegisteredRefObjectsStack();
	
	public LowLevelEventWriter(PacketBuffer aStream)
	{
		itsStream = aStream;
		itsBufferStore = new byte[4096];
		itsBuffer = ByteBuffer.wrap(itsBufferStore);
		itsBuffer.order(ByteOrder.nativeOrder());
	}

	private static void sendEventType(ByteBuffer aBuffer, _LowLevelEventType aType) 
	{
		aBuffer.put((byte) aType.ordinal());
	}

	private static void sendValueType(ByteBuffer aBuffer, _ValueType aType) 
	{
		aBuffer.put((byte) aType.ordinal());
	}
	
	private static void sendCommand(ByteBuffer aBuffer, Command aCommands) 
	{
		aBuffer.put((byte) (aCommands.ordinal() + Command.BASE));
	}
	
	private static void sendCallType(ByteBuffer aBuffer, _BehaviorCallType aType)
	{
		aBuffer.put((byte) aType.ordinal());
	}
	
	private static void sendString(ByteBuffer aBuffer, String aString)
	{
		int theSize = aString.length();
		aBuffer.putInt(theSize);
		for(int i=0;i<theSize;i++) aBuffer.putChar(aString.charAt(i));
	}
	
	private void sendStd(
			_LowLevelEventType aType,
			long aTimestamp)
	{
//		System.out.println("Starting packet: "+itsStream.getThreadId()+" - "+aTimestamp);
		sendEventType(itsBuffer, aType);
		itsBuffer.putLong(aTimestamp);
	}

	/**
	 * Copy the temporary buffer to the output stream, and resets the buffer.
	 */
	private void copyBuffer() 
	{
		itsStream.write(itsBufferStore, itsBuffer.position(), false);
		itsBuffer.clear();
	}
	
	public void sendClInitEnter(
			long aTimestamp,
			int aBehaviorId, 
			_BehaviorCallType aCallType) throws IOException
	{
		sendStd(CLINIT_ENTER, aTimestamp);

		itsBuffer.putInt(aBehaviorId);
		sendCallType(itsBuffer, aCallType);

		copyBuffer();

		sendRegisteredObjects();
	}
	
	public void sendBehaviorEnter(
			long aTimestamp,
			int aBehaviorId, 
			_BehaviorCallType aCallType,
			Object aTarget, 
			Object[] aArguments) throws IOException
	{
		sendStd(BEHAVIOR_ENTER, aTimestamp);

		itsBuffer.putInt(aBehaviorId);
		sendCallType(itsBuffer, aCallType);
		sendValue(itsBuffer, aTarget, aTimestamp);
		sendArguments(itsBuffer, aArguments, aTimestamp);

		copyBuffer();

		sendRegisteredObjects();

	}

	public void sendClInitExit(
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId) throws IOException
	{
		sendStd(CLINIT_EXIT, aTimestamp);

		itsBuffer.putInt(aProbeId);
		itsBuffer.putInt(aBehaviorId);

		copyBuffer();

		sendRegisteredObjects();
	}
	
	public void sendBehaviorExit(
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId,
			Object aResult) throws IOException
	{
		sendStd(BEHAVIOR_EXIT, aTimestamp);

		itsBuffer.putInt(aProbeId);
		itsBuffer.putInt(aBehaviorId);
		sendValue(itsBuffer, aResult, aTimestamp);

		copyBuffer();

		sendRegisteredObjects();
	}
	
	public void sendBehaviorExitWithException(
			long aTimestamp,
			int aBehaviorId, 
			Object aException) throws IOException
	{
		sendStd(BEHAVIOR_EXIT_EXCEPTION, aTimestamp);

		itsBuffer.putInt(aBehaviorId);
		sendValue(itsBuffer, aException, aTimestamp);

		copyBuffer();

		sendRegisteredObjects();
	}
	
	public void sendExceptionGenerated(
			long aTimestamp,
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature, 
			int aOperationBytecodeIndex,
			Object aException) throws IOException
	{
		sendStd(EXCEPTION_GENERATED, aTimestamp);

		sendString(itsBuffer, aMethodName);
		sendString(itsBuffer, aMethodSignature);
		sendString(itsBuffer, aMethodDeclaringClassSignature);
		itsBuffer.putShort((short) aOperationBytecodeIndex);
		sendValue(itsBuffer, aException, aTimestamp);

		copyBuffer();

		// There might be harmless recursive exceptions here, ignore
		// Note: this is only for exception events, other events don't need this.
		ExceptionGeneratedReceiver.setIgnoreExceptions(true);
		sendRegisteredObjects();
		ExceptionGeneratedReceiver.setIgnoreExceptions(false);
	}
	
	public void sendFieldWrite(
			long aTimestamp,
			int aProbeId, 
			int aFieldId,
			Object aTarget, 
			Object aValue) throws IOException
	{
		sendStd(FIELD_WRITE, aTimestamp);

		itsBuffer.putInt(aProbeId);
		itsBuffer.putInt(aFieldId);
		sendValue(itsBuffer, aTarget, aTimestamp);
		sendValue(itsBuffer, aValue, aTimestamp);

		copyBuffer();

		sendRegisteredObjects();
	}
	
	public void sendNewArray(
			long aTimestamp,
			int aProbeId, 
			Object aTarget,
			int aBaseTypeId,
			int aSize) throws IOException
	{
		sendStd(NEW_ARRAY, aTimestamp);

		itsBuffer.putInt(aProbeId);
		sendValue(itsBuffer, aTarget, aTimestamp);
		itsBuffer.putInt(aBaseTypeId);
		itsBuffer.putInt(aSize);

		copyBuffer();

		sendRegisteredObjects();
	}
	

	public void sendArrayWrite(
			long aTimestamp,
			int aProbeId, 
			Object aTarget,
			int aIndex,
			Object aValue) throws IOException
	{
		sendStd(ARRAY_WRITE, aTimestamp);

		itsBuffer.putInt(aProbeId);
		sendValue(itsBuffer, aTarget, aTimestamp);
		itsBuffer.putInt(aIndex);
		sendValue(itsBuffer, aValue, aTimestamp);

		copyBuffer();

		sendRegisteredObjects();
	}
	
	public void sendInstanceOf(
			long aTimestamp,
			int aProbeId, 
			Object aObject,
			int aTypeId,
			boolean aResult) throws IOException
	{
		sendStd(INSTANCEOF, aTimestamp);
		
		itsBuffer.putInt(aProbeId);
		sendValue(itsBuffer, aObject, aTimestamp);
		itsBuffer.putInt(aTypeId);
		itsBuffer.put(aResult ? TRUE : FALSE);
		
		copyBuffer();
		
		sendRegisteredObjects();
	}
	
	public void sendLocalVariableWrite(
			long aTimestamp,
			int aProbeId, 
			int aVariableId,
			Object aValue) throws IOException
	{
		sendStd(LOCAL_VARIABLE_WRITE, aTimestamp);

		itsBuffer.putInt(aProbeId);
		itsBuffer.putInt(aVariableId);
		sendValue(itsBuffer, aValue, aTimestamp);

		copyBuffer();

		sendRegisteredObjects();
	}
	
	public void sendBeforeBehaviorCallDry(
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId,
			_BehaviorCallType aCallType) throws IOException
	{
		sendStd(BEFORE_CALL_DRY, aTimestamp);

		itsBuffer.putInt(aProbeId);
		itsBuffer.putInt(aBehaviorId);
		sendCallType(itsBuffer, aCallType);

		copyBuffer();
	}
	
	public void sendAfterBehaviorCallDry(
			long aTimestamp) throws IOException
	{
		sendStd(AFTER_CALL_DRY, aTimestamp);

		copyBuffer();
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
			_BehaviorCallType aCallType, 
			Object aTarget) throws IOException
	{
		if (aCallType == _BehaviorCallType.INSTANTIATION && shouldSendByValue(aTarget))
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
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId,
			_BehaviorCallType aCallType,
			Object aTarget, 
			Object[] aArguments) throws IOException
	{
		sendStd(BEFORE_CALL, aTimestamp);

		itsBuffer.putInt(aProbeId);
		itsBuffer.putInt(aBehaviorId);
		sendCallType(itsBuffer, aCallType);
		sendTarget(aTimestamp, aBehaviorId, aCallType, aTarget);
		sendArguments(itsBuffer, aArguments, aTimestamp);
		
		copyBuffer();
		
		sendRegisteredObjects();
	}

	public void sendAfterBehaviorCall(
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId, 
			Object aTarget,
			Object aResult) throws IOException
	{
		sendStd(AFTER_CALL, aTimestamp);

		itsBuffer.putInt(aProbeId);
		itsBuffer.putInt(aBehaviorId);
		sendValue(itsBuffer, aTarget, aTimestamp);
		sendValue(itsBuffer, aResult, aTimestamp);

		copyBuffer();
		
		checkDeferred(aBehaviorId);
		sendRegisteredObjects();
	}
	
	public void sendAfterBehaviorCallWithException(
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId, 
			Object aTarget, 
			Object aException) throws IOException
	{
		sendStd(AFTER_CALL_EXCEPTION, aTimestamp);

		itsBuffer.putInt(aProbeId);
		itsBuffer.putInt(aBehaviorId);
		sendValue(itsBuffer, aTarget, aTimestamp);
		sendValue(itsBuffer, aException, aTimestamp);

		copyBuffer();
		
		checkDeferred(aBehaviorId);
		sendRegisteredObjects();

	}
	
	public void sendOutput(
			long aTimestamp,
			_Output aOutput, 
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

	/**
	 * Determines if the given object is indexable (see ILogCollector.register)
	 */
	private boolean isIndexable(Object aObject)
	{
		return (aObject instanceof String);
	}
	
	public void sendThread(
			long aJVMThreadId,
			String aName) throws IOException
	{
		sendEventType(itsBuffer, REGISTER_THREAD);

		itsBuffer.putLong(aJVMThreadId);
		sendString(itsBuffer, aName);

		copyBuffer();

		sendRegisteredObjects();
	}

	public void sendClear() 
	{
		sendCommand(itsBuffer, Command.DBCMD_CLEAR);
		copyBuffer();
	}

	public void sendFlush() 
	{
		sendCommand(itsBuffer, Command.DBCMD_FLUSH);
		copyBuffer();
	}
	
	public void sendEnd() 
	{
		sendCommand(itsBuffer, Command.DBCMD_END);
		copyBuffer();
	}
	
	public void sendEvCaptureEnabled(boolean aValue)
	{
		sendCommand(itsBuffer, Command.DBEV_CAPTURE_ENABLED);
		itsBuffer.put(aValue ? (byte) 1 : (byte) 0);
		copyBuffer();
	}
	
	/**
	 * Sends an argument to the socket. This method handles arrays, single
	 * objects or null values.
	 */
	private void sendArguments(
			ByteBuffer aBuffer, 
			Object[] aArguments, 
			long aTimestamp) throws IOException
	{
		aBuffer.putInt(aArguments != null ? aArguments.length : 0);

		if (aArguments != null) for (Object theArgument : aArguments)
			sendValue(aBuffer, theArgument, aTimestamp);
	}

	private void sendValue(ByteBuffer aBuffer, Object aValue, long aTimestamp) throws IOException
	{
		sendValue(aBuffer, aValue, aTimestamp, -1);
	}

	private void sendValue(ByteBuffer aBuffer, Object aValue, long aTimestamp, int aDeferRequestor) throws IOException
	{
		if (aValue == null)
		{
			sendValueType(aBuffer, _ValueType.NULL);
		}
		else if (aValue instanceof Boolean)
		{
			Boolean theBoolean = (Boolean) aValue;
			sendValueType(aBuffer, _ValueType.BOOLEAN);
			aBuffer.put(theBoolean.booleanValue() ? TRUE : FALSE);
		}
		else if (aValue instanceof Byte)
		{
			Byte theByte = (Byte) aValue;
			sendValueType(aBuffer, _ValueType.BYTE);
			aBuffer.put(theByte.byteValue());
		}
		else if (aValue instanceof Character)
		{
			Character theCharacter = (Character) aValue;
			sendValueType(aBuffer, _ValueType.CHAR);
			aBuffer.putChar(theCharacter.charValue());
		}
		else if (aValue instanceof Integer)
		{
			Integer theInteger = (Integer) aValue;
			sendValueType(aBuffer, _ValueType.INT);
			aBuffer.putInt(theInteger.intValue());
		}
		else if (aValue instanceof Long)
		{
			Long theLong = (Long) aValue;
			sendValueType(aBuffer, _ValueType.LONG);
			aBuffer.putLong(theLong.longValue());
		}
		else if (aValue instanceof Float)
		{
			Float theFloat = (Float) aValue;
			sendValueType(aBuffer, _ValueType.FLOAT);
			aBuffer.putFloat(theFloat.floatValue());
		}
		else if (aValue instanceof Double)
		{
			Double theDouble = (Double) aValue;
			sendValueType(aBuffer, _ValueType.DOUBLE);
			aBuffer.putDouble(theDouble.doubleValue());
		}
		else if (shouldSendByValue(aValue))
		{
			sendObjectByValue(aBuffer, aValue, aTimestamp, aDeferRequestor);
		}
		else
		{
			sendObjectByRef(aBuffer, aValue, aTimestamp);
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
	private void sendObjectByValue(ByteBuffer aBuffer, Object aObject, long aTimestamp, int aDeferRequestor) 
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

		sendValueType(aBuffer, _ValueType.OBJECT_UID);
		aBuffer.putLong(theObjectId);
	}

	/**
	 * Sends an object by reference. This method checks if the object already had an
	 * id. If it didn't, it is placed on the registered refs stack so that
	 * its type is sent when {@link #sendRegisteredObjects()} is called. In any
	 * case, the id of the object is sent.
	 */
	private void sendObjectByRef(ByteBuffer aBuffer, Object aObject, long aTimestamp) 
	{
		long theObjectId = ObjectIdentity.get(aObject);
		assert theObjectId != 0;
		
		if (theObjectId < 0)
		{
			// First time this object appears, register its type
			theObjectId = -theObjectId;
			Class<?> theClass = aObject.getClass();
			itsRegisteredRefObjectsStack.push(aObject, theObjectId, theClass, aTimestamp);
		}

		sendValueType(aBuffer, _ValueType.OBJECT_UID);
		aBuffer.putLong(theObjectId);
	}

	/**
	 * Sends all pending registered objects.
	 */
	private void sendRegisteredObjects() throws IOException
	{
		// Note: remember that this is thread-safe because SocketCollector has one
		// CollectorPacketWriter per thread.

		while (!itsRegisteredObjectsStack.isEmpty())
		{
			ObjectEntry theEntry = itsRegisteredObjectsStack.pop();
			sendRegisteredObject(theEntry.id, theEntry.object, theEntry.timestamp);
		}
		
		while (!itsRegisteredRefObjectsStack.isEmpty())
		{
			RefObjectEntry theEntry = itsRegisteredRefObjectsStack.pop();
			sendRegisteredRefObject(theEntry.object, theEntry.id, theEntry.cls, theEntry.timestamp);
		}

	}

	private void sendRegisteredObject(long aId, Object aObject, long aTimestamp) throws IOException
	{
		// We use a different buffer here because the packet might be huge.
		ByteArrayOutputStream theBuffer = new ByteArrayOutputStream();
		
		// Write header placeholder
		theBuffer.write(itsBufferStore, 0, 22);
		
		ObjectOutputStream theObjectOut = new ObjectOutputStream(theBuffer);
		theObjectOut.writeObject(ObjectValueFactory.ensurePortable(aObject));
		theObjectOut.flush();
		
		int theSize = theBuffer.size()-5; // 5: event type + size 
	
		sendEventType(itsBuffer, REGISTER_OBJECT);
		itsBuffer.putInt(theSize); 
		
		itsBuffer.putLong(aId);
		itsBuffer.putLong(aTimestamp);
		itsBuffer.put(isIndexable(aObject) ? (byte) 1 : (byte) 0);
		
		byte[] theArray = theBuffer.toByteArray();
		System.arraycopy(itsBufferStore, 0, theArray, 0, 22);
		itsBuffer.clear();

		itsStream.write(theArray, theArray.length, true);
	}
	
	private void sendRegisteredRefObject(Object aObject, long aId, Class<?> aClass, long aTimestamp) throws IOException
	{
		// That must stay before we start using the buffer
		if (aClass == Class.class)
		{
			// We have to register it explicitly now otherwise the system thinks it
			// is already registered because it has an id.
			sendRegisterClass(aId, (Class<?>) aObject);
		}
		long theClassId = getClassId(aClass); 
		
		sendEventType(itsBuffer, _LowLevelEventType.REGISTER_REFOBJECT);
		
		itsBuffer.putLong(aId);
		itsBuffer.putLong(aTimestamp);
		itsBuffer.putLong(theClassId);
		
		copyBuffer();
	}
	
	private long getClassId(Class<?> aClass) throws IOException
	{
		long theId = ObjectIdentity.get(aClass);
		assert theId != 0;
		
		if (theId < 0)
		{
			theId = -theId;
			sendRegisterClass(theId, aClass);
		}
		
		return theId;
	}
	
	private void sendRegisterClass(long aClassId, Class<?> aClass) throws IOException
	{
		// That must stay before we start using the buffer
		long theLoaderId = getClassLoaderId(aClass.getClassLoader());
		
		sendEventType(itsBuffer, _LowLevelEventType.REGISTER_CLASS);
		
		itsBuffer.putLong(aClassId);
		itsBuffer.putLong(theLoaderId);
		
		String theName = aClass.getName();
		itsBuffer.putShort((short) theName.length());
		for(int i=0;i<theName.length();i++) itsBuffer.putChar(theName.charAt(i));
		
		copyBuffer();
	}
	
	private long getClassLoaderId(ClassLoader aLoader) throws IOException
	{
		if (aLoader == null) return 0;
		
		long theId = ObjectIdentity.get(aLoader);
		assert theId != 0;
		
		if (theId < 0)
		{
			theId = -theId;
			sendRegisterClassLoader(theId, aLoader);
		}
		
		return theId;
	}
	
	private void sendRegisterClassLoader(long aLoaderId, ClassLoader aLoader) throws IOException
	{
		// That must stay before we start using the buffer
		long theLoaderClassId = getClassId(aLoader.getClass());
		
		sendEventType(itsBuffer, _LowLevelEventType.REGISTER_CLASSLOADER);
		
		itsBuffer.putLong(aLoaderId);
		itsBuffer.putLong(theLoaderClassId);
		
		copyBuffer();
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
		 * everybody".
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
				System.out.println("---------TOD---------WARNING");
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
		 * everybody". 
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

	/**
	 * A stack of newly created objects whose type must be registered
	 * @author gpothier
	 */
	private static class RegisteredRefObjectsStack
	{
		/**
		 * List of registered objects that must be sent.
		 */
		private final RefObjectEntry[] itsObjects = new RefObjectEntry[1024];

		/**
		 * Number of entries in {@link #itsObjects}.
		 */
		private int itsSize = 0;

		public RegisteredRefObjectsStack()
		{
			for (int i = 0; i < itsObjects.length; i++)
			{
				itsObjects[i] = new RefObjectEntry();
			}
		}

		public void push(Object aObject, long aId, Class<?> aClass, long aTimestamp)
		{
			itsObjects[itsSize++].set(aObject, aId, aClass, aTimestamp);
		}

		public boolean isEmpty()
		{
			return itsSize == 0;
		}

		public RefObjectEntry pop()
		{
			return itsObjects[--itsSize];
		}
	}

	private static class RefObjectEntry
	{
		public Object object;
		public long id;
		public Class<?> cls;
		public long timestamp;

		public void set(Object aObject, long aId, Class<?> aClass, long aTimestamp)
		{
			object = aObject;
			id = aId;
			cls = aClass;
			timestamp = aTimestamp;
		}
	}


}
