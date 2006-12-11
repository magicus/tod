/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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

	public final int host;
	public final int thread;
	public final long timestamp;

	public ExternalPointer(int aHost, int aThread, long aTimestamp)
	{
		host = aHost;
		thread = aThread;
		timestamp = aTimestamp;
	}
	
	/**
	 * Serializes this pointer to the given struct.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
		write(aBitStruct, host, thread, timestamp);
	}
	
	/**
	 * Creates an external pointer with the given information.
	 */
	public static byte[] create(int aHost, int aThread, long aTimestamp)
	{
		BitStruct theBitStruct = new ByteBitStruct(EVENTID_POINTER_SIZE);
		write(theBitStruct, aHost, aThread, aTimestamp);
		return theBitStruct.packedBytes();
	}
	
	public static void write(BitStruct aBitStruct, int aHost, int aThread, long aTimestamp)
	{
		if (BitUtils.isOverflow(aHost, EVENT_HOST_BITS))
			throw new RuntimeException("Overflow on host: "+aHost);
		
		if (BitUtils.isOverflow(aThread, EVENT_THREAD_BITS))
			throw new RuntimeException("Overflow on thread: "+aThread);
		
		if (BitUtils.isOverflow(aTimestamp, EVENT_TIMESTAMP_BITS))
			throw new RuntimeException("Overflow on timestamp: "+aTimestamp);
		
		aBitStruct.writeInt(aHost, EVENT_HOST_BITS);
		aBitStruct.writeInt(aThread, EVENT_THREAD_BITS);
		aBitStruct.writeLong(aTimestamp, EVENT_TIMESTAMP_BITS);
	}
	
	/**
	 * Reads an external pointer from the given struct.
	 */
	public static ExternalPointer read(BitStruct aBitStruct)
	{
		int theHost = aBitStruct.readInt(EVENT_HOST_BITS);
		int theThread = aBitStruct.readInt(EVENT_THREAD_BITS);
		long theTimestamp = aBitStruct.readLong(EVENT_TIMESTAMP_BITS);
		
		return new ExternalPointer(theHost, theThread, theTimestamp);
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

