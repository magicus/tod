/*
 * Created on Oct 28, 2005
 */
package tod.core.transport;

import java.io.DataInputStream;
import java.io.IOException;

import tod.core.BehaviourKind;
import tod.core.ILocationRegistrer;
import tod.core.ILogCollector;
import tod.core.Output;
import tod.core.ILocationRegistrer.LineNumberInfo;
import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.database.structure.ObjectId;

public class CollectorPacketReader
{
	public static void readPacket(
			DataInputStream aStream, 
			ILogCollector aCollector,
			ILocationRegistrer aLocationRegistrer) throws IOException
	{
		MessageType theCommand = readMessageType(aStream);
		readPacket(aStream, aCollector, aLocationRegistrer, theCommand);
	}
	
	public static void readPacket(
			DataInputStream aStream,
			ILogCollector aCollector, 
			ILocationRegistrer aLocationRegistrer,
			MessageType aCommand) throws IOException
	{
		switch (aCommand)
		{
			case INSTANTIATION:
                readInstantiation(aStream, aCollector);
                break;
                
			case CONSTRUCTOR_CHAINING:
				readConstructorChaining(aStream, aCollector);
				break;
				
			case BEHAVIOR_ENTER:
                readBehaviorEnter(aStream, aCollector);
                break;
                
			case BEHAVIOR_EXIT:
                readBehaviorExit(aStream, aCollector);
                break;
                
			case BEHAVIOR_EXIT_WITH_EXCEPTION:
				readBehaviorExitWithException(aStream, aCollector);
				break;
				
			case EXCEPTION_GENERATED:
				readExceptionGenerated(aStream, aCollector);
				break;
				
			case EXCEPTION_GENERATED_UNRESOLVED:
				readExceptionGeneratedUnresolved(aStream, aCollector);
				break;
				
			case FIELD_WRITE:
                readFieldWrite(aStream, aCollector);
                break;
                
			case LOCAL_VARIABLE_WRITE:
				readLocalVariableWrite(aStream, aCollector);
				break;
				
			case BEFORE_METHOD_CALL:
                readBeforeMethodCall(aStream, aCollector);
                break;
                
			case BEFORE_METHOD_CALL_DRY:
				readBeforeMethodCallDry(aStream, aCollector);
				break;
				
			case AFTER_METHOD_CALL:
                readAfterMethodCall(aStream, aCollector);
				break;
				
			case AFTER_METHOD_CALL_DRY:
				readAfterMethodCallDry(aStream, aCollector);
				break;
				
			case AFTER_METHOD_CALL_WITH_EXCEPTION:
				readAfterMethodCallWithException(aStream, aCollector);
				break;
				
			case OUTPUT:
				readOutput(aStream, aCollector);
				break;
				
			case REGISTER_THREAD:
				readThread(aStream, aCollector);
				break;
				

			default:
				readPacket(aStream, aLocationRegistrer, aCommand);
		}

	}
	
	public static void readPacket(
			DataInputStream aStream, 
			ILocationRegistrer aRegistrer) throws IOException
	{
		MessageType theCommand = readMessageType(aStream);
		readPacket(aStream, aRegistrer, theCommand);
	}
	
	public static void readPacket(
			DataInputStream aStream,
			ILocationRegistrer aRegistrer, 
			MessageType aCommand) throws IOException
	{
		switch (aCommand)
		{
		case REGISTER_CLASS:
			readClass(aStream, aRegistrer);
			break;
			
		case REGISTER_BEHAVIOR:
			readBehaviour(aStream, aRegistrer);
			break;
			
		case REGISTER_BEHAVIOR_ATTRIBUTES:
			readBehaviourAttributes(aStream, aRegistrer);
			break;
			
		case REGISTER_FIELD:
			readField(aStream, aRegistrer);
			break;
			
		case REGISTER_FILE:
			readFile(aStream, aRegistrer);
			break;
			
		default:
			throw new RuntimeException("Unexpected message: "+aCommand);
		}
		
	}
	
