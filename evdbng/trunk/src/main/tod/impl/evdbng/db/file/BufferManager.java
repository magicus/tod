/*
 * Created on Jan 28, 2008
 */
package tod.impl.evdbng.db.file;

import static tod.impl.evdbng.DebuggerGridConfigNG.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.BitSet;

import tod.impl.evdbng.db.file.PagedFile.Page;
import zz.utils.Utils;
import zz.utils.list.NakedLinkedList;
import zz.utils.list.NakedLinkedList.Entry;
import zz.utils.monitoring.AggregationType;
import zz.utils.monitoring.Monitor;
import zz.utils.monitoring.Probe;
import zz.utils.primitive.FixedIntStack;

/**
 * Manages the shared buffer of {@link PagedFile}s.
 * @author gpothier
 */
public class BufferManager
{
	private static BufferManager INSTANCE = new BufferManager();

	public static BufferManager getInstance()
	{
		return INSTANCE;
	}

	/**
	 * This buffer spans all the pages of this page manager.
	 */
	private final ByteBuffer itsBuffer;
	
	private final int itsPageSize;
	private final int itsBufferCount;

	/**
	 * Buffers that have been modified and thus need to be stored are marked
	 * by a bit in this set. 
	 */
	private final BitSet itsDirtyBuffers = new BitSet();
	
	/**
	 * The page attached to each buffer.
	 */
	private final Page[] itsAttachedPages;
	
	private long itsReadCount = 0;
	private long itsWriteCount = 0;

	
	private PageReplacementAlgorithm itsPageReplacementAlgorithm;

	private BufferManager()
	{
		itsPageSize = DB_PAGE_SIZE;
		itsBufferCount = (int) (DB_PAGE_BUFFER_SIZE/DB_PAGE_SIZE);
		
		int theSize = itsPageSize*itsBufferCount;
		try
		{
			Utils.println("Allocating %d buffers (%d bytes).", itsBufferCount, theSize);
			itsBuffer = ByteBuffer.allocateDirect(theSize);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Cannot allocate buffer of size: "+theSize, e);
		}
		itsBuffer.order(ByteOrder.nativeOrder());
		
		itsAttachedPages = new Page[itsBufferCount];
		
		itsPageReplacementAlgorithm = new LRUAlgorithm(itsBufferCount);
		
		Monitor.getInstance().register(this);
	}
	
	public ByteBuffer getBuffer()
	{
		return itsBuffer;
	}
	
	public int getPageSize()
	{
		return itsPageSize;
	}
	
	/**
	 * Returns a view of the main buffer with position and limit set to the beginning
	 * and end of the buffer for the specified id.
	 */
	private ByteBuffer getPageData(int aBufferId)
	{
		assert aBufferId >= 0 && aBufferId < itsBufferCount : String.format("bid: %d, count: %d", aBufferId, itsBufferCount);
		ByteBuffer thePageData = itsBuffer.duplicate();
		int theBufferPos = aBufferId * itsPageSize;
		assert theBufferPos >= 0 && theBufferPos < itsBuffer.limit(): String.format("bufferPos: %d, limit: %d", theBufferPos, itsBuffer.limit());
		thePageData.position(theBufferPos);
		thePageData.limit(theBufferPos+itsPageSize);
		return thePageData;
	}
	
	/**
	 * Returns the id of a free buffer.
	 */
	private int getFreeBuffer()
	{
		return itsPageReplacementAlgorithm.getFreeBuffer();
	}

	/**
	 * Frees the specified buffer, saves it to the file if dirty, and notifies the attached
	 * page.
	 */
	private synchronized void freeBuffer(int aBufferId)
	{
		Page thePage = itsAttachedPages[aBufferId];
		if (thePage == null) return; // already free.
		
		assert thePage.getBufferId() == aBufferId;
		
		if (itsDirtyBuffers.get(aBufferId))
		{
			assert thePage.getBufferId() >= 0 : "Page #"+thePage.getPageId()+" @"+aBufferId;
			thePage.getFile().write(thePage);
			itsDirtyBuffers.clear(aBufferId);
		}

		thePage.pagedOut();
		itsAttachedPages[aBufferId] = null;
		
		itsPageReplacementAlgorithm.bufferFreed(aBufferId);
	}
	
