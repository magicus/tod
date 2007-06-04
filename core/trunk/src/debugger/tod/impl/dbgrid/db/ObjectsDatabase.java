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
package tod.impl.dbgrid.db;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_BYTEOFFSET_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_SIZE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.db.file.IndexTuple;
import tod.impl.dbgrid.db.file.IndexTupleCodec;
import tod.impl.dbgrid.db.file.HardPagedFile.Page;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;
import zz.utils.bit.BitStruct;

/**
 * A database for storing registered objects.
 * Each object has an associated id. It is assumed that
 * objects are sent in in their id order.
 * @author gpothier
 */
public class ObjectsDatabase
{
	private HardPagedFile itsFile;
	private HierarchicalIndex<ObjectPointerTuple> itsindex;
	private ObjectPointerTuple itsTuple = new ObjectPointerTuple(0, 0, 0);
	
	private long itsLastRecordedId = 0;
	private int itsDroppedObjects = 0; 
	
	private ByteBuffer itsByteBuffer = ByteBuffer.allocate(0);
	private IntBuffer itsIntBuffer = itsByteBuffer.asIntBuffer();
	
	/**
	 * Current data page
	 */
	private Page itsCurrentPage;
	
	/**
	 * Offset in the current page, in bytes.
	 */
	private int itsCurrentOffset;
	
	private long itsObjectsCount = 0;
	
