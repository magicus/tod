/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid;

import zz.utils.BitStruct;

/**
 * Expanded representation of an external pointer.
 * @author gpothier
 */
public class ExternalPointer
{
	private final int itsNode;
	private final int itsHost;
	private final int itsThread;
	private final long itsTimestamp;

	public ExternalPointer(int aNode, int aHost, int aThread, long aTimestamp)
	{
		itsNode = aNode;
		itsHost = aHost;
		itsThread = aThread;
		itsTimestamp = aTimestamp;
	}
	
	/**
	 * Serializes this pointer to the given struct.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
		write(aBitStruct, itsNode, itsHost, itsThread, itsTimestamp);
	}
	
	public static byte[] create(int aNode, int aHost, int aThread, long aTimestamp)
	{
		BitStruct theBitStruct = new BitStruct(DebuggerGridConfig.EVENTID_POINTER_SIZE);
		write(theBitStruct, aNode, aHost, aThread, aTimestamp);
		return theBitStruct.packedBytes();
	}
	
	public static void write(BitStruct aBitStruct, int aNode, int aHost, int aThread, long aTimestamp)
	{
		if (aNode >= BitStruct.pow2(DebuggerGridConfig.EVENT_NODE_BITS))
			throw new RuntimeException("Overflow on node: "+aNode);
		
		if (aHost >= BitStruct.pow2(DebuggerGridConfig.EVENT_HOST_BITS))
			throw new RuntimeException("Overflow on host: "+aHost);
		
		if (aThread >= BitStruct.pow2(DebuggerGridConfig.EVENT_THREAD_BITS))
			throw new RuntimeException("Overflow on thread: "+aThread);
		
		if (aTimestamp >= BitStruct.pow2(DebuggerGridConfig.EVENT_TIMESTAMP_BITS))
			throw new RuntimeException("Overflow on timestamp: "+aTimestamp);
		
		aBitStruct.writeInt(aNode, DebuggerGridConfig.EVENT_NODE_BITS);
		aBitStruct.writeInt(aHost, DebuggerGridConfig.EVENT_HOST_BITS);
		aBitStruct.writeInt(aThread, DebuggerGridConfig.EVENT_THREAD_BITS);
		aBitStruct.writeLong(aTimestamp, DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
	}
	
	/**
	 * Reads an external pointer from the given struct.
	 */
	public static ExternalPointer read(BitStruct aBitStruct)
	{
		int theNode = aBitStruct.readInt(DebuggerGridConfig.EVENT_NODE_BITS);
		int theHost = aBitStruct.readInt(DebuggerGridConfig.EVENT_HOST_BITS);
		int theThread = aBitStruct.readInt(DebuggerGridConfig.EVENT_THREAD_BITS);
		long theTimestamp = aBitStruct.readLong(DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		
		return new ExternalPointer(theNode, theHost, theThread, theTimestamp);
	}
	
}

