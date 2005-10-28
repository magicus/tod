/*
 * Created on Oct 28, 2005
 */
package tod.core.transport;

import java.io.DataOutputStream;
import java.io.IOException;

import tod.core.BehaviourType;
import tod.core.IIdentifiableObject;
import tod.core.ObjectIdentity;
import tod.core.Output;
import tod.core.ILocationRegistrer.LineNumberInfo;
import tod.core.ILocationRegistrer.LocalVariableInfo;

/**
 * Provides the methods used to encode streamed log data.
 */
public class CollectorPacketWriter
{
	public static void sendBehaviorEnter(
			DataOutputStream aStream,
			long aTimestamp, 
			long aThreadId, 
			int aLocationId) throws IOException
	{
		sendMessageType(aStream, MessageType.BEHAVIOR_ENTER);

		aStream.writeLong(aTimestamp);
		aStream.writeLong(aThreadId);
		aStream.writeInt(aLocationId);
	}
	
	public static void sendBehaviorExit(
			DataOutputStream aStream,
			long aTimestamp,
			long aThreadId,
			int aLocationId) throws IOException
	{
		sendMessageType(aStream, MessageType.BEHAVIOR_EXIT);

		aStream.writeLong(aTimestamp);
		aStream.writeLong(aThreadId);
		aStream.writeInt(aLocationId);
	}
	
    public static void sendBeforeMethodCall(
			DataOutputStream aStream,
            long aTimestamp,
            long aThreadId, 
            int aOperationBytecodeIndex, 
            int aMethodLocationId,
            Object aTarget,
            Object[] aArguments) throws IOException
    {
            sendMessageType(aStream, MessageType.BEFORE_METHOD_CALL);
            
            aStream.writeLong(aTimestamp);
            aStream.writeLong(aThreadId);
            aStream.writeShort((short) aOperationBytecodeIndex);
            aStream.writeInt(aMethodLocationId);
            sendValue(aStream, aTarget);
            sendArguments(aStream, aArguments);
    }

	
	public static void sendAfterMethodCall(
			DataOutputStream aStream,
			long aTimestamp, 
            long aThreadId, 
            int aOperationBytecodeIndex,
            int aMethodLocationId,
            Object aTarget, 
            Object aResult) throws IOException
	{
            sendMessageType(aStream, MessageType.AFTER_METHOD_CALL);
            
            aStream.writeLong(aTimestamp);
            aStream.writeLong(aThreadId);
            aStream.writeShort((short) aOperationBytecodeIndex);
            aStream.writeInt(aMethodLocationId);
            sendValue(aStream, aTarget);
            sendValue(aStream, aResult);
	}

	public static void sendFieldWrite(
			DataOutputStream aStream,
			long aTimestamp, 
            long aThreadId, 
            int aOperationBytecodeIndex,
            int aFieldLocationId, 
            Object aTarget, 
            Object aValue) throws IOException
	{
            sendMessageType(aStream, MessageType.FIELD_WRITE);
            
            aStream.writeLong(aTimestamp);
            aStream.writeLong(aThreadId);
            aStream.writeShort((short) aOperationBytecodeIndex);
            aStream.writeInt(aFieldLocationId);
            sendValue(aStream, aTarget);
            sendValue(aStream, aValue);
	}

	public static void sendInstantiation(
			DataOutputStream aStream,
			long aTimestamp, 
            long aThreadId,
            int aOperationBytecodeIndex, 
            int aTypeLocationId,
            Object aInstance) throws IOException
	{
            sendMessageType(aStream, MessageType.INSTANTIATION);
            
            aStream.writeLong(aTimestamp);
            aStream.writeLong(aThreadId);
            aStream.writeShort((short) aOperationBytecodeIndex);
            aStream.writeInt(aTypeLocationId);
            sendValue(aStream, aInstance);
	}

	public static void sendLocalVariableWrite(
			DataOutputStream aStream,
			long aTimestamp,
            long aThreadId,
            int aOperationBytecodeIndex, 
            int aVariableId,
            Object aTarget,
            Object aValue) throws IOException
	{
            sendMessageType(aStream, MessageType.LOCAL_VARIABLE_WRITE);
            
            aStream.writeLong(aTimestamp);
            aStream.writeLong(aThreadId);
            aStream.writeShort((short) aOperationBytecodeIndex);
            aStream.writeShort((short) aVariableId);
            sendValue(aStream, aTarget);
            sendValue(aStream, aValue);
	}

	public static void sendOutput(
			DataOutputStream aStream,
			long aTimestamp, 
            long aThreadId, 
            Output aOutput, 
            byte[] aData) throws IOException
	{
            sendMessageType(aStream, MessageType.OUTPUT);
            
            aStream.writeLong(aTimestamp);
            aStream.writeLong(aThreadId);
            aStream.writeByte((byte) aOutput.ordinal());
            
            aStream.writeInt(aData.length);
            aStream.write(aData);
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
    		BehaviourType aBehaviourType,
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
	
	public static void sendRegisterThread (
			DataOutputStream aStream,
			long aThreadId,
			String aName) throws IOException
	{
			sendMessageType(aStream, MessageType.REGISTER_THREAD);
			aStream.writeLong(aThreadId);
			aStream.writeUTF(aName);
	}

    /**
     * Sends an argument to the socked. This method handles arrays,
     * single objects or null values.
     */
    private static void sendArguments(
			DataOutputStream aStream,
    		Object[] aArguments) throws IOException
    {
        aStream.writeInt(aArguments != null ? aArguments.length : 0);
        
        if (aArguments != null) 
        	for (Object theArgument : aArguments) sendValue(aStream, theArgument);
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
		else if (aValue instanceof String)
		{
			String theString = (String) aValue;
			sendMessageType(aStream, MessageType.STRING);
			aStream.writeUTF(theString);
		}
		else if (aValue instanceof IIdentifiableObject)
		{
			IIdentifiableObject theObject = (IIdentifiableObject) aValue;
			long theObjectId = ObjectIdentity.get(theObject);
			sendMessageType(aStream, MessageType.OBJECT_UID);
			aStream.writeLong(theObjectId);
		}
		else
		{
			int theHash = aValue.hashCode();
			sendMessageType(aStream, MessageType.OBJECT_HASH);
			aStream.writeInt(theHash);
		}
	}

	private static void sendMessageType (DataOutputStream aStream, MessageType aMessageType) throws IOException
	{
		aStream.writeByte(aMessageType.ordinal());			
	}

}