	/**
	 * Creates a new page.
	 */
	public synchronized Page create(PagedFile aFile, int aPageId)
	{
		int theBufferId = getFreeBuffer();
		ByteBuffer thePageData = getPageData(theBufferId);
		
		// Clear the page
		LongBuffer theLongBuffer = thePageData.asLongBuffer();
		for (int i=0;i<itsPageSize/8;i++) theLongBuffer.put(0);
		
		Page thePage = aFile.new Page(theBufferId, aPageId);
		assert itsAttachedPages[theBufferId] == null;
		itsAttachedPages[theBufferId] = thePage;
		
		return thePage;
	}

	/**
	 * Flushes all dirty buffers to disk
	 */
	public synchronized void flush()
	{
		for (int i=0;i<itsBufferCount;i++)
		{
			if (itsAttachedPages[i] != null) freeBuffer(i);
		}
	}
	
	/**
	 * Flushed all the buffers that pertain to the given file.
	 */
	public synchronized void flush(PagedFile aFile)
	{
		for (int i=0;i<itsBufferCount;i++)
		{
			Page thePage = itsAttachedPages[i];
			if (thePage != null && thePage.getFile() == aFile) freeBuffer(i);
		}
	}
	
	/**
	 * Invalidates all the pages of the specified file.
	 */
	public synchronized void invalidatePages(PagedFile aFile)
	{
		for (Page thePage : itsAttachedPages) 
		{
			if (thePage != null && thePage.getFile() == aFile) thePage.invalidate();
		}
	}

	/**
	 * Reloads a page from the disk. It is assumed that no buffer already holds this page.
	 */
	synchronized void loadPage(Page aPage)
	{
		int theBufferId = getFreeBuffer();
		aPage.getFile().read(aPage, theBufferId);
		
		assert itsAttachedPages[theBufferId] == null;
		itsAttachedPages[theBufferId] = aPage;
		aPage.pagedIn(theBufferId);
	}
	
	/**
	 * Registers an access of the given buffer.
	 */
	public void use(int aBufferId)
	{
		itsPageReplacementAlgorithm.use(aBufferId);
	}
	
	/**
	 * Indicates to the page manager that this page is not going to be used anymore.
	 * This is optional, not calling it has no adverse effects, and the effect of calling
	 * it is a potiential increase in efficiency.
	 */
	public synchronized void free(Page aPage)
	{
		int theBufferId = aPage.getBufferId();
		if (theBufferId != -1) itsPageReplacementAlgorithm.free(theBufferId);
	}
	
	/**
	 * Marks the specified buffer as dirty. 
	 */
	public synchronized void modified(int aBufferId)
	{
		itsDirtyBuffers.set(aBufferId);
		use(aBufferId);
	} 
	
	private void printBuffer(
			String aLabel, 
			Page aPage, 
			int thePhysicalPageId,
			int aBufferId)
	{
		if (true) return;
		
		StringBuilder theBuilder = new StringBuilder(String.format(
				"%s [%s] pid: %d, ppid: %d, bid: %d - ",
				aLabel,
				aPage.getFile().getName(),
				aPage.getPageId(),
				thePhysicalPageId,
				aBufferId));
		
		ByteBuffer theBuffer = getPageData(aBufferId);

		int p = theBuffer.position();
		for (int i = 0; i < itsPageSize; i++)
		{
			if (i % 16 == 0) theBuilder.append("["+i+"] ");
			
			String theHexString = Integer.toHexString(theBuffer.get(p+i) & 0xff);
			if (theHexString.length() == 1) theBuilder.append('0');
			theBuilder.append(theHexString);
			theBuilder.append(' ');
		}

		System.out.println(theBuilder.toString());
	}
	
