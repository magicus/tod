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

import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;

import zz.utils.ArrayStack;
import zz.utils.RingBuffer;
import zz.utils.Utils;
import zz.utils.bit.ByteBitStruct;
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
	
	private long itsWrittenPagesCount;
	private long itsReadPagesCount;
	
	private ByteBuffer itsByteBuffer;
	private IntBuffer itsIntBufferView;
	
	public PagedFile(File aFile, int aPageSize) throws FileNotFoundException
	{
		Monitor.getInstance().register(this);
		assert itsPageSize % 4 == 0;
		
		aFile.delete();
		itsFile = new RandomAccessFile(aFile, "rw").getChannel();
		itsPageSize = aPageSize;
		itsPagesCount = 0;
		
		itsByteBuffer = ByteBuffer.allocateDirect(itsPageSize);
		itsIntBufferView = itsByteBuffer.asIntBuffer();
	}

	/**
	 * Page size, in bytes.
	 */
	public int getPageSize()
	{
		return itsPageSize;
	}
	
	/**
	 * Returns the amount of storage, in bytes, occupied by this file.
	 */
	@Probe(key = "file storage", aggr = AggregationType.SUM)
	public long getStorageSpace()
	{
		return itsPagesCount * itsPageSize;
	}
	
	@Probe(key = "file written bytes", aggr = AggregationType.SUM)
	public long getWrittenBytes()
	{
		return itsWrittenPagesCount * itsPageSize;
	}

	@Probe(key = "file read bytes", aggr = AggregationType.SUM)
	public long getReadBytes()
	{
		return itsReadPagesCount * itsPageSize;
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
	 * Returns a page object suitable for overwriting the file page
	 * of the specified id. The data of the returned page is undefined.
	 * Moreover, the actual file contents can be considered as undefined
	 * once this method has been called, because of the way page caching works:
	 * it is not guaranteed that a client can retrieve the previous content of
	 * the page even is the page is not written out.
	 */
	public Page getPageForOverwrite(long aPageId)
	{
		Reference<Page> thePageRef = itsPagesMap.get(aPageId);
		Page thePage = thePageRef != null ? thePageRef.get() : null;
		if (thePage == null)
		{
			int[] theBuffer = PageManager.getInstance().getFreeBuffer(itsPageSize, false);
			thePage = new Page(theBuffer, aPageId);
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
			
			itsReadPagesCount++;
			
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
			if (true)
			{
				int theWritten = itsFile.write(itsByteBuffer, aPage.getPageId() * itsPageSize);
				if (theWritten != itsPageSize) throw new IOException("Could not write page");
			}
			aPage.clearModified();
			
			itsWrittenPagesCount++;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Marks the given page as free. The caller should ensure that 
	 * all references to the page have been cleared.
	 * After calling this method the page can not be used anymore.
	 */
	public void freePage(Page aPage)
	{
		itsPagesMap.remove(aPage.getPageId());
		int[] theData = aPage.getData();
		aPage.tearDown();
		PageManager.getInstance().freeBuffer(theData);
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
		 * Marks this page as invalid and frees its buffer.
		 */
		private void tearDown()
		{
			itsData = null;
		}

		/**
		 * Returns the id of this page within its file.
		 */
		public long getPageId()
		{
			return itsPageId;
		}
		
		/**
		 * Returns a new {@link ByteBitStruct} backed by this page.
		 * The advantage of having the {@link ByteBitStruct} and page separate
		 * is that we can maintain several {@link ByteBitStruct}s on the same page,
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
			Monitor.getInstance().register(this);
		}
		
		private long itsRecycleCount;
		private long itsRequestsCount;
		
		private Map<Integer, RingBuffer<int[]>> itsFreeBuffers =
			new HashMap<Integer, RingBuffer<int[]>>();
		
		
		public void freeBuffer(int[] aBuffer)
		{
			RingBuffer<int[]> theBuffers = itsFreeBuffers.get(aBuffer.length*4);
			if (! theBuffers.isFull()) theBuffers.add(aBuffer);
		}
			
		/**
		 * Returns a free byte buffer of the indicated size.
		 * @param aSize The required buffer size, int bytes.
		 * @param aInitialize If true, the buffer is initialized to contain only 0s 
		 */
		public int[] getFreeBuffer(int aSize, boolean aInitialize)
		{
			itsRequestsCount++;
			
//			return new int[aSize/4];
			RingBuffer<int[]> theBuffers = itsFreeBuffers.get(aSize);
			if (theBuffers == null)
			{
				theBuffers = new RingBuffer<int[]>(128);
				itsFreeBuffers.put(aSize, theBuffers);
			}
			
			if (theBuffers.isEmpty()) return new int[aSize/4];
			else 
			{
				itsRecycleCount++;
				int[] theBuffer = theBuffers.remove();
				if (aInitialize) Utils.memset(theBuffer, 0);
				return theBuffer;
			}
		}

		@Probe(key = "pages recycled")
		public long getRecycleCount()
		{
			return itsRecycleCount;
		}

		@Probe(key = "pages requested")
		public long getRequestsCount()
		{
			return itsRequestsCount;
		}
		
		@Probe(key = "pages recycle ratio")
		public float getRecycleRatio()
		{
			return 1.0f * itsRecycleCount / itsRequestsCount;
		}
	}

}
