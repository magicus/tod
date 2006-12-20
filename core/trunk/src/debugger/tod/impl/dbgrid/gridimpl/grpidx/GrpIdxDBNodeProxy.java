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
package tod.impl.dbgrid.gridimpl.grpidx;

import java.io.IOException;
import java.net.Socket;

import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.gridimpl.grpidx.GrpIdxDatabaseNode.IndexKind;
import tod.impl.dbgrid.gridimpl.uniform.UniformDBNodeProxy;
import tod.utils.NativeStream;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

public class GrpIdxDBNodeProxy extends UniformDBNodeProxy
{
	private final int[] itsIndexBuffer = new int[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE];
	private final byte[] itsIndexByteBuffer = new byte[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE*4];
	private final BitStruct itsIndexBufferStruct = new IntBitStruct(itsIndexBuffer);

	private long itsSentIndexCount = 0;
	private int itsBufferedIndexCount = 0;

	public GrpIdxDBNodeProxy(Socket aSocket, int aNodeId, GridMaster aMaster)
	{
		super(aSocket, aNodeId, aMaster);
	}
	
	public void pushIndexData(IndexKind aIndexKind, int aIndex, StdTuple aTuple)
	{
		if (aTuple.getBitCount() 
				+ GrpIdxDatabaseNode.KIND_BITS
				+ aIndexKind.getIndexBits() > itsIndexBufferStruct.getRemainingBits())
		{
			sendIndexBuffer();
		}
		
		itsIndexBufferStruct.writeInt(aIndexKind.ordinal(), GrpIdxDatabaseNode.KIND_BITS);
		itsIndexBufferStruct.writeInt(aIndex, aIndexKind.getIndexBits());
		aTuple.writeTo(itsIndexBufferStruct);
		
		itsBufferedIndexCount++;
	}
	
	@Override
	protected synchronized void sendEventBuffer()
	{
		super.sendEventBuffer();
	}
	
	private synchronized void sendIndexBuffer()
	{
//		System.out.println(String.format(
//				"Sending %d messages to node %d (already sent %d)",
//				itsMessagesCount,
//				itsNodeId,
//				itsSentMessagesCount));
		try
		{
			getOutStream().writeByte(GrpIdxDatabaseNode.CMD_PUSH_INDEX_DATA);
			getOutStream().writeInt(itsBufferedIndexCount);
			
			NativeStream.i2b(itsIndexBuffer, itsIndexByteBuffer);
			
			getOutStream().write(itsIndexByteBuffer);
//			itsOutputStream.flush();
			
			itsSentIndexCount += itsBufferedIndexCount;
			
			itsIndexBufferStruct.reset();
			itsBufferedIndexCount = 0;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	

}