	/**
	 * Writes a particular page to the disk
	 * @param aPhysPageId Physical id of the page (location on disk), 
	 * might be different from the logical page id stored in the Page.
	 */
	public synchronized void write(Page aPage, int aPhysPageId)
	{
		ByteBuffer thePageData = getPageData(aPage.getBufferId());
		printBuffer("w", aPage, aPhysPageId, aPage.getBufferId());
		
		long thePagePos = ((long) aPhysPageId) * ((long) itsPageSize);
		int theRemaining = itsPageSize;
		try
		{
			while (theRemaining > 0)
			{
				int theWritten = aPage.getFile().getChannel().write(thePageData, thePagePos);
				theRemaining -= theWritten;
				thePagePos += theWritten;
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
		itsWriteCount++;
	}
	
	/**
	 * Reads a page to the disk
	 * @param aPhysPageId Id of the page on disk
	 */
	public synchronized void read(Page aPage, int aPhysPageId, int aBufferId)
	{
		ByteBuffer thePageData = getPageData(aBufferId);
		
		long thePagePos = ((long) aPhysPageId) * ((long) itsPageSize);
		assert thePagePos >= 0 : thePagePos;
		int theRemaining = itsPageSize;
		try
		{
			while (theRemaining > 0)
			{
				int theRead = aPage.getFile().getChannel().read(thePageData, thePagePos);
				assert theRead >= 0 : "theRead: "+theRead+", thePagePos: "+thePagePos+", aPageId: "+aPhysPageId;
				theRemaining -= theRead;
				thePagePos += theRead;
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
		printBuffer("r", aPage, aPhysPageId, aBufferId);
		itsReadCount++;
	}

	@Probe(key = "buffer count", aggr = AggregationType.SUM)
	public long getBufferCount()
	{
		return itsBufferCount;
	}
	
	@Probe(key = "buffer space", aggr = AggregationType.SUM)
	public long getBufferSpace()
	{
		return itsBufferCount*itsPageSize;
	}
	

	
	/**
	 * Abstract paging algorithm. Decides which buffers to page out when
	 * new buffers are requested.
	 * @author gpothier
	 */
	private abstract class PageReplacementAlgorithm
	{
		
		protected void freeBuffer(int aBufferId)
		{
			BufferManager.this.freeBuffer(aBufferId);
		}

		/**
		 * Indicates that the specified buffer has been accessed. This method
		 * is called very often so it should execute fast.
		 */
		public abstract void use(int aBufferId);
		
		/**
		 * Indicates that the specified buffer will not be used in the near future.
		 */
		public abstract void free(int aBufferId);
		
		/**
		 * Returns a free buffer, paging out other buffers if necessary.
		 */
		public abstract int getFreeBuffer();
		
		/**
		 * Called when a buffer has been freed so as to update the algorithm's state.
		 */
		public abstract void bufferFreed(int aBufferId);
		
		/**
		 * Clears all stored data.
		 */
		public abstract void clear();
	}

	/**
	 * From Wikipedia: http://en.wikipedia.org/wiki/Page_replacement_algorithm#Aging
	 * @author gpothier
	 */
	private class AgingAlgorithm extends PageReplacementAlgorithm
	{
		/**
		 * Number of page accesses between each "clock cycle" (aging algorithm).
		 */
		private static final int ACCESSES_PER_CYCLE = 100000;
		
		/**
		 * Ids of free pages
		 */
		private final FixedIntStack itsFreeBuffersIds;
		
		/**
		 * Buffers accessed during the last "clock cycle" are marked with a bit
		 * in this set (aging algorithm). 
		 */
		private final BitSet itsAccessedBuffers = new BitSet();
		
		/**
		 * There is a counter for each buffer. At the end of each "clock cycle", the counters are
		 * updated:
		 * c = (c >>> 1) | (accessed ? 0x80 : 0)
		 * Thus the least frequently & recently used buffers have the lowest counter values
		 * (aging algorithm).
		 */
		private final byte[] itsCounters;
		
		/**
		 * Number of counters for each possible counter value. 
		 * At the end of each "clock cycle", when the counters are updated the counter bins
		 * are updated accordingly. This is used to speed up the collection of free
		 * pages.
		 */
		private final int[] itsCounterBins = new int[256];
		
		private final int itsBufferCount;

		public AgingAlgorithm(int aBufferCount)
		{
			itsBufferCount = aBufferCount;
			
			itsCounters = new byte[itsBufferCount];
			itsFreeBuffersIds = new FixedIntStack(itsBufferCount);
		}
		
		@Override
		public void clear()
		{
			// Mark all buffers free and clear counters
			itsFreeBuffersIds.clear();
			for (int i=0;i<itsBufferCount;i++) 
			{
				itsFreeBuffersIds.push(i);
				itsCounters[i] = 0;
			}
			
			// Reset counter bins
			for (int i=0;i<itsCounterBins.length;i++) itsCounterBins[i] = 0;
			
			itsAccessedBuffers.clear();
		}
		
		@Override
		public void use(int aBufferId)
		{
			itsAccessedBuffers.set(aBufferId);
		}
		
		@Override
		public void free(int aBufferId)
		{
			itsAccessedBuffers.clear(aBufferId);
			byte c = itsCounters[aBufferId];
			itsCounterBins[c & 0xff]--;
			itsCounters[aBufferId] = 0;
			itsCounterBins[0]++;
		}
		
		@Override
		public int getFreeBuffer()
		{
			if (itsFreeBuffersIds.isEmpty())
			{
				freeNBuffers((itsBufferCount/20) + 1);
			}
			return itsFreeBuffersIds.pop();
		}
		
		@Override
		public void bufferFreed(int aBufferId)
		{
			itsAccessedBuffers.clear(aBufferId);
			byte c = itsCounters[aBufferId];
			itsCounterBins[c & 0xff]--;
			itsCounters[aBufferId] = 0;
			itsCounterBins[0]++;
			
			itsFreeBuffersIds.push(aBufferId);
		}

		private synchronized void updateUsage()
		{
//			if (itsRemainingAccesses > 0) return; // In case of concurrent invocation, don't reexecute
//			itsRemainingAccesses = ACCESSES_PER_CYCLE;
		
			long t0 = System.currentTimeMillis();
			for(int i=0;i<itsCounterBins.length;i++) itsCounterBins[i] = 0;
			
			for(int i=0;i<itsBufferCount;i++)
			{
				byte c = itsCounters[i];
				c = (byte) ((c & 0xff) >>> 1);
				boolean theAccessed = itsAccessedBuffers.get(i);
				if (theAccessed) c |= 0x80;
				itsCounters[i] = c;
			
				itsCounterBins[c & 0xff]++;
			}
			
			itsAccessedBuffers.clear();
			
			long t1 = System.currentTimeMillis();
			
			
			long t = t1-t0;
			
//			System.out.println("updateUsage executed in "+t+"ms.");
		}
		
		/**
		 * Frees at least N of the least used buffers.
		 * @param aCount The minumum number of buffers to free
		 */
		private void freeNBuffers(int aCount)
		{
			updateUsage();
			
			int theSum = 0;
			int theTreshold = 0;
			for (int i=0;i<256;i++)
			{
				theTreshold = i;
				theSum += itsCounterBins[i];
				if (theSum >= aCount) break;
			}
			
			int theFreed = 0;
			for (int i=0;i<itsBufferCount;i++) 
			{
				byte c = itsCounters[i];
				if (c <= theTreshold) 
				{
					freeBuffer(i);
					theFreed++;
				}
				if (theFreed >= aCount) break;
			}
		}
	}
	
	private class LRUAlgorithm extends PageReplacementAlgorithm
	{
		/**
		 * Most recently used items go to the tail of the list.
		 */
		private final NakedLinkedList<Integer> itsLRUList = new NakedLinkedList<Integer>();

		private final Entry<Integer>[] itsEntries;
		
		public LRUAlgorithm(int aBufferCount)
		{
			itsEntries = new Entry[aBufferCount];
			for (int i=0;i<itsEntries.length;i++) 
			{
				Entry<Integer> theEntry = itsLRUList.createEntry(i);
				itsEntries[i] = theEntry;
				itsLRUList.addFirst(theEntry);
			}
		}

		@Override
		public void clear()
		{
			// It is not necessary to reset the lru list
		}

		@Override
		public void use(int aBufferId)
		{
			Entry<Integer> theEntry = itsEntries[aBufferId];
			
			assert theEntry.isAttached();
			itsLRUList.moveLast(theEntry);
		}
		
		@Override
		public void free(int aBufferId)
		{
			Entry<Integer> theEntry = itsEntries[aBufferId];
			
			assert theEntry.isAttached();
			itsLRUList.moveFirst(theEntry);
		}
		
		@Override
		public void bufferFreed(int aBufferId)
		{
		}

		@Override
		public int getFreeBuffer()
		{
			Entry<Integer> theEntry = itsLRUList.getFirstEntry();
			int theBufferId = theEntry.getValue();

			freeBuffer(theBufferId);
			assert theEntry.isAttached();
			itsLRUList.moveLast(theEntry);
			
			return theBufferId;
		}
	}


}