	private static MessageType readMessageType (DataInputStream aStream) throws IOException
	{
		byte theByte = aStream.readByte();
		return MessageType.values()[theByte];
	}
	
    private static Object[] readArguments(DataInputStream aStream) throws IOException
    {
        int theCount = aStream.readInt();
        Object[] theArguments = new Object[theCount];
        
        for (int i=0;i<theCount;i++)
        {
            theArguments[i] = readValue(aStream);
        }
        return theArguments;
    }
    
	private static Object readValue (DataInputStream aStream) throws IOException
	{
		MessageType theType = readMessageType(aStream);
		switch (theType)
		{
			case NULL:
				return null;
				
			case BOOLEAN:
				return new Boolean (aStream.readByte() != 0);
				
			case BYTE:
				return new Byte (aStream.readByte());
				
			case CHAR:
				return new Character (aStream.readChar());
				
			case INT:
				return new Integer (aStream.readInt());
				
			case LONG:
				return new Long (aStream.readLong());
				
			case FLOAT:
				return new Float (aStream.readFloat());
				
			case DOUBLE:
				return new Double (aStream.readDouble());
				
			case STRING:
				return aStream.readUTF();
				
			case OBJECT_UID:
				return new ObjectId.ObjectUID(aStream.readLong());
				
			case OBJECT_HASH:
				return new ObjectId.ObjectHash(aStream.readInt());
				
			default:
				throw new RuntimeException("Unexpected message: "+theType);
		}
	}
	
    public static void readInstantiation(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
        aCollector.logInstantiation(aStream.readLong());
    }
    
    public static void readConstructorChaining(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
    	aCollector.logConstructorChaining(aStream.readLong());
    }
    
    public static void readBehaviorEnter(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
        aCollector.logBehaviorEnter(
        		aStream.readLong(),
                aStream.readLong(),
                aStream.readInt(),
                readValue(aStream),
                readArguments(aStream));
    }
    
    public static void readBehaviorExit(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
        aCollector.logBehaviorExit(
        		aStream.readLong(),
                aStream.readLong(),
                aStream.readInt(),
                readValue(aStream));
    }
    
    public static void readBehaviorExitWithException(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
    	aCollector.logBehaviorExitWithException(
    			aStream.readLong(),
    			aStream.readLong(),
    			aStream.readInt(),
    			readValue(aStream));
    }
    
    public static void readExceptionGenerated(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
    	aCollector.logExceptionGenerated(
    			aStream.readLong(),
    			aStream.readLong(),
    			aStream.readInt(),
    			aStream.readInt(),
    			readValue(aStream));
    }
    
    public static void readExceptionGeneratedUnresolved(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
    	aCollector.logExceptionGenerated(
    			aStream.readLong(),
    			aStream.readLong(),
    			aStream.readUTF(),
    			aStream.readUTF(),
    			aStream.readUTF(),
    			aStream.readInt(),
    			readValue(aStream));
    }
    
    public static void readBeforeMethodCall(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
        aCollector.logBeforeBehaviorCall(
        		aStream.readLong(),
        		aStream.readLong(), 
                aStream.readShort(),
                aStream.readInt(),
                readValue(aStream),
                readArguments(aStream));
    }
    
    public static void readBeforeMethodCallDry(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
    	aCollector.logBeforeBehaviorCall(
    			aStream.readLong(),
    			aStream.readShort(),
    			aStream.readInt());
    }
    
    public static void readAfterMethodCall(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
        aCollector.logAfterBehaviorCall(
        		aStream.readLong(),
        		aStream.readLong(),
                aStream.readShort(),
                aStream.readInt(),
                readValue(aStream),
                readValue(aStream));
    }
    
    public static void readAfterMethodCallDry(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
    	aCollector.logAfterBehaviorCall(aStream.readLong());
    }
    
    public static void readAfterMethodCallWithException(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
    	aCollector.logAfterBehaviorCallWithException(
    			aStream.readLong(),
    			aStream.readLong(),
    			aStream.readShort(),
    			aStream.readInt(),
    			readValue(aStream),
    			readValue(aStream));
    }
    
