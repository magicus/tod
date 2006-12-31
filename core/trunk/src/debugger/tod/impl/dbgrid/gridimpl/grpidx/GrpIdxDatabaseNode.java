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

import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_ARRAY_INDEX_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BEHAVIOR_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BYTECODE_LOCS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_DEPTH_RANGE;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_FIELD_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_HOSTS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_OBJECT_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_THREADS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_VAR_COUNT;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.RoleIndexSet;
import tod.impl.dbgrid.db.StdIndexSet;
import tod.impl.dbgrid.db.RoleIndexSet.RoleTuple;
import tod.impl.dbgrid.db.StdIndexSet.StdTuple;
import tod.impl.dbgrid.db.file.IndexTuple;
import tod.impl.dbgrid.db.file.IndexTupleCodec;
import tod.impl.dbgrid.gridimpl.AbstractEventDatabase;
import tod.impl.dbgrid.gridimpl.uniform.UniformDatabaseNode;
import tod.impl.dbgrid.messages.MessageType;
import tod.utils.NativeStream;
import zz.utils.Utils;
import zz.utils.bit.BitStruct;
import zz.utils.bit.BitUtils;
import zz.utils.bit.IntBitStruct;

public class GrpIdxDatabaseNode extends UniformDatabaseNode
{
	/**
	 * This command pushes a list of index data to the node.
	 * args:
	 *  count: int
	 *  index data: list of (index kind: KIND_BITS bits, index number: variable, index value: variable)
	 * return: none
	 */
	public static final byte CMD_PUSH_INDEX_DATA = 16;
	
	
	/**
	 * Number of bits to encode index kind.
	 */
	public static final int KIND_BITS = BitUtils.log2ceil(IndexKind.VALUES.length);

	private final int[] itsBuffer = new int[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE];
	private final byte[] itsByteBuffer = new byte[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE*4];
	private final BitStruct itsStruct = new IntBitStruct(itsBuffer);

	
	public static enum IndexKind
	{
		TYPE(BitUtils.log2ceil(MessageType.VALUES.length), StdIndexSet.TUPLE_CODEC)
		{
			@Override
			public void index(Indexes aIndexes, int aIndex, IndexTuple aTuple)
			{
				aIndexes.indexType(aIndex, (StdTuple) aTuple);
			}
		},
		HOST(BitUtils.log2ceil(STRUCTURE_HOSTS_COUNT), StdIndexSet.TUPLE_CODEC)
		{
			@Override
			public void index(Indexes aIndexes, int aIndex, IndexTuple aTuple)
			{
				aIndexes.indexHost(aIndex, (StdTuple) aTuple);
			}
		}, 
		THREAD(BitUtils.log2ceil(STRUCTURE_THREADS_COUNT), StdIndexSet.TUPLE_CODEC)
		{
			@Override
			public void index(Indexes aIndexes, int aIndex, IndexTuple aTuple)
			{
				aIndexes.indexThread(aIndex, (StdTuple) aTuple);
			}
		}, 
		DEPTH(BitUtils.log2ceil(STRUCTURE_DEPTH_RANGE), StdIndexSet.TUPLE_CODEC)
		{
			@Override
			public void index(Indexes aIndexes, int aIndex, IndexTuple aTuple)
			{
				aIndexes.indexDepth(aIndex, (StdTuple) aTuple);
			}
		}, 
		LOCATION(BitUtils.log2ceil(STRUCTURE_BYTECODE_LOCS_COUNT), StdIndexSet.TUPLE_CODEC)
		{
			@Override
			public void index(Indexes aIndexes, int aIndex, IndexTuple aTuple)
			{
				aIndexes.indexLocation(aIndex, (StdTuple) aTuple);
			}
		},
		BEHAVIOR(BitUtils.log2ceil(STRUCTURE_BEHAVIOR_COUNT), RoleIndexSet.TUPLE_CODEC)
		{
			@Override
			public void index(Indexes aIndexes, int aIndex, IndexTuple aTuple)
			{
				aIndexes.indexBehavior(aIndex, (RoleTuple) aTuple);
			}
		}, 
		FIELD(BitUtils.log2ceil(STRUCTURE_FIELD_COUNT), StdIndexSet.TUPLE_CODEC)
		{
			@Override
			public void index(Indexes aIndexes, int aIndex, IndexTuple aTuple)
			{
				aIndexes.indexField(aIndex, (StdTuple) aTuple);
			}
		}, 
		VARIABLE(BitUtils.log2ceil(STRUCTURE_VAR_COUNT), StdIndexSet.TUPLE_CODEC)
		{
			@Override
			public void index(Indexes aIndexes, int aIndex, IndexTuple aTuple)
			{
				aIndexes.indexVariable(aIndex, (StdTuple) aTuple);
			}
		}, 
		INDEX(BitUtils.log2ceil(STRUCTURE_ARRAY_INDEX_COUNT), StdIndexSet.TUPLE_CODEC)
		{
			@Override
			public void index(Indexes aIndexes, int aIndex, IndexTuple aTuple)
			{
				aIndexes.indexArrayIndex(aIndex, (StdTuple) aTuple);
			}
		}, 
		OBJECT(BitUtils.log2ceil(STRUCTURE_OBJECT_COUNT), RoleIndexSet.TUPLE_CODEC)
		{
			@Override
			public void index(Indexes aIndexes, int aIndex, IndexTuple aTuple)
			{
				aIndexes.indexObject(aIndex, (RoleTuple) aTuple);
			}
		};
		
