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

import tod.core.BehaviorKind;
import tod.core.ObjectIdentity;
import tod.core.Output;
import static tod.core.ILocationRegistrer.LineNumberInfo;
import static tod.core.ILocationRegistrer.LocalVariableInfo;

/**
 * Provides the methods used to encode streamed log data.
 */
public class CollectorPacketWriter
{
	private static final ThreadLocal<MyBuffer> itsBuffers = new ThreadLocal<MyBuffer>()
	{
		@Override
		protected MyBuffer initialValue()
		{
			return new MyBuffer();
		}
	};
	
	private static void sendMethodCall(
			DataOutputStream aStream,
			MessageType aMessageType,
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments) throws IOException
	{
		sendMessageType(aStream, aMessageType);

		MyBuffer theBuffer = itsBuffers.get();
		
		sendStd(theBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		theBuffer.writeInt(aOperationBytecodeIndex);
		theBuffer.writeBoolean(aDirectParent);
		theBuffer.writeInt(aCalledBehavior);
		theBuffer.writeInt(aExecutedBehavior);
		sendValue(theBuffer, aTarget);
		sendArguments(theBuffer, aArguments);
		
		theBuffer.writeTo(aStream);
	}
	
	public static void sendMethodCall(
			DataOutputStream aStream,
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments) throws IOException
	{
		sendMethodCall(
				aStream,
				MessageType.METHOD_CALL,
				aThreadId, 
				aParentTimestamp,
				aDepth,
				aTimestamp, 
				aOperationBytecodeIndex,
				aDirectParent, 
				aCalledBehavior,
				aExecutedBehavior, 
				aTarget,
				aArguments);
	}
	
	public static void sendInstantiation(
			DataOutputStream aStream,
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments) throws IOException
	{
		sendMethodCall(
				aStream,
				MessageType.INSTANTIATION,
				aThreadId, 
				aParentTimestamp,
				aDepth,
				aTimestamp, 
				aOperationBytecodeIndex,
				aDirectParent, 
				aCalledBehavior,
				aExecutedBehavior, 
				aTarget,
				aArguments);
	}
	
	public static void sendSuperCall(
			DataOutputStream aStream,
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments) throws IOException
	{
		sendMethodCall(
				aStream,
				MessageType.SUPER_CALL,
				aThreadId, 
				aParentTimestamp,
				aDepth,
				aTimestamp, 
				aOperationBytecodeIndex,
				aDirectParent, 
				aCalledBehavior,
				aExecutedBehavior, 
				aTarget,
				aArguments);
	}
	
	public static void sendBehaviorExit(
			DataOutputStream aStream,
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			int aBehaviorId,
			boolean aHasThrown,
			Object aResult) throws IOException
	{
		sendMessageType(aStream, MessageType.BEHAVIOR_EXIT);

		MyBuffer theBuffer = itsBuffers.get();
		
		sendStd(theBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		theBuffer.writeInt(aOperationBytecodeIndex);
		theBuffer.writeInt(aBehaviorId);
		theBuffer.writeBoolean(aHasThrown);
		sendValue(theBuffer, aResult);
		
		theBuffer.writeTo(aStream);
	}
	
	public static void sendFieldWrite(
			DataOutputStream aStream,
			int aThreadId,
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			int aFieldLocationId,
			Object aTarget,
			Object aValue) throws IOException
	{
		sendMessageType(aStream, MessageType.FIELD_WRITE);

		MyBuffer theBuffer = itsBuffers.get();
		
		sendStd(theBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		theBuffer.writeInt(aOperationBytecodeIndex);
		theBuffer.writeInt(aFieldLocationId);
		sendValue(theBuffer, aTarget);
		sendValue(theBuffer, aValue);
		
		theBuffer.writeTo(aStream);
	}
	
	public static void sendArrayWrite(
			DataOutputStream aStream,
			int aThreadId,
			long aParentTimestamp,
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			Object aTarget,
			int aIndex,
			Object aValue) throws IOException
	{
		sendMessageType(aStream, MessageType.ARRAY_WRITE);

		MyBuffer theBuffer = itsBuffers.get();
		
		sendStd(theBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		theBuffer.writeInt(aOperationBytecodeIndex);
		sendValue(theBuffer, aTarget);
		theBuffer.writeInt(aIndex);
		sendValue(theBuffer, aValue);
		
		theBuffer.writeTo(aStream);
	}
	
	public static void sendLocalWrite(
			DataOutputStream aStream,
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp,
			int aOperationBytecodeIndex,
			int aVariableId,
			Object aValue) throws IOException
	{
		sendMessageType(aStream, MessageType.LOCAL_VARIABLE_WRITE);

		MyBuffer theBuffer = itsBuffers.get();
		
		sendStd(theBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		theBuffer.writeInt(aOperationBytecodeIndex);
		theBuffer.writeInt(aVariableId);
		sendValue(theBuffer, aValue);
		
		theBuffer.writeTo(aStream);
	}
	
	public static void sendException(
			DataOutputStream aStream,
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
		sendMessageType(aStream, MessageType.EXCEPTION);

		MyBuffer theBuffer = itsBuffers.get();
		
		sendStd(theBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		theBuffer.writeUTF(aMethodName);
		theBuffer.writeUTF(aMethodSignature);
		theBuffer.writeUTF(aMethodDeclaringClassSignature);
		theBuffer.writeInt(aOperationBytecodeIndex);
		sendValue(theBuffer, aException);
		
		theBuffer.writeTo(aStream);
	}
	
	public static void sendOutput(
			DataOutputStream aStream,
			int aThreadId, 
			long aParentTimestamp,
			int aDepth,
			long aTimestamp,
			Output aOutput,
			byte[] aData) throws IOException
	{
		sendMessageType(aStream, MessageType.OUTPUT);

		MyBuffer theBuffer = itsBuffers.get();
		
		sendStd(theBuffer, aThreadId, aParentTimestamp, aDepth, aTimestamp);
		theBuffer.writeByte((byte) aOutput.ordinal());
		theBuffer.writeInt(aData.length);
		theBuffer.write(aData);
		
		theBuffer.writeTo(aStream);
	}
	
	public static void sendThread(
			DataOutputStream aStream,
			int aThreadId, 
			long aJVMThreadId,
			String aName) throws IOException
	{
		sendMessageType(aStream, MessageType.REGISTER_THREAD);

		MyBuffer theBuffer = itsBuffers.get();
		
		theBuffer.writeInt(aThreadId);
		theBuffer.writeLong(aJVMThreadId);
		theBuffer.writeUTF(aName);
		
		theBuffer.writeTo(aStream);
	}

	public static void sendRegisterType(
			DataOutputStream aStream,
			int aTypeId,
			String aTypeName,
			int aSupertypeId, 
			int[] aInterfaceIds) throws IOException
	{
		sendMessageType(aStream, MessageType.REGISTER_CLASS);
		aStream.writeInt(aTypeId);
		aStream.writeUTF(aTypeName);
		aStream.writeInt(aSupertypeId);
		aStream.writeByte(aInterfaceIds != null ? (byte) aInterfaceIds.length : 0);
		if (aInterfaceIds != null) for (int theId : aInterfaceIds) aStream.writeInt(theId);
	}
	
    
    public static void sendRegisterBehavior(
			DataOutputStream aStream,
    		BehaviorKind aBehaviourType,
            int aBehaviourId, 
            int aTypeId,
            String aBehaviourName,
            String aSignature) throws IOException
    {
		sendMessageType(aStream, MessageType.REGISTER_BEHAVIOR);
		aStream.writeByte((byte) aBehaviourType.ordinal());
		aStream.writeInt(aBehaviourId);
		aStream.writeInt(aTypeId);
		aStream.writeUTF(aBehaviourName);
		aStream.writeUTF(aSignature);
	}
	
    public static void sendRegisterBehaviorAttributes(
			DataOutputStream aStream,
    		int aBehaviourId, 
    		LineNumberInfo[] aLineNumberTable, 
    		LocalVariableInfo[] aLocalVariableTable) throws IOException
    {
		sendMessageType(aStream, MessageType.REGISTER_BEHAVIOR_ATTRIBUTES);
		aStream.writeInt(aBehaviourId);
		
		// Send line number table
		aStream.writeInt(aLineNumberTable != null ? aLineNumberTable.length : 0);
		if (aLineNumberTable != null) for (LineNumberInfo theLineNumberInfo : aLineNumberTable)
		{
			aStream.writeShort(theLineNumberInfo.getStartPc());
			aStream.writeShort(theLineNumberInfo.getLineNumber());
		}
		
		// Send local variable table
		aStream.writeInt(aLocalVariableTable != null ? aLocalVariableTable.length : 0);
		if (aLocalVariableTable != null) for (LocalVariableInfo theLocalVariableInfo : aLocalVariableTable)
		{
			aStream.writeShort(theLocalVariableInfo.getStartPc());
			aStream.writeShort(theLocalVariableInfo.getLength());
			aStream.writeUTF(theLocalVariableInfo.getVariableName());
			aStream.writeUTF(theLocalVariableInfo.getVariableTypeName());
			aStream.writeShort(theLocalVariableInfo.getIndex());
		}
    }
    
	public static void sendRegisterField(
			DataOutputStream aStream,
			int aFieldId, 
			int aClassId, 
			String aFieldName) throws IOException
	{
		sendMessageType(aStream, MessageType.REGISTER_FIELD);
		aStream.writeInt(aFieldId);
		aStream.writeInt(aClassId);
		aStream.writeUTF(aFieldName);
	}
	
	public static void sendRegisterFile(
			DataOutputStream aStream,
			int aFileId,
			String aFileName) throws IOException
	{
		sendMessageType(aStream, MessageType.REGISTER_FILE);
		aStream.writeInt(aFileId);
		aStream.writeUTF(aFileName);
	}

	private static void sendStd(
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
	private static void sendArguments(
			DataOutputStream aStream,
			Object[] aArguments) throws IOException
	{
		aStream.writeInt(aArguments != null ? aArguments.length : 0);

		if (aArguments != null) for (Object theArgument : aArguments)
			sendValue(aStream, theArgument);
	}
    

	
	private static void sendValue (DataOutputStream aStream, Object aValue) throws IOException
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
			sendRegisteredObject(aStream, aValue);
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
	
	private static void sendRegisteredObject(DataOutputStream aStream, Object aObject) throws IOException
	{
		long theObjectId = ObjectIdentity.get(aObject);
		
		if (theObjectId > 0)
		{
			// Already registered, we only send object id.
			sendMessageType(aStream, MessageType.OBJECT_UID);
			aStream.writeLong(theObjectId);
		}
		else
		{
			// First time this object appears, register it.
			sendMessageType(aStream, MessageType.REGISTERED);
			aStream.writeLong(-theObjectId);
			MyObjectOutputStream theStream = new MyObjectOutputStream(aStream);
			theStream.writeObject(aObject);
			theStream.drain();
		}

	}

	private static void sendMessageType (DataOutputStream aStream, MessageType aMessageType) throws IOException
	{
		aStream.writeByte(aMessageType.ordinal());	
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
}
