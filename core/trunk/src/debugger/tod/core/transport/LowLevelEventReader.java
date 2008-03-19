/*
 * Created on Mar 14, 2008
 */
package tod.core.transport;

import static tod.core.transport.ValueReader.*;
import java.io.DataInputStream;
import java.io.IOException;

import tod.agent.AgentConfig;
import tod.agent.BehaviorCallType;
import tod.agent.transport.LowLevelEventType;
import tod.agent.transport.ValueType;
import tod.core.DebugFlags;
import tod.core.ILogCollector;
import tod.core.database.structure.ObjectId;

public class LowLevelEventReader
{
	public static void read(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		LowLevelEventType theType = readEventType(aStream);
		readEvent(theType, aStream, aCollector);
	}
	
	public static void readEvent(LowLevelEventType aType, DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		switch(aType)
		{
		case CLINIT_ENTER:
			readClInitEnter(aStream, aCollector);
			break;
			
		case BEHAVIOR_ENTER:
			readBehaviorEnter(aStream, aCollector);
			break;
			
		case CLINIT_EXIT:
			readClInitExit(aStream, aCollector);
			break;
			
		case BEHAVIOR_EXIT:
			readBehaviorExit(aStream, aCollector);
			break;
			
		case BEHAVIOR_EXIT_EXCEPTION:
			readBehaviorExitWithException(aStream, aCollector);
			break;
			
		case EXCEPTION_GENERATED:
			readExceptionGenerated(aStream, aCollector);
			break;
			
		case FIELD_WRITE:
			readFieldWrite(aStream, aCollector);
			break;
			
		case NEW_ARRAY:
			readNewArray(aStream, aCollector);
			break;
			
		case ARRAY_WRITE:
			readArrayWrite(aStream, aCollector);
			break;
			
		case LOCAL_VARIABLE_WRITE:
			readLocalVariableWrite(aStream, aCollector);
			break;
			
		case INSTANCEOF:
			readInstanceOf(aStream, aCollector);
			break;
			
		case BEFORE_CALL_DRY:
			readBeforeBehaviorCallDry(aStream, aCollector);
			break;
			
		case BEFORE_CALL:
			readBeforeBehaviorCall(aStream, aCollector);
			break;
			
		case AFTER_CALL_DRY:
			readAfterBehaviorCallDry(aStream, aCollector);
			break;
			
		case AFTER_CALL:
			readAfterBehaviorCall(aStream, aCollector);
			break;
			
		case AFTER_CALL_EXCEPTION:
			readAfterBehaviorCallWithException(aStream, aCollector);
			break;
			
		case OUTPUT:
			readOutput(aStream, aCollector);
			break;
			
		case REGISTER_THREAD:
			readRegisterThread(aStream, aCollector);
			break;
			
		default: throw new RuntimeException("Not handled: "+aType);
		}
	}
	
	private static void readRegisterThread(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.registerThread(
				aStream.readInt(), 
				aStream.readLong(), 
				aStream.readUTF());
	}

	private static void readOutput(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		throw new UnsupportedOperationException();
	}

	private static void readAfterBehaviorCallWithException(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logAfterBehaviorCallWithException(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream),
				readValue(aStream));
	}

	private static void readAfterBehaviorCall(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logAfterBehaviorCall(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream),
				readValue(aStream));
	}

	private static void readAfterBehaviorCallDry(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logAfterBehaviorCallDry(
				aStream.readInt(),
				aStream.readLong());
	}

	private static void readBeforeBehaviorCall(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logBeforeBehaviorCall(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readCallType(aStream),
				readValue(aStream),
				readArguments(aStream));
	}

	private static void readBeforeBehaviorCallDry(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logBeforeBehaviorCallDry(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readCallType(aStream));
	}

	private static void readLocalVariableWrite(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logLocalVariableWrite(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream));
	}

	private static void readArrayWrite(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logArrayWrite(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				readValue(aStream),
				aStream.readInt(),
				readValue(aStream));
	}

	private static void readInstanceOf(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logInstanceOf(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				readValue(aStream),
				aStream.readInt(),
				aStream.readBoolean());
	}
	
	private static void readNewArray(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logNewArray(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				readValue(aStream),
				aStream.readInt(),
				aStream.readInt());
	}

	private static void readFieldWrite(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logFieldWrite(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream),
				readValue(aStream));
	}

	private static void readExceptionGenerated(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logExceptionGenerated(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readUTF(),
				aStream.readUTF(),
				aStream.readUTF(),
				aStream.readShort(),
				readValue(aStream));
	}

	private static void readBehaviorExitWithException(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logBehaviorExitWithException(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				readValue(aStream));
	}

	private static void readBehaviorExit(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logBehaviorExit(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream));
	}

	private static void readClInitExit(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logClInitExit(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt());
	}

	private static void readBehaviorEnter(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logBehaviorEnter(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				readCallType(aStream),
				readValue(aStream),
				readArguments(aStream));
	}

	private static void readClInitEnter(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		aCollector.logClInitEnter(
				aStream.readInt(),
				aStream.readLong(),
				aStream.readInt(),
				readCallType(aStream));
	}

	private static LowLevelEventType readEventType (DataInputStream aStream) throws IOException
	{
		byte theByte = aStream.readByte();
		return LowLevelEventType.VALUES[theByte];
	}
	
	private static BehaviorCallType readCallType (DataInputStream aStream) throws IOException
	{
		byte theByte = aStream.readByte();
		return BehaviorCallType.VALUES[theByte];
	}
	

}