		/**
		 * Number of bits used to represent an index value.
		 */
		private int itsIndexBits;
		private IndexTupleCodec<? extends IndexTuple> itsCodec;

		private IndexKind(
				int aIndexBits, 
				IndexTupleCodec<? extends IndexTuple> aCodec)
		{
			itsIndexBits = aIndexBits;
			itsCodec = aCodec;
		}

		public int getIndexBits()
		{
			return itsIndexBits;
		}
		
		/**
		 * Appends the given tuple to the index corresponding to this kind. 
		 */
		public abstract void index(
				Indexes aIndexes, 
				int aIndex, 
				IndexTuple aTuple);
		
		public IndexTuple readTuple(BitStruct aStruct)
		{
			return itsCodec.read(aStruct);
		}
		
		/**
		 * Cached values; call to values() is costly. 
		 */
		public static final IndexKind[] VALUES = values();
		
	}

	public GrpIdxDatabaseNode() throws RemoteException
	{
	}
	
	@Override
	protected AbstractEventDatabase createDatabase(File aFile)
	{
		int theNodeIndex = Integer.parseInt(getNodeId().substring(3));

		return new GrpIdxEventDatabase(theNodeIndex, aFile);
	}

	@Override
	public GrpIdxEventDatabase getDatabase()
	{
		return (GrpIdxEventDatabase) super.getDatabase();
	}

	@Override
	protected void processCommand(
			byte aCommand, 
			DataInputStream aInStream, 
			DataOutputStream aOutStream) throws IOException
	{
		switch (aCommand)
		{
		case CMD_PUSH_INDEX_DATA:
			pushIndexData(aInStream);
			break;
			
		default:
			super.processCommand(aCommand, aInStream, aOutStream);
				
		}
	}

	private void pushIndexData(DataInputStream aStream) throws IOException
	{
		int theCount = aStream.readInt();
		
		Utils.readFully(aStream, itsByteBuffer);
		NativeStream.b2i(itsByteBuffer, itsBuffer);
		itsStruct.reset();
		
		for (int i=0;i<theCount;i++)
		{
			IndexKind theKind = IndexKind.VALUES[itsStruct.readInt(GrpIdxDatabaseNode.KIND_BITS)];
			
			int theIndex = itsStruct.readInt(theKind.getIndexBits());
			IndexTuple theTuple = theKind.readTuple(itsStruct);
			
			getDatabase().index(theKind, theIndex, theTuple);
		}
	}


	
}