    public static void readFieldWrite(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
        aCollector.logFieldWrite(
        		aStream.readLong(),
                aStream.readLong(),
                aStream.readShort(),
                aStream.readInt(),
                readValue(aStream),
                readValue(aStream));
    }
    
    public static void readLocalVariableWrite(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
    	aCollector.logLocalVariableWrite(
    			aStream.readLong(),
    			aStream.readLong(),
    			aStream.readShort(),
    			aStream.readShort(),
    			readValue(aStream));
    }
    
    private static byte[] readBytes(DataInputStream aStream) throws IOException
    {
        int theLength = aStream.readInt();
        byte[] theBytes = new byte[theLength];
        aStream.readFully(theBytes);
        return theBytes;
    }
    
    public static void readOutput(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
        aCollector.logOutput(
        		aStream.readLong(),
        		aStream.readLong(),
                Output.values()[aStream.readByte()],
                readBytes(aStream));
    }
    
	public static void readClass(DataInputStream aStream, ILocationRegistrer aRegistrer) throws IOException
	{
		int theClassId = aStream.readInt();
		String theName = aStream.readUTF();
		
		int theSupertypeId = aStream.readInt();
		
		byte theInterfacesCount = aStream.readByte();
		int[] theInterfaceIds = theInterfacesCount > 0 ? new int[theInterfacesCount] : null;
		for (int i = 0; i < theInterfacesCount; i++)
		{
			theInterfaceIds[i] = aStream.readInt();
		}
		
		aRegistrer.registerType(theClassId, theName, theSupertypeId, theInterfaceIds);
	}
	
	public static void readFile(DataInputStream aStream, ILocationRegistrer aRegistrer) throws IOException
	{
		int theFileId = aStream.readInt();
		String theName = aStream.readUTF();
		aRegistrer.registerFile(theFileId, theName);
	}
	
	public static void readBehaviour (DataInputStream aStream, ILocationRegistrer aRegistrer) throws IOException
	{
		BehaviourKind theType = BehaviourKind.values()[aStream.readByte()];
		int theId = aStream.readInt();
		int theClassId = aStream.readInt();
		String theName = aStream.readUTF();
		String theSignature = aStream.readUTF();
		
        aRegistrer.registerBehavior(theType, theId, theClassId, theName, theSignature);
	}

	public static void readBehaviourAttributes (DataInputStream aStream, ILocationRegistrer aRegistrer) throws IOException
	{
		int theId = aStream.readInt();
		
		int theLineNumberTableLength = aStream.readInt();
		LineNumberInfo[] theLineNumberTable = new LineNumberInfo[theLineNumberTableLength];
		for (int i = 0; i < theLineNumberTable.length; i++)
		{
			short theStartPc = aStream.readShort();
			short theLineNumber = aStream.readShort();
			theLineNumberTable[i] = new LineNumberInfo(theStartPc, theLineNumber);
		}
		
		int theLocalVariableTableLength = aStream.readInt();
		LocalVariableInfo[] theLocalVariableTable = new LocalVariableInfo[theLocalVariableTableLength];
		for (int i = 0; i < theLocalVariableTable.length; i++)
		{
			short theStartPc = aStream.readShort();
			short theLength = aStream.readShort();
			String theVariableName = aStream.readUTF();
			String theVariableTypeName = aStream.readUTF();
			short theIndex = aStream.readShort();
			theLocalVariableTable[i] = new LocalVariableInfo(theStartPc, theLength, theVariableName, theVariableTypeName, theIndex);
		}
		
		aRegistrer.registerBehaviorAttributes(theId, theLineNumberTable, theLocalVariableTable);
	}
	
	public static void readField (DataInputStream aStream, ILocationRegistrer aRegistrer) throws IOException
	{
		int theId = aStream.readInt();
		int theClassId = aStream.readInt();
		String theName = aStream.readUTF();
		
		aRegistrer.registerField(theId, theClassId, theName);
	}
	
	public static void readThread (DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		long theId = aStream.readLong();
		String theName = aStream.readUTF();
		
		aCollector.registerThread(theId, theName);
	}

}
