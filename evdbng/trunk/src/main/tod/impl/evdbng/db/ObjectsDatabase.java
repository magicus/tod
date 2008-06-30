/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tod.impl.evdbng.db.file.ObjectPointerTree;
import tod.impl.evdbng.db.file.ObjectPointerTuple;
import tod.impl.evdbng.db.file.PagedFile;
import tod.impl.evdbng.db.file.PagedFile.Page;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;
import zz.utils.monitoring.AggregationType;
import zz.utils.monitoring.Monitor;
import zz.utils.monitoring.Probe;

/**
 * A database for storing registered objects.
 * Each object has an associated id. It is assumed that
 * objects are sent in in their id order.
 * @author gpothier
 */
public class ObjectsDatabase
{
	private PagedFile itsObjectsFile;
	private ObjectPointerTree itsPointersTree;
	
	private long itsLastRecordedId = 0;
	private int itsDroppedObjects = 0; 
	
	/**
	 * Current data page
	 */
	private Page itsCurrentPage;
	private PageIOStream itsCurrentStruct;
	
	private long itsObjectsCount = 0;
	
	/**
	 * Create an objects database
	 * @param aIndexFile The file to use for indexes (can be shared with other structures).
	 * @param aObjectsFile The file to use to store actual object data (might be shared with other structures,
	 * but having it separate permits to have objects data in a separate file).
	 */
	public ObjectsDatabase(PagedFile aIndexFile, PagedFile aObjectsFile)
	{
		Monitor.getInstance().register(this);
		itsObjectsFile = aObjectsFile;
		itsPointersTree = new ObjectPointerTree("[ObjectsDatabase] pointers tree", aIndexFile);
		
		itsCurrentPage = itsObjectsFile.create();
		itsCurrentStruct = itsCurrentPage.asIOStream();
	}
	
	public void dispose()
	{
		Monitor.getInstance().unregister(this);
		itsObjectsFile.dispose();
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
	protected Object decode(InputStream aStream)
	{
		try
		{
			ObjectInputStream theOIStream = new ObjectInputStream(aStream);
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
		
		int theDataSize = aData.length;

		// Check if we have enough space to store data size & next page pointer
		if (itsCurrentStruct.remaining() <= PageIOStream.pagePointerSize()*2)
		{
			// Skip to next page.
			itsCurrentPage = itsObjectsFile.create();
			itsCurrentStruct = itsCurrentPage.asIOStream();
		}

		int thePageId = itsCurrentStruct.getPage().getPageId();
		int theOffset = itsCurrentStruct.getPos();
		
		itsCurrentStruct.writeInt(theDataSize);
		int theRemainingData = theDataSize;
		int theCurrentOffset = 0;
		while (theRemainingData > 0)
		{
			int theSizeOnPage = Math.min(
					itsCurrentStruct.remaining()-PageIOStream.pagePointerSize(), 
					theRemainingData);
			
			itsCurrentStruct.writeBytes(aData, theCurrentOffset, theSizeOnPage);
			theRemainingData -= theSizeOnPage;
			theCurrentOffset += theSizeOnPage;
			
			if (theRemainingData > 0)
			{
				// Link to next page
				Page theNextPage = itsObjectsFile.create();
				PageIOStream theNextStruct = theNextPage.asIOStream();
				assert itsCurrentStruct.getPos() == itsCurrentStruct.size() - PageIOStream.pagePointerSize();
				itsCurrentStruct.writePagePointer(theNextPage.getPageId());
				
				itsCurrentPage = theNextPage;
				itsCurrentStruct = theNextStruct;
			}
			
			itsCurrentPage.use();
		}
		
		// Add index tuple
		itsPointersTree.add(aId, thePageId, theOffset);
		
		itsLastRecordedId = aId;
	}
	
	/**
	 * Loads an object from the database.
	 * @param aId Id of the object to load.
	 */
	public Object load(long aId)
	{
		ObjectPointerTuple theTuple = itsPointersTree.getTupleAt(aId, null);
		if (theTuple == null) return null;
		
		int theOffset = theTuple.getOffset();
		Page thePage = itsObjectsFile.get(theTuple.getPageId());
		PageIOStream theStruct = thePage.asIOStream();
		
		theStruct.setPos(theOffset);
		int theDataSize = theStruct.readInt();
		
		PageListInputStream theStream = new PageListInputStream(theStruct, theDataSize);
		
		return decode(theStream);
	}
	
	@Probe(key = "objects count", aggr = AggregationType.SUM)
	public long getObjectsCount()
	{
		return itsObjectsCount;
	}
	
	/**
	 * An input stream that wraps around a linked list of pages.
	 * @author gpothier
	 */
	private static class PageListInputStream extends InputStream
	{
		private PageIOStream itsStruct;
		
		/**
		 * Total remaining data.
		 */
		private int itsRemainingData;

		public PageListInputStream(PageIOStream aStruct, int aRemainingData)
		{
			itsStruct = aStruct;
			itsRemainingData = aRemainingData;
		}

		@Override
		public int available() 
		{
			return Math.min(itsStruct.remaining(), itsRemainingData);
		}

		/**
		 * If the end of the current page is reached, load next page.
		 */
		private void checkPageEnd()
		{
			if (itsStruct.remaining() <= PageIOStream.pagePointerSize())
			{
				assert itsStruct.remaining() == PageIOStream.pagePointerSize();
				int theNextPageId = itsStruct.readPagePointer();
				Page theNextPage = itsStruct.getPage().getFile().get(theNextPageId);
				itsStruct = theNextPage.asIOStream();
			}
		}
		
		@Override
		public int read() 
		{
			assert itsRemainingData >= 0;
			if (itsRemainingData == 0) return -1;

			checkPageEnd();
			itsRemainingData--;
			return itsStruct.readByte();
		}

		@Override
		public int read(byte[] aBuffer, int aOff, int aLen)
		{
			assert itsRemainingData >= 0;
			if (itsRemainingData == 0) return -1;
			
			checkPageEnd();
			int theSizeToRead = Math.min(aLen, itsStruct.remaining()-4);
			itsRemainingData -= theSizeToRead;
			itsStruct.readBytes(aBuffer, aOff, theSizeToRead);
			return theSizeToRead;
		}
	}
}
