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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

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
                
			case SUPER_CALL:
				readSuperCall(aStream, aCollector);
				break;
				
			case METHOD_CALL:
                readMethodCall(aStream, aCollector);
                break;
                
			case BEHAVIOR_EXIT:
                readBehaviorExit(aStream, aCollector);
                break;
                
			case FIELD_WRITE:
                readFieldWrite(aStream, aCollector);
                break;
                
			case ARRAY_WRITE:
				readArrayWrite(aStream, aCollector);
				break;
				
			case LOCAL_VARIABLE_WRITE:
				readLocalWrite(aStream, aCollector);
				break;
				
			case OUTPUT:
				readOutput(aStream, aCollector);
				break;
				
			case EXCEPTION:
				readException(aStream, aCollector);
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
	
    private static Object[] readArguments(DataInputStream aStream, ILogCollector aCollector) throws IOException
    {
        int theCount = aStream.readInt();
        Object[] theArguments = new Object[theCount];
        
        for (int i=0;i<theCount;i++)
        {
            theArguments[i] = readValue(aStream, aCollector);
        }
        return theArguments;
    }
    
	private static Object readValue (DataInputStream aStream, ILogCollector aCollector) throws IOException
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
				
				aCollector.register(theObjectId, theObject);
				return new ObjectId.ObjectUID(theObjectId);
				
			case OBJECT_UID:
				return new ObjectId.ObjectUID(aStream.readLong());
				
			case OBJECT_HASH:
				return new ObjectId.ObjectHash(aStream.readInt());
				
			default:
				throw new RuntimeException("Unexpected message: "+theType);
		}
	}
	
	
	public static void readMethodCall(DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		aStream.readInt(); // Packet size
		aCollector.methodCall(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readShort(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readBoolean(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream, aCollector),
				readArguments(aStream, aCollector));
	}
	
	public static void readInstantiation(DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		aStream.readInt(); // Packet size
		aCollector.instantiation(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readShort(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readBoolean(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream, aCollector),
				readArguments(aStream, aCollector));
	}
	
	public static void readSuperCall(DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		aStream.readInt(); // Packet size
		aCollector.superCall(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readShort(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readBoolean(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream, aCollector),
				readArguments(aStream, aCollector));

	}
	
	public static void readBehaviorExit(DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		aStream.readInt(); // Packet size
		aCollector.behaviorExit(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readShort(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				aStream.readBoolean(),
				readValue(aStream, aCollector));
	}
	
	public static void readFieldWrite(DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		aStream.readInt(); // Packet size
		aCollector.fieldWrite(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readShort(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream, aCollector),
				readValue(aStream, aCollector));
	}
	
	public static void readArrayWrite(DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		aStream.readInt(); // Packet size
		aCollector.arrayWrite(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readShort(),
				aStream.readLong(),
				aStream.readInt(),
				readValue(aStream, aCollector),
				aStream.readInt(),
				readValue(aStream, aCollector));
	}
	
	public static void readLocalWrite(DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		aStream.readInt(); // Packet size
		aCollector.localWrite(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readShort(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream, aCollector));
	}
	
	public static void readException(DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		aStream.readInt(); // Packet size
		aCollector.exception(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readShort(),
				aStream.readLong(),
				aStream.readUTF(),
				aStream.readUTF(),
				aStream.readUTF(),
				aStream.readInt(),
				readValue(aStream, aCollector));
	}
	
	public static void readOutput(DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		aStream.readInt(); // Packet size
        aCollector.output(
        		aStream.readInt(),
				aStream.readLong(),
        		aStream.readShort(),
        		aStream.readLong(),
                Output.values()[aStream.readByte()],
                readBytes(aStream));
	}
	
	public static void readThread(DataInputStream aStream, ILogCollector aCollector) throws IOException
	{
		aStream.readInt(); // Packet size
		aCollector.thread(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readUTF());
	}
    
    private static byte[] readBytes(DataInputStream aStream) throws IOException
    {
        int theLength = aStream.readInt();
        byte[] theBytes = new byte[theLength];
        aStream.readFully(theBytes);
        return theBytes;
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
	
}
