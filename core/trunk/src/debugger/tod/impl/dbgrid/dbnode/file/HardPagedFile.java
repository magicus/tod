/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode.file;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_BUFFER_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_SIZE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

import tod.DebugFlags;
import tod.impl.dbgrid.dbnode.file.PageBank.Page;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;
import tod.utils.NativeStream;
import zz.utils.ArrayStack;
import zz.utils.Stack;
import zz.utils.Utils;
import zz.utils.bit.ByteBitStruct;
import zz.utils.cache.MRUBuffer;
import zz.utils.list.NakedLinkedList.Entry;

/**
 * A file organized in pages.
 * @author gpothier
 */
public class HardPagedFile extends PageBank
{
	private static PageDataManager itsPageDataManager = 
		new PageDataManager(DB_PAGE_SIZE, (int) (DB_PAGE_BUFFER_SIZE/DB_PAGE_SIZE));
	
	private FileAccessor itsFileAccessor;
	private int itsPageSize;
	
	/**
	 * Number of pages currently in the file.
	 */
	private long itsPagesCount;
	
	private long itsWrittenPagesCount;
	private long itsReadPagesCount;
	
	public HardPagedFile(File aFile, int aPageSize) throws FileNotFoundException
	{
		Monitor.getInstance().register(this);
		assert itsPageSize % 4 == 0;
	
		itsPageSize = aPageSize;
		aFile.delete();
		itsFileAccessor = new FileAccessor (new RandomAccessFile(aFile, "rw"));
		itsPagesCount = 0;
	}
	
	public void unregister()
	{
		Monitor.getInstance().unregister(this);
		itsFileAccessor.unregister();
	}

	/**
	 * Page size, in bytes.
	 */
	public int getPageSize()
	{
		return itsPageSize;
	}
	
	/**
	 * Returns the size, in bits, of a page pointer
	 */
	public int getPagePointerSize()
	{
		return DB_PAGE_POINTER_BITS;
	}
	
