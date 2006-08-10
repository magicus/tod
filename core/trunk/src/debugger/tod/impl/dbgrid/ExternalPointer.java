/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid;

import zz.utils.bit.BitStruct;
import zz.utils.bit.BitUtils;
import zz.utils.bit.ByteBitStruct;

import static tod.impl.dbgrid.DebuggerGridConfig.*;

/**
 * Expanded representation of an external pointer.
 * @author gpothier
 */
public class ExternalPointer
{
	public static final byte[] BLANK_POINTER = new byte[(EVENTID_POINTER_SIZE+7) / 8];

	
	public final int node;
	public final int host;
	public final int thread;
	public final long timestamp;

	public ExternalPointer(int aNode, int aHost, int aThread, long aTimestamp)
	{
		node = aNode;
		host = aHost;
		thread = aThread;
		timestamp = aTimestamp;
	}
	
	/**
	 * Serializes this pointer to the given struct.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
		write(aBitStruct, node, host, thread, timestamp);
	}
	
	/**
	 * Creates an external pointer with the given information.
	 */
	public static byte[] create(int aNode, int aHost, int aThread, long aTimestamp)
	{
		BitStruct theBitStruct = new ByteBitStruct(DebuggerGridConfig.EVENTID_POINTER_SIZE);
		write(theBitStruct, aNode, aHost, aThread, aTimestamp);
		return theBitStruct.packedBytes();
	}
	
	public static void write(BitStruct aBitStruct, int aNode, int aHost, int aThread, long aTimestamp)
	{
		if (aNode >= BitUtils.pow2i(DebuggerGridConfig.EVENT_NODE_BITS))
			throw new RuntimeException("Overflow on node: "+aNode);
		
		if (aHost >= BitUtils.pow2i(DebuggerGridConfig.EVENT_HOST_BITS))
			throw new RuntimeException("Overflow on host: "+aHost);
		
		if (aThread >= BitUtils.pow2i(DebuggerGridConfig.EVENT_THREAD_BITS))
			throw new RuntimeException("Overflow on thread: "+aThread);
		
		if (aTimestamp >= BitUtils.pow2(DebuggerGridConfig.EVENT_TIMESTAMP_BITS))
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
	
	public static ExternalPointer read(byte[] aPointer)
	{
		return read(new ByteBitStruct(aPointer));
	}
	
	/**
	 * Indicates if the given pointer is null
	 */
	public static boolean isNull(byte[] aPointer)
	{
		for (byte b : aPointer) if (b != 0) return false;
		return true;
	}
}

