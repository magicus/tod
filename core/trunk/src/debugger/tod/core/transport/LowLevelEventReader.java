/*
 * Created on Mar 14, 2008
 */
package tod.core.transport;

import static tod.core.transport.ValueReader.readArguments;
import static tod.core.transport.ValueReader.readValue;

import java.io.DataInput;
import java.io.IOException;

import tod.agent.AgentConfig;
import tod.agent.BehaviorCallType;
import tod.agent.transport.LowLevelEventType;
import tod.core.DebugFlags;

public class LowLevelEventReader
{
//	public static void read(DataInputStream aStream, ILowLevelCollector aCollector) throws IOException
//	{
//		LowLevelEventType theType = readEventType(aStream);
//		readEvent(theType, aStream, aCollector);
//	}
	
	public static void readEvent(int aThreadId, LowLevelEventType aType, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		switch(aType)
		{
		case CLINIT_ENTER:
			readClInitEnter(aThreadId, aStream, aCollector);
			break;
			
		case BEHAVIOR_ENTER:
			readBehaviorEnter(aThreadId, aStream, aCollector);
			break;
			
		case CLINIT_EXIT:
			readClInitExit(aThreadId, aStream, aCollector);
			break;
			
		case BEHAVIOR_EXIT:
			readBehaviorExit(aThreadId, aStream, aCollector);
			break;
			
		case BEHAVIOR_EXIT_EXCEPTION:
			readBehaviorExitWithException(aThreadId, aStream, aCollector);
			break;
			
		case EXCEPTION_GENERATED:
			readExceptionGenerated(aThreadId, aStream, aCollector);
			break;
			
		case FIELD_WRITE:
			readFieldWrite(aThreadId, aStream, aCollector);
			break;
			
		case NEW_ARRAY:
			readNewArray(aThreadId, aStream, aCollector);
			break;
			
		case ARRAY_WRITE:
			readArrayWrite(aThreadId, aStream, aCollector);
			break;
			
		case LOCAL_VARIABLE_WRITE:
			readLocalVariableWrite(aThreadId, aStream, aCollector);
			break;
			
		case INSTANCEOF:
			readInstanceOf(aThreadId, aStream, aCollector);
			break;
			
		case BEFORE_CALL_DRY:
			readBeforeBehaviorCallDry(aThreadId, aStream, aCollector);
			break;
			
		case BEFORE_CALL:
			readBeforeBehaviorCall(aThreadId, aStream, aCollector);
			break;
			
		case AFTER_CALL_DRY:
			readAfterBehaviorCallDry(aThreadId, aStream, aCollector);
			break;
			
		case AFTER_CALL:
			readAfterBehaviorCall(aThreadId, aStream, aCollector);
			break;
			
		case AFTER_CALL_EXCEPTION:
			readAfterBehaviorCallWithException(aThreadId, aStream, aCollector);
			break;
			
		case OUTPUT:
			readOutput(aThreadId, aStream, aCollector);
			break;
			
		case REGISTER_OBJECT:
			readRegisterObject(aThreadId, aStream, aCollector);
			break;
			
		case REGISTER_THREAD:
			readRegisterThread(aThreadId, aStream, aCollector);
			break;
			
		default: throw new RuntimeException("Not handled: "+aType);
		}
	}
	
	private static void readRegisterThread(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.registerThread(
				aThreadId, 
				aStream.readLong(), 
				aStream.readUTF());
	}

	private static void readRegisterObject(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		int theSize = aStream.readInt(); // Packet size
		
		long theObjectId = aStream.readLong();
		if (DebugFlags.IGNORE_HOST) theObjectId >>>= AgentConfig.HOST_BITS;
		long theObjectTimestamp = aStream.readLong();
		boolean theIndexable = aStream.readBoolean();

		byte[] theData = new byte[theSize-17];
		aStream.readFully(theData);
		
		aCollector.registerObject(theObjectId, theData, theObjectTimestamp, theIndexable);
	}

	private static void readOutput(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	private static void readAfterBehaviorCallWithException(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logAfterBehaviorCallWithException(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream),
				readValue(aStream));
	}

	private static void readAfterBehaviorCall(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logAfterBehaviorCall(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream),
				readValue(aStream));
	}

	private static void readAfterBehaviorCallDry(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logAfterBehaviorCallDry(
				aThreadId,
				aStream.readLong());
	}

	private static void readBeforeBehaviorCall(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logBeforeBehaviorCall(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readCallType(aStream),
				readValue(aStream),
				readArguments(aStream));
	}

	private static void readBeforeBehaviorCallDry(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logBeforeBehaviorCallDry(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readCallType(aStream));
	}

	private static void readLocalVariableWrite(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logLocalVariableWrite(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream));
	}

	private static void readArrayWrite(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logArrayWrite(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				readValue(aStream),
				aStream.readInt(),
				readValue(aStream));
	}

	private static void readInstanceOf(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logInstanceOf(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				readValue(aStream),
				aStream.readInt(),
				aStream.readBoolean());
	}
	
	private static void readNewArray(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logNewArray(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				readValue(aStream),
				aStream.readInt(),
				aStream.readInt());
	}

	private static void readFieldWrite(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logFieldWrite(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream),
				readValue(aStream));
	}

	private static void readExceptionGenerated(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logExceptionGenerated(
				aThreadId,
				aStream.readLong(),
				aStream.readUTF(),
				aStream.readUTF(),
				aStream.readUTF(),
				aStream.readShort(),
				readValue(aStream));
	}

	private static void readBehaviorExitWithException(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logBehaviorExitWithException(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				readValue(aStream));
	}

	private static void readBehaviorExit(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logBehaviorExit(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt(),
				readValue(aStream));
	}

	private static void readClInitExit(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logClInitExit(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				aStream.readInt());
	}

	private static void readBehaviorEnter(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logBehaviorEnter(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				readCallType(aStream),
				readValue(aStream),
				readArguments(aStream));
	}

	private static void readClInitEnter(int aThreadId, DataInput aStream, ILowLevelCollector aCollector) throws IOException
	{
		aCollector.logClInitEnter(
				aThreadId,
				aStream.readLong(),
				aStream.readInt(),
				readCallType(aStream));
	}

	private static LowLevelEventType readEventType (DataInput aStream) throws IOException
	{
		byte theByte = aStream.readByte();
		return LowLevelEventType.VALUES[theByte];
	}
	
	private static BehaviorCallType readCallType (DataInput aStream) throws IOException
	{
		byte theByte = aStream.readByte();
		return BehaviorCallType.VALUES[theByte];
	}
	

}