	/**
	 * Returns the number of allocated pages.
	 */
	@Probe(key="file page count", aggr=AggregationType.SUM)
	public long getPagesCount()
	{
		return itsPagesCount;
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
	
	@Probe(key = "file written pages", aggr = AggregationType.SUM)
	public long getWrittenPages()
	{
		return itsWrittenPagesCount;
	}
	
	@Probe(key = "file read pages", aggr = AggregationType.SUM)
	public long getReadPages()
	{
		return itsReadPagesCount;
	}
	
	/**
	 * Returns a particular page of this file.
	 */
	public Page get(long aPageId)
	{
		PageKey theKey = new PageKey(this, aPageId);
		PageData theData = itsPageDataManager.get(theKey, false);
		return theData != null ? theData.getAttachedPage() : new Page(theKey);
	}
	
	@Override
	public void free(PageBank.Page aPage)
	{
		PageKey theKey = (PageKey) aPage.getKey();
		itsPageDataManager.drop(theKey);
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
//		Reference<Page> thePageRef = itsPagesMap.get(aPageId);
//		Page thePage = thePageRef != null ? thePageRef.get() : null;
//		if (thePage == null)
//		{
//			int[] theBuffer = PageManager.getInstance().getFreeBuffer(itsPageSize, false);
//			thePage = new Page(theBuffer, aPageId);
//			itsPagesMap.put(aPageId, new WeakReference<Page>(thePage));
//		}
//		
//		return thePage;
		return get(aPageId);
	}
	
	/**
	 * Creates and returns a new page in this file.
	 */
	public Page create()
	{
		itsPagesCount++;
		long thePageId = itsPagesCount-1;
		Entry<PageData> theData = itsPageDataManager.create(this, thePageId);
		Page thePage = new Page(theData);
		return thePage;
	}
	
	private int[] loadPageData(long aId)
	{
//		System.out.println("Loading page: "+aId+" on "+itsName);
		if (aId >= itsPagesCount) throw new RuntimeException("Page does not exist: "+aId+" (page count: "+itsPagesCount+")");
		
		try
		{
			int[] theBuffer = itsPageDataManager.getFreeBuffer();
			
			if (! DebugFlags.DISABLE_STORE)
			{
				itsFileAccessor.read(aId, theBuffer);
			}
			
			itsReadPagesCount++;
			
			return theBuffer;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void storePageData(long aId, int[] aData)
	{
//		System.out.println("Storing page: "+aId+" on "+itsName);
		try
		{
			if (! DebugFlags.DISABLE_STORE)
			{
				itsFileAccessor.write(aId, aData);
			}
			
			itsWrittenPagesCount++;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private class FileAccessor extends Thread
	{
		private RandomAccessFile itsFile;

		private byte[] itsReadByteBuffer;
		private byte[] itsWriteByteBuffer;
		private long itsPageId;
		private IOException itsException;
		
		private long itsReadCount = 0;
		private long itsWriteCount = 0;

		public FileAccessor(RandomAccessFile aFile)
		{
			Monitor.getInstance().register(this);
			
			itsFile = aFile;
			itsReadByteBuffer = new byte[itsPageSize];
			itsWriteByteBuffer = new byte[itsPageSize];
			itsPageId = -1;
			if (! DebugFlags.DISABLE_ASYNC_WRITES) start();
		}
		
		public void unregister()
		{
			Monitor.getInstance().unregister(this);			
		}
		
		public synchronized void read(long aId, int[] aBuffer) throws IOException
		{
			itsReadCount++;
			assert itsFile.length() >= (aId+1) * itsPageSize;
			itsFile.seek(aId * itsPageSize);
			itsFile.readFully(itsReadByteBuffer);
			
			NativeStream.b2i(itsReadByteBuffer, aBuffer);
		}
		
		public synchronized void write(long aId, int[] aData) throws IOException
		{
			itsWriteCount++;
			if (DebugFlags.DISABLE_ASYNC_WRITES)
			{
				NativeStream.i2b(aData, itsWriteByteBuffer);
				itsFile.seek(aId * itsPageSize);
				itsFile.write(itsWriteByteBuffer);
				return;
			}
			
			try
			{
				while (itsPageId >= 0) wait();
				itsPageId = aId;
				NativeStream.i2b(aData, itsWriteByteBuffer);
				IOException theException = itsException;
				itsException = null;
				notifyAll();
				
				if (theException != null) throw theException;
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public synchronized void run()
		{
			try
			{
				while (true)
				{
					while (itsPageId < 0) wait();
					try
					{
						itsFile.seek(itsPageId * itsPageSize);
						itsFile.write(itsWriteByteBuffer);
					}
					catch (IOException e)
					{
						System.err.println("Exception in file writer thread");
						itsException = e;
					}
					itsPageId = -1;
					notifyAll();
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		@Probe(key = "read/write ratio (%)", aggr = AggregationType.AVG)
		public float getWrittenPages()
		{
			return 100f*itsReadCount/itsWriteCount;
		}
	}
	
	/**
	 * Manages the collections of {@link PageData} instances.
	 * Ensures that all dirty pages are saved before 
	 * @author gpothier
	 */
	private static class PageDataManager extends MRUBuffer<PageKey, PageData>
	{
		private Stack<int[]> itsFreeBuffers = new ArrayStack<int[]>();
		private long itsCurrentSpace = 0;
		private final int itsPageSize;
		
		private PageDataManager(int aPageSize, int aBufferedPages)
		{
			super(aBufferedPages);
			itsPageSize = aPageSize;
			Monitor.getInstance().register(this);
		}
		
		public int[] getFreeBuffer()
		{
			if (itsFreeBuffers.isEmpty()) 
			{
				itsCurrentSpace += itsPageSize;
				return new int[itsPageSize/4];
			}
			else return itsFreeBuffers.pop();
		}
		
		private void addFreeBuffer(int[] aBuffer)
		{
			Utils.memset(aBuffer, 0);
			itsFreeBuffers.push(aBuffer);
		}
		
		public Entry<PageData> create(HardPagedFile aFile, long aId)
		{
			assert aFile.getPageSize() == itsPageSize;
			
			PageData theData = new PageData(
					new PageKey(aFile, aId),
					getFreeBuffer());
			
			return add(theData);
		}
		
		@Override
		protected void dropped(PageData aPageData)
		{
			aPageData.store();
			addFreeBuffer(aPageData.detach());
		}

		@Override
		protected PageData fetch(PageKey aKey)
		{
			return aKey.load();
		}

		@Override
		protected PageKey getKey(PageData aValue)
		{
			return aValue.getKey();
		}

		@Probe(key="page manager space", aggr=AggregationType.SUM)
		public long getCurrentSpace()
		{
			return itsCurrentSpace;
		}
	}
	
	public static class PageKey extends PageBank.PageKey
	{
		public PageKey(HardPagedFile aFile, long aPageId)
		{
			super(aFile, aPageId);
		}
		
		public HardPagedFile getFile()
		{
			return (HardPagedFile) getBank();
		}

		public PageData load()
		{
			int[] theData = getFile().loadPageData(getPageId());
			return new PageData(this, theData);
		}
		
		public void store(int[] aData)
		{
			getFile().storePageData(getPageId(), aData);
		}
	}
	
	/**
	 * Instances of this class hold the actual data of a page through a weak reference.
	 * @author gpothier
	 */
	private static class PageData
	{
		private PageKey itsKey;
		private List<Page> itsAttachedPages = new ArrayList<Page>(1);
		private int[] itsData;
		private boolean itsDirty = false;
		
		public PageData(PageKey aKey, int[] aData)
		{
			assert aData != null;
			itsKey = aKey;
			itsData = aData;
		}
		
		@Override
		protected void finalize() throws Throwable
		{
			if (itsDirty)
			{
				System.err.println("Finalizing dirty page: "+itsKey);
			}
		}
		
		public int[] getData()
		{
			assert itsData != null;
			return itsData;
		}
		
		public PageKey getKey()
		{
			return itsKey;
		}
		
		/**
		 * Returns the size, in bytes, of this page data.
		 */
		public int getSize()
		{
			return itsData.length * 4;
		}
		
		public void markDirty()
		{
			itsDirty = true;
		}
		
		/**
		 * Stores this page data if necessary.
		 */
		public void store() 
		{
			if (itsDirty)
			{
				itsKey.store(itsData);
				itsDirty = false;
			}
		}
		
		public void attach(Page aPage)
		{
			itsAttachedPages.add(aPage);
			if (itsAttachedPages.size() > 1) System.err.println(String.format(
					"Warning: page %d attached %d times",
					getKey().getPageId(),
					itsAttachedPages.size()));
		}
		
		/**
		 * Detaches this page data from its pages, so that it can be reclaimed.
		 */
		public int[] detach()
		{
			for (Page thePage : itsAttachedPages)
			{
				thePage.clearData();
			}
			itsAttachedPages = null;
			
			int[] theData = itsData;
			itsData = null;
			return theData;
		}
		
		/**
		 * Returns the currently attached {@link Page}.
		 */
		public Page getAttachedPage()
		{
			// There should always be at least one attached page
			// when this method is called
			return itsAttachedPages.get(0);
		}
	}
	
	public static class Page extends PageBank.Page
	{
		private Entry<PageData> itsData;
		
		private Page(PageKey aKey)
		{
			super(aKey);
			assert aKey.getPageId() < aKey.getFile().itsPagesCount;
		}
		
		private Page(Entry<PageData> aData)
		{
			this(aData.getValue().getKey());
			itsData = aData;
			itsData.getValue().attach(this);
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
		
		@Override
		public PageKey getKey()
		{
			return (PageKey) super.getKey();
		}
		
		public HardPagedFile getFile()
		{
			return getKey().getFile();
		}
		
		@Override
		public int getSize()
		{
			return getFile().getPageSize();
		}
		
		int[] getData()
		{
			if (itsData == null)
			{
				itsData = itsPageDataManager.getEntry(getKey());
				itsData.getValue().attach(this);
			}
			return itsData.getValue().getData();
		}
		
		void clearData()
		{
			itsData = null;
		}
		
		void modified()
		{
			if (itsData == null) throw new IllegalStateException("Trying to modify an absent page...");
			itsData.getValue().markDirty();
		}
		
		@Override
		public void use()
		{
			if (! DebugFlags.DISABLE_USE_PAGES && itsData != null) 
				itsPageDataManager.use(itsData);
		}
	}
	
	public static class PageBitStruct extends PageBank.PageBitStruct
	{
		public PageBitStruct(Page aPage)
		{
			// We pass null as data because we override the getData method
			super(0, aPage.getData().length, aPage);
		}
		
		@Override
		public Page getPage()
		{
			return (Page) super.getPage();
		}
		
		@Override
		protected int[] getData()
		{
			return getPage().getData();
		}
	}
	
}
