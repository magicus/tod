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
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

/**
 * A file organized in pages.
 * @author gpothier
 */
public class PagedFile
{
	private Map<Long, Reference<Page>> itsPagesMap = new HashMap<Long, Reference<Page>>();
	private FileChannel itsFile;
	private int itsPageSize;
	
	/**
	 * Number of pages currently in the file.
	 */
	private long itsPagesCount;
	
	private ByteBuffer itsByteBuffer;
	private IntBuffer itsIntBufferView;
	
	public PagedFile(File aFile, int aPageSize) throws FileNotFoundException
	{
		assert itsPageSize % 4 == 0;
		
		itsFile = new RandomAccessFile(aFile, "rw").getChannel();
		itsPageSize = aPageSize;
		itsPagesCount = 0;
		
		itsByteBuffer = ByteBuffer.allocateDirect(itsPageSize);
		itsIntBufferView = itsByteBuffer.asIntBuffer();
	}

	public int getPageSize()
	{
		return itsPageSize;
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
			int[] thePageData = loadPageData(aPageId);
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
		itsPagesCount++;
		int[] thePageData = PageManager.getInstance().getFreeBuffer(itsPageSize, true);
		long thePageId = itsPagesCount-1;
		Page thePage = new Page(thePageData, thePageId);
		itsPagesMap.put(thePageId, new WeakReference<Page>(thePage));
		return thePage;
	}
	
	private int[] loadPageData(long aPageId)
	{
		if (aPageId >= itsPagesCount) throw new RuntimeException("Page does not exist: "+aPageId+" (page count: "+itsPagesCount+")");
		
		try
		{
			int[] theBuffer = PageManager.getInstance().getFreeBuffer(itsPageSize, false);
			itsByteBuffer.rewind();
			
			itsFile.position(aPageId * itsPageSize);
			int theTotalReadBytes = 0;
			while (theTotalReadBytes < itsPageSize)
			{
				int theReadBytes = itsFile.read(itsByteBuffer);
				if (theReadBytes == -1) throw new IOException("Could not read page");
				theTotalReadBytes += theReadBytes;
			}
			
			itsIntBufferView.rewind();
			itsIntBufferView.get(theBuffer);
			
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
			itsIntBufferView.rewind();
			itsIntBufferView.put(aPage.getData());
			itsByteBuffer.rewind();
			int theWritten = itsFile.write(itsByteBuffer, aPage.getPageId() * itsPageSize);
			if (theWritten != itsPageSize) throw new IOException("Could not write page");
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
		private int[] itsData;
		
		private Page(int[] aData, long aPageId)
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
		
		private int[] getData()
		{
			return itsData;
		}
	}
	
	public static class PageBitStruct extends IntBitStruct
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
		protected int[] getData()
		{
			return itsPage.getData();
		}

		@Override
		protected void setData(int[] aData)
		{
			throw new UnsupportedOperationException();
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
		 * @param aSize The required buffer size, int bytes.
		 * @param aInitialize If true, the buffer is initialized to contain only 0s 
		 */
		public int[] getFreeBuffer(int aSize, boolean aInitialize)
		{
			// TODO: finish this. We must manage memory...
			return new int[aSize/4];
		}
	}

}
