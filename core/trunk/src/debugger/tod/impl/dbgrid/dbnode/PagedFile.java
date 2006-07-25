/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zz.utils.BitStruct;

/**
 * A file organized in pages.
 * @author gpothier
 */
public class PagedFile
{
	private Map<Long, Reference<Page>> itsPagesMap = new HashMap<Long, Reference<Page>>();
	private RandomAccessFile itsFile;
	private int itsPageSize;
	
	/**
	 * Number of pages currently in the file.
	 */
	private long itsPagesCount;
	
	public PagedFile(File aFile, int aPageSize) throws FileNotFoundException
	{
		itsFile = new RandomAccessFile(aFile, "rw");
		itsPageSize = aPageSize;
		itsPagesCount = 0;
	}

	/**
	 * Returns a particular page of this file.
	 */
	public Page getPage(long aPageId)
	{
		Reference<Page> thePageRef = itsPagesMap.get(aPageId);
		Page thePage = thePageRef != null ? thePageRef.get() : null;
		if (thePage == null)
		{
			byte[] thePageData = loadPageData(aPageId);
			thePage = new Page(thePageData, aPageId);
			itsPagesMap.put(aPageId, new WeakReference<Page>(thePage));
		}
		
		return thePage;
	}
	
	/**
	 * Creates and returns a new page in this file.
	 */
	public Page createPage()
	{
		try
		{
			itsPagesCount++;
			itsFile.setLength(itsPagesCount * itsPageSize);
			byte[] thePageData = PageManager.getInstance().getFreeBuffer(itsPageSize, true);
			long thePageId = itsPagesCount-1;
			Page thePage = new Page(thePageData, thePageId);
			itsPagesMap.put(thePageId, new WeakReference<Page>(thePage));
			return thePage;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private byte[] loadPageData(long aPageId)
	{
		if (aPageId >= itsPagesCount) throw new RuntimeException("Page does not exist: "+aPageId+" (page count: "+itsPagesCount+")");
		
		try
		{
			byte[] theBuffer = PageManager.getInstance().getFreeBuffer(itsPageSize, false);
			itsFile.seek(aPageId * itsPageSize);
			itsFile.readFully(theBuffer);
			return theBuffer;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes the specified page to disk.
	 */
	public void writePage(Page aPage)
	{
		try
		{
			itsFile.seek(aPage.getPageId() * itsPageSize);
			itsFile.write(aPage.getData());
			aPage.clearModified();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
	public class Page 
	{
		private boolean itsModified;
		private long itsPageId;
		private byte[] itsData;
		
		private Page(byte[] aData, long aPageId)
		{
			itsData = aData;
			itsPageId = aPageId;
		}

		boolean isModified()
		{
			return itsModified;
		}
		
		/**
		 * Marks this page as modified.
		 */
		private void modified()
		{
			itsModified = true;
		}
		
		private void clearModified()
		{
			itsModified = false;
		}

		/**
		 * Returns the id of this page within its file.
		 */
		public long getPageId()
		{
			return itsPageId;
		}
		
		/**
		 * Returns a new {@link BitStruct} backed by this page.
		 * The advantage of having the {@link BitStruct} and page separate
		 * is that we can maintain several {@link BitStruct}s on the same page,
		 * each with a different position.
		 */
		public PageBitStruct asBitStruct()
		{
			return new PageBitStruct(this);
		}
		
		private byte[] getData()
		{
			return itsData;
		}
	}
	
	public static class PageBitStruct extends BitStruct
	{
		private Page itsPage;
		
		public PageBitStruct(Page aPage)
		{
			super(null, 0);
			itsPage = aPage;
		}

		public Page getPage()
		{
			return itsPage;
		}

		@Override
		protected void grow(int aMinSize)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		protected void setBytes(byte[] aBytes)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		protected byte[] getBytes()
		{
			return itsPage.getData();
		}

		@Override
		public void writeBoolean(boolean aValue)
		{
			super.writeBoolean(aValue);
			itsPage.modified();
		}

		@Override
		public void writeBytes(byte[] aBytes, int aBitCount)
		{
			super.writeBytes(aBytes, aBitCount);
			itsPage.modified();
		}

		@Override
		public void writeBytes(byte[] aBytes)
		{
			super.writeBytes(aBytes);
			itsPage.modified();
		}

		@Override
		public void writeInt(int aValue, int aBitCount)
		{
			super.writeInt(aValue, aBitCount);
			itsPage.modified();
		}

		@Override
		public void writeLong(long aValue, int aBitCount)
		{
			super.writeLong(aValue, aBitCount);
			itsPage.modified();
		}
	}
	
	
	
	/**
	 * A singleton class that is used to manage pages of all
	 * {@link PagedFile}s. Its main role is to manage memory.
	 * @author gpothier
	 */
	private static class PageManager
	{
		private static PageManager INSTANCE = new PageManager();

		public static PageManager getInstance()
		{
			return INSTANCE;
		}

		private PageManager()
		{
		}
		
		private Map<Integer, List<byte[]>> itsFreeBuffers =
			new HashMap<Integer, List<byte[]>>();
		
		/**
		 * Returns a free byte buffer of the indicated size.
		 * @param aSize The required buffer size.
		 * @param aInitialize If true, the buffer is initialized to contain only 0s 
		 */
		public byte[] getFreeBuffer(int aSize, boolean aInitialize)
		{
			// TODO: finish this. We must manage memory...
			return new byte[aSize];
		}
	}
}