	public ObjectsDatabase(File aFile)
	{
		Monitor.getInstance().register(this);
		try
		{
			itsFile = new HardPagedFile(aFile, DB_PAGE_SIZE);
			itsindex = new HierarchicalIndex<ObjectPointerTuple>(
					ObjectTupleCodec.getInstance(),
					itsFile);
			
			itsCurrentPage = itsFile.create();
			itsCurrentOffset = 0;
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void unregister()
	{
		Monitor.getInstance().unregister(this);
		itsFile.dispose();
	}
	
	/**
	 * Serializes the given object into an array of bytes so that
	 * it can be stored.
	 */
	protected byte[] encode(Object aObject)
	{
		try
		{
			ByteArrayOutputStream theStream = new ByteArrayOutputStream();
			ObjectOutputStream theOOStream = new ObjectOutputStream(theStream);
			theOOStream.writeObject(aObject);
			theOOStream.flush();
			return theStream.toByteArray();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Deserializes an object previously serialized by {@link #encode(Object)}.
	 */
	protected Object decode(byte[] aData)
	{
		assert aData.length > 0;
		try
		{
			ByteArrayInputStream theStream = new ByteArrayInputStream(aData);
			ObjectInputStream theOIStream = new ObjectInputStream(theStream);
			return theOIStream.readObject();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Stores an object into the database. The object is serialized by {@link #encode(Object)}
	 * prior to storage.
	 * @param aId Id of the object to store.
	 * @param aObject The object to store.
	 */
	public void store(long aId, Object aObject)
	{
		store(aId, encode(aObject));
	}
	
	private void checkBufferSize(int aSize)
	{
		if (itsIntBuffer.capacity() < aSize)
		{
			itsByteBuffer = ByteBuffer.allocate(4*Math.max(itsIntBuffer.capacity()*2, aSize));
			itsByteBuffer.order(ByteOrder.nativeOrder());
			itsIntBuffer = itsByteBuffer.asIntBuffer();
		}
	}
	
	/**
	 * Stores an already-serialized object into the database.
	 */
	public void store(long aId, byte[] aData)
	{
		itsObjectsCount++;
		
		assert aData.length > 0;
		if (aId < itsLastRecordedId)
		{
			itsDroppedObjects++;
			return;
		}
		
		// Add index tuple
		itsTuple.set(aId, itsCurrentPage.getPageId(), itsCurrentOffset);
		itsindex.add(itsTuple);
		
		// Store object data
		itsLastRecordedId = aId;
		int theDataSize = aData.length;
		int theStorageSize = 1 + (theDataSize+3)/4;
		
		checkBufferSize(theStorageSize);
		
		itsIntBuffer.position(0);
		itsIntBuffer.put(theDataSize);
		itsByteBuffer.position(4);
		itsByteBuffer.put(aData);

		itsIntBuffer.position(0);
		int theRemaining = theStorageSize*4;
		while (theRemaining > 0)
		{			
			// Determine available space in current page, keeping 64 bits
			// for next-page pointer
			int theSpaceInPage = itsCurrentPage.getSize()-8-itsCurrentOffset;
			int[] thePageData = itsCurrentPage.getData();
			
			int theAmountToCopy = Math.min(theRemaining, theSpaceInPage);
			assert theAmountToCopy % 4 == 0;
			assert itsCurrentOffset % 4 == 0;
			
			itsIntBuffer.get(thePageData, itsCurrentOffset/4, theAmountToCopy/4);
			
			itsCurrentPage.modified();
			
			theRemaining -= theAmountToCopy;
			itsCurrentOffset += theAmountToCopy;
			
			if (theAmountToCopy == theSpaceInPage)
			{
				// Allocate next page
				Page theNextPage = itsFile.create();
				long thePageId = theNextPage.getPageId();
				assert itsCurrentPage.getSize()-8 == itsCurrentOffset;
				
				thePageData[itsCurrentOffset/4] = (int) (thePageId & 0xffffffff);
				thePageData[(itsCurrentOffset/4)+1] = (int) (thePageId >>> 32);
				
				itsCurrentPage = theNextPage;
				itsCurrentOffset = 0;
			}
			
			itsCurrentPage.use();
		}
	}
	
	/**
	 * Loads an object from the database.
	 * @param aId Id of the object to load.
	 */
	public Object load(long aId)
	{
		ObjectPointerTuple theTuple = itsindex.getTupleAt(aId, true);
		if (theTuple == null) return null;
		
		int theOffset = theTuple.getOffset();
		Page thePage = itsFile.get(theTuple.getPageId());
		
		assert theOffset % 4 == 0;
		int theDataSize = thePage.getData()[theOffset/4];

		int theStorageSize = (theDataSize+3)/4;
		
		checkBufferSize(theStorageSize);

		theOffset += 4;
		
		itsIntBuffer.position(0);
		int theRemaining = theStorageSize*4;
		while (theRemaining > 0)
		{
			// Determine available space in current page, keeping 64 bits
			// for next-page pointer
			int theSpaceInPage = thePage.getSize()-8-theOffset;
			int[] thePageData = thePage.getData();
			
			int theAmountToCopy = Math.min(theRemaining, theSpaceInPage);
			assert theAmountToCopy % 4 == 0;
			assert theOffset % 4 == 0;
			
			if (theAmountToCopy > 0) itsIntBuffer.put(thePageData, theOffset/4, theAmountToCopy/4); 
			
			theRemaining -= theAmountToCopy;
			theOffset += theAmountToCopy;
			
			if (theRemaining > 0)
			{
				// Get next page
				assert thePage.getSize()-8 == theOffset;

				long theI1 = thePageData[theOffset/4];
				long theI2 = thePageData[(theOffset/4)+1];
				
				long thePageId = theI1 + (theI2 << 32); 
				thePage = itsFile.get(thePageId);
				theOffset = 0;
			}
		}
		
		return decode(itsByteBuffer.array());
	}
	
	@Probe(key = "objects count", aggr = AggregationType.SUM)
	public long getObjectsCount()
	{
		return itsObjectsCount;
	}
	
	/**
	 * Codec for {@link InternalTuple}.
	 * @author gpothier
	 */
	private static class ObjectTupleCodec extends IndexTupleCodec<ObjectPointerTuple>
	{
		private static ObjectTupleCodec INSTANCE = new ObjectTupleCodec();

		public static ObjectTupleCodec getInstance()
		{
			return INSTANCE;
		}

		private ObjectTupleCodec()
		{
		}
		
		@Override
		public int getTupleSize()
		{
			return super.getTupleSize() 
					+ DB_PAGE_POINTER_BITS
					+ DB_PAGE_BYTEOFFSET_BITS;
		}

		@Override
		public ObjectPointerTuple read(BitStruct aBitStruct)
		{
			return new ObjectPointerTuple(aBitStruct);
		}
	}
	
	
	
	private static class ObjectPointerTuple extends IndexTuple
	{
		private long itsPageId;
		private int itsOffset;
		
		public ObjectPointerTuple(long aKey, long aPageId, int aOffset)
		{
			super(aKey);
			itsPageId = aPageId;
			itsOffset = aOffset;
		}
		
		public ObjectPointerTuple(BitStruct aBitStruct)
		{
			super(aBitStruct);
			itsPageId = aBitStruct.readLong(DB_PAGE_POINTER_BITS);
			itsOffset = aBitStruct.readInt(DB_PAGE_BYTEOFFSET_BITS);
		}

		@Override
		public void writeTo(BitStruct aBitStruct)
		{
			super.writeTo(aBitStruct);
			aBitStruct.writeLong(itsPageId, DB_PAGE_POINTER_BITS);
			aBitStruct.writeInt(itsOffset, DB_PAGE_BYTEOFFSET_BITS);
		}
		
		@Override
		public int getBitCount()
		{
			return super.getBitCount() 
					+ DB_PAGE_POINTER_BITS
					+ DB_PAGE_BYTEOFFSET_BITS;
		}
		
		public void set(long aKey, long aPageId, int aOffset)
		{
			super.set(aKey);
			itsPageId = aPageId;
			itsOffset = aOffset;
		}

		public int getOffset()
		{
			return itsOffset;
		}

		public long getPageId()
		{
			return itsPageId;
		}

	}
}
