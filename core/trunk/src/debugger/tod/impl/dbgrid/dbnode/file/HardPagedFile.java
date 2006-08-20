/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode.file;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;
import zz.utils.bit.ByteBitStruct;
import zz.utils.cache.MRUBuffer;

/**
 * A file organized in pages.
 * @author gpothier
 */
public class HardPagedFile extends PageBank<HardPagedFile.Page, HardPagedFile.PageBitStruct>
{
	private Map<Long, PageRef> itsPagesMap = new HashMap<Long, PageRef>();
	private ReferenceQueue<Page> itsPageRefQueue = new ReferenceQueue<Page>();
	
	private String itsName;
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
	
	public HardPagedFile(File aFile, int aPageSize) throws FileNotFoundException
	{
		Monitor.getInstance().register(this);
		assert itsPageSize % 4 == 0;
	
		itsName = aFile.getName();
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
	 * Returns the size, in bits, of a page pointer
	 */
	public int getPagePointerSize()
	{
		return DB_PAGE_POINTER_BITS;
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
	public Page get(long aPageId)
	{
		// Remove unused keys from the map.
		PageRef theQuededRef;
		while ((theQuededRef = (PageRef) itsPageRefQueue.poll()) != null)
		{
			itsPagesMap.remove(theQuededRef.getId());
		}
		
		
		PageRef thePageRef = itsPagesMap.get(aPageId);
		Page thePage = thePageRef != null ? thePageRef.get() : null;
		if (thePage == null)
		{
			thePage = new Page(aPageId);
			itsPagesMap.put(aPageId, new PageRef(thePage));
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
		PageDataManager.getInstance().create(this, thePageId);
		Page thePage = new Page(thePageId);
		itsPagesMap.put(thePageId, new PageRef(thePage));
		return thePage;
	}
	
	private int[] loadPageData(long aId)
	{
//		System.out.println("Loading page: "+aId+" on "+itsName);
		if (aId >= itsPagesCount) throw new RuntimeException("Page does not exist: "+aId+" (page count: "+itsPagesCount+")");
		
		try
		{
			int[] theBuffer = new int[itsPageSize/4];
			itsByteBuffer.rewind();
			
			assert itsFile.size() > (aId+1) * itsPageSize;
			itsFile.position(aId * itsPageSize);
			int theTotalReadBytes = 0;
			while (theTotalReadBytes < itsPageSize)
			{
				int theReadBytes = itsFile.read(itsByteBuffer);
				if (theReadBytes == -1) throw new IOException("Could not read page "+aId);
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
	
	private void storePageData(long aId, int[] aData)
	{
//		System.out.println("Storing page: "+aId+" on "+itsName);
		try
		{
			itsIntBufferView.rewind();
			itsIntBufferView.put(aData);
			itsByteBuffer.rewind();
			if (true)
			{
				int theWritten = itsFile.write(itsByteBuffer, aId * itsPageSize);
				if (theWritten != itsPageSize) throw new IOException("Could not write page");
			}
			
			itsWrittenPagesCount++;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * Manages the collections of {@link PageData} instances.
	 * @author gpothier
	 */
	private static class PageDataManager extends MRUBuffer<PageKey, PageData>
	{
		private static PageDataManager INSTANCE = new PageDataManager();

		public static PageDataManager getInstance()
		{
			return INSTANCE;
		}

		private PageDataManager()
		{
			super(-1);
		}
		
		private int itsMaxSpace = 100*1000*1000;
		private int itsCurrentSpace;
		
		public PageData create(HardPagedFile aFile, long aId)
		{
			PageData theData = new PageData(
					new PageKey(aFile, aId), 
					new int[aFile.getPageSize()/4]);
			
			add(theData);
			return theData;
		}
		
		@Override
		protected boolean shouldDrop(int aCachedItems)
		{
			return itsCurrentSpace >= itsMaxSpace;
		}
		
		@Override
		protected void added(PageData aPageData)
		{
			itsCurrentSpace += aPageData.getSize();
		}
		
		@Override
		protected void drop(PageData aPageData)
		{
			aPageData.store();
			itsCurrentSpace -= aPageData.getSize();
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
	}
	
	private static class PageKey
	{
		private final HardPagedFile itsFile;
		private final long itsPageId;
		
		public PageKey(HardPagedFile aFile, long aPageId)
		{
			itsFile = aFile;
			itsPageId = aPageId;
		}

		public PageData load()
		{
			int[] theData = itsFile.loadPageData(itsPageId);
			return new PageData(this, theData);
		}
		
		public void store(int[] aData)
		{
			itsFile.storePageData(itsPageId, aData);
		}
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((itsFile == null) ? 0 : itsFile.hashCode());
			result = prime * result + (int) (itsPageId ^ (itsPageId >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			final PageKey other = (PageKey) obj;
			if (itsFile == null)
			{
				if (other.itsFile != null) return false;
			}
			else if (!itsFile.equals(other.itsFile)) return false;
			if (itsPageId != other.itsPageId) return false;
			return true;
		}
	}
	
	/**
	 * Instances of this class hold the actual data of a page through a weak reference.
	 * @author gpothier
	 */
	private static class PageData
	{
		private PageKey itsKey;
		private Page itsPage;
		private int[] itsData;
		private boolean itsDirty = false;
		
		public PageData(PageKey aKey, int[] aData)
		{
			itsKey = aKey;
			itsData = aData;
		}
		
		@Override
		protected void finalize() throws Throwable
		{
			assert ! itsDirty;
		}
		
		public int[] getData()
		{
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
			assert itsPage == null;
			itsPage = aPage;
		}
		
		/**
		 * Detaches this page data from its page, so that it can be reclaimed.
		 */
		public void detach()
		{
			itsPage.clearData();
		}
	}
	
	public class Page extends PageBank.Page
	{
		private PageData itsData;
		
		private Page(long aPageId)
		{
			super(aPageId);
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
		
		int[] getData()
		{
			if (itsData == null)
			{
				itsData = PageDataManager.getInstance().get(new PageKey(HardPagedFile.this, getPageId()));
				itsData.attach(this);
			}
			return itsData.getData();
		}
		
		void clearData()
		{
			itsData = null;
		}
		
		void modified()
		{
			if (itsData == null) throw new IllegalStateException("Trying to modify an absent page...");
			itsData.markDirty();
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
	
	public class PageRef extends SoftReference<Page>
	{
		private long itsId;

		public PageRef(Page aPage)
		{
			super(aPage);
			itsId = aPage.getPageId();
		}

		public long getId()
		{
			return itsId;
		}
	}
	
}
