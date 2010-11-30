/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.replay2;

import java.io.Serializable;

import tod.core.database.structure.ObjectId;
import tod.utils.ByteBuffer;

public class LocalsSnapshot implements Serializable
{
	private static final long serialVersionUID = 5471154741961L;

	private long itsBlockId;
	
	/**
	 * Offset of the start of the current packet in the trace file.
	 */
	private long itsPacketStartOffset;
	
	/**
	 * Offset of the current message within the packet.
	 */
	private int itsPacketOffset;
	
	/**
	 * Probe id of the location in the code that generated the snapshot
	 */
	private int itsProbeId;
	
	/**
	 * As behavior ids are encoded with deltas, we need the current value 
	 * at the time the snapshot was taken.
	 */
	private int itsBehIdCurrentValue;
	
	/**
	 * As object ids are encoded with deltas, we need the current value 
	 * at the time the snapshot was taken.
	 */
	private long itsObjIdCurrentValue;
	
	private int itsIntValuesIndex;
	private int[] itsIntValues;
	
	private int itsLongValuesIndex;
	private long[] itsLongValues;
	
	private int itsFloatValuesIndex;
	private float[] itsFloatValues;
	
	private int itsDoubleValuesIndex;
	private double[] itsDoubleValues;
	
	private int itsRefValuesIndex;
	private ObjectId[] itsRefValues;
	
	public LocalsSnapshot(
			long aBlockId,
			long aPacketStartOffset,
			int aPacketOffset,
			int aProbeId,
			int aBehIdCurrentValue,
			long aObjIdCurrentValue)
	{
		itsBlockId = aBlockId;
		itsPacketStartOffset = aPacketStartOffset;
		itsPacketOffset = aPacketOffset;
		itsProbeId = aProbeId;
		itsBehIdCurrentValue = aBehIdCurrentValue;
		itsObjIdCurrentValue = aObjIdCurrentValue;
	}
	
	/**
	 * Instantiates a snapshot by reading its contents from the given buffer.
	 */
	public LocalsSnapshot(ByteBuffer aBuffer)
	{
		itsBlockId = aBuffer.getLong();
		itsPacketStartOffset = aBuffer.getLong();
		itsPacketOffset = aBuffer.getInt();
		itsProbeId = aBuffer.getInt();
		itsBehIdCurrentValue = aBuffer.getInt();
		itsObjIdCurrentValue = aBuffer.getLong();
		
		itsIntValuesIndex = aBuffer.getShort();
		itsIntValues = new int[itsIntValuesIndex];
		for(int i=0;i<itsIntValuesIndex;i++) itsIntValues[i] = aBuffer.getInt();
		
		itsLongValuesIndex = aBuffer.getShort();
		itsLongValues = new long[itsLongValuesIndex];
		for(int i=0;i<itsLongValuesIndex;i++) itsLongValues[i] = aBuffer.getLong();
		
		itsFloatValuesIndex = aBuffer.getShort();
		itsFloatValues = new float[itsFloatValuesIndex];
		for(int i=0;i<itsFloatValuesIndex;i++) itsFloatValues[i] = aBuffer.getFloat();
		
		itsDoubleValuesIndex = aBuffer.getShort();
		itsDoubleValues = new double[itsDoubleValuesIndex];
		for(int i=0;i<itsDoubleValuesIndex;i++) itsDoubleValues[i] = aBuffer.getDouble();
		
		itsRefValuesIndex = aBuffer.getShort();
		itsRefValues = new ObjectId[itsRefValuesIndex];
		for(int i=0;i<itsRefValuesIndex;i++) itsRefValues[i] = new ObjectId(aBuffer.getLong());
	}

	/**
	 * Writes this snapshot to the given buffer.
	 */
	public void write(ByteBuffer aBuffer)
	{
		aBuffer.putLong(itsBlockId);
		aBuffer.putLong(itsPacketStartOffset);
		aBuffer.putInt(itsPacketOffset);
		aBuffer.putInt(itsProbeId);
		aBuffer.putInt(itsBehIdCurrentValue);
		aBuffer.putLong(itsObjIdCurrentValue);
		
		aBuffer.putShort((short) itsIntValues.length);
		aBuffer.putInts(itsIntValues);
		
		aBuffer.putShort((short) itsLongValues.length);
		aBuffer.putLongs(itsLongValues);
		
		aBuffer.putShort((short) itsFloatValues.length);
		aBuffer.putFloats(itsFloatValues);
		
		aBuffer.putShort((short) itsDoubleValues.length);
		aBuffer.putDoubles(itsDoubleValues);
		
		aBuffer.putShort((short) itsRefValues.length);
		for(int i=0;i<itsRefValues.length;i++) aBuffer.putLong(itsRefValues[i].getId());
	}
	
	public long getBlockId()
	{
		return itsBlockId;
	}
	
	public long getPacketStartOffset()
	{
		return itsPacketStartOffset;
	}
	
	public int getPacketOffset()
	{
		return itsPacketOffset;
	}
	
	public int getProbeId()
	{
		return itsProbeId;
	}
	
	public int getBehIdCurrentValue()
	{
		return itsBehIdCurrentValue;
	}
	
	public long getObjIdCurrentValue()
	{
		return itsObjIdCurrentValue;
	}
	
	public void alloc(
			int aIntValuesCount, 
			int aLongValuesCount, 
			int aFloatValuesCount, 
			int aDoubleValuesCount, 
			int aRefValuesCount)
	{
		itsIntValues = new int[aIntValuesCount];
		itsLongValues = new long[aLongValuesCount];
		itsFloatValues = new float[aFloatValuesCount];
		itsDoubleValues = new double[aDoubleValuesCount];
		itsRefValues = new ObjectId[aRefValuesCount];
	}
	
	public void pushInt(int aValue)
	{
		itsIntValues[itsIntValuesIndex++] = aValue;
	}
	
	public int popInt()
	{
		return itsIntValues[--itsIntValuesIndex];
	}
	
	public void pushLong(long aValue)
	{
		itsLongValues[itsLongValuesIndex++] = aValue;
	}
	
	public long popLong()
	{
		return itsLongValues[--itsLongValuesIndex];
	}
	
	public void pushFloat(float aValue)
	{
		itsFloatValues[itsFloatValuesIndex++] = aValue;
	}
	
	public float popFloat()
	{
		return itsFloatValues[--itsFloatValuesIndex];
	}
	
	public void pushDouble(double aValue)
	{
		itsDoubleValues[itsDoubleValuesIndex++] = aValue;
	}
	
	public double popDouble()
	{
		return itsDoubleValues[--itsDoubleValuesIndex];
	}
	
	public void pushRef(ObjectId aValue)
	{
		itsRefValues[itsRefValuesIndex++] = aValue;
	}
	
	public ObjectId popRef()
	{
		return itsRefValues[--itsRefValuesIndex];
	}
	
}