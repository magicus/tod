package tod.impl.evdbng.db.fieldwriteindex;

import tod.impl.evdbng.db.file.DeltaBTree;
import tod.impl.evdbng.db.file.Page;
import tod.impl.evdbng.db.file.Page.BooleanSlot;
import tod.impl.evdbng.db.file.Page.ByteSlot;
import tod.impl.evdbng.db.file.Page.IntSlot;
import tod.impl.evdbng.db.file.Page.PageIOStream;
import tod.impl.evdbng.db.file.Page.PidSlot;
import tod.impl.evdbng.db.file.Page.UnsignedByteSlot;
import tod.impl.evdbng.db.file.PagedFile;
import zz.utils.Utils;
import zz.utils.cache.MRUBuffer;

public class OnDiskIndex
{
	private static final int[] OBJECTS_PER_SHAREDPAGE = {128, 64, 32, 16, 8, 4, 2};
	private static final int N_LOG2_OBJECTSPERPAGE = OBJECTS_PER_SHAREDPAGE.length;
	
	private static final boolean LOG = false;
	
	private final PagedFile itsFile;
	private Directory itsDirectory;
	private PageStack itsEmptyPagesStack;
	
	/**
	 * Stacks of non-full shared pages.
	 */
	private PageStack[] itsSharedPagesStack = new PageStack[N_LOG2_OBJECTSPERPAGE]; 

	private final ObjectBTree itsObjectBTree;
	private final ObjectAccessStoreCache itsObjectAccessStoreCache = new ObjectAccessStoreCache();

	public OnDiskIndex(PidSlot aDirectoryPageSlot)
	{
		itsFile = aDirectoryPageSlot.getFile();
		itsDirectory = new Directory(aDirectoryPageSlot.getPage(true));
		
		itsObjectBTree = new ObjectBTree("objects map", itsDirectory.getObjectMapSlot());

		itsEmptyPagesStack = new PageStack(itsDirectory.getEmptyPagesStackSlot());
		for(int i=0;i<N_LOG2_OBJECTSPERPAGE;i++)
			itsSharedPagesStack[i] = new PageStack(itsDirectory.getIncompleteSharedPagesStackSlot(i));
	}
	
	private PagedFile getFile()
	{
		return itsFile;
	}
	
	public ObjectAccessStore getStore(long aObjectFieldId, boolean aReadOnly)
	{
		return itsObjectAccessStoreCache.get(aReadOnly ? -aObjectFieldId : aObjectFieldId);
	}
	
	/**
	 * Really creates the {@link ObjectAccessStore}; only called when the OAS is not
	 * found in the cache.
	 * @param aObjectFieldId Negative value means readonly.
	 */
	private ObjectAccessStore createStore(long aObjectFieldId)
	{
		if (aObjectFieldId > 0)
		{
			ObjectPageSlot theSlot = itsObjectBTree.getSlot(aObjectFieldId);
			return new ObjectAccessStore(aObjectFieldId, theSlot);
		}
		else
		{
			// Read-only
			int thePid = itsObjectBTree.get(-aObjectFieldId);
			if (thePid == 0) return new ObjectAccessStore(0, null);
			else throw new UnsupportedOperationException("Not yet");
		}
	}
	
	private Page getEmptyPage()
	{
		int thePid = itsEmptyPagesStack.pop();
		if (thePid == 0) return getFile().create();
		else return getFile().get(thePid);
	}
	
	/**
	 * Returns a non-full shared page of the specified size. 
	 * @param aLogMaxCount An index into {@link #OBJECTS_PER_SHAREDPAGE} that indicates 
	 * the number of objects per page
	 */
	private SharedPage getSharedPage(int aLogMaxCount)
	{
		assert aLogMaxCount >= 0 && aLogMaxCount < N_LOG2_OBJECTSPERPAGE;
		PageStack theStack = itsSharedPagesStack[aLogMaxCount];
		Page thePage;
		SharedPage theSharedPage;
		if (theStack.isEmpty())
		{
			thePage = getEmptyPage();
			thePage.writeByte(0, aLogMaxCount);
			theStack.push(thePage.getPageId());
//			if (LOG) Utils.println("New shared page: %d [%d]", thePage.getPageId(), aLogMaxCount);
			theSharedPage = new SharedPage(thePage);
			theSharedPage.markFree(true);
		}
		else
		{
			int thePid = theStack.peek();
			thePage = getFile().get(thePid);
//			if (LOG) Utils.println("Reusing shared page: %d [%d]", thePage.getPageId(), aLogMaxCount);
			theSharedPage = new SharedPage(thePage);
			assert theSharedPage.isMarkedFree();
			assert theSharedPage.hasFreeIds();
		}
		return theSharedPage;
	}
	
	private void unfreeSharedPage(SharedPage aPage)
	{
		assert ! aPage.hasFreeIds();
		if (! aPage.isMarkedFree()) return;
//		if (LOG) Utils.println("Unfree shared page: %d", aPage.getPageId());
		int theLogMaxCount = aPage.getLogMaxCount();
		PageStack theStack = itsSharedPagesStack[theLogMaxCount];
		int thePid = theStack.pop();
		assert thePid == aPage.getPageId();
		aPage.markFree(false);
	}
	
	private void freeSharedPage(SharedPage aPage)
	{
		assert aPage.hasFreeIds();
		if (aPage.isMarkedFree()) return;
//		if (LOG) Utils.println("Free shared page: %d", aPage.getPageId());
		int theLogMaxCount = aPage.getLogMaxCount();
		PageStack theStack = itsSharedPagesStack[theLogMaxCount];
		theStack.push(aPage.getPageId());
		aPage.markFree(true);
	}
	
	private class Directory
	{
		private static final int POS_PAGECOUNT = 0;
		private static final int POS_EMPTYPAGESSTACK = 4;
		private static final int POS_OBJECTMAP = 8;
		private static final int POS_SHAREDPAGES_START = 12;
		
		private final Page itsPage;
		private final IntSlot itsCountSlot;
		private final PidSlot itsEmptyPagesSlot;
		private final PidSlot itsObjectMapSlot;
		private final PidSlot[] itsSharedPagesSlots;
		
		public Directory(Page aPage)
		{
			itsPage = aPage;
			
			itsCountSlot = new IntSlot(itsPage, POS_PAGECOUNT);
			itsEmptyPagesSlot = new PidSlot(itsPage, POS_EMPTYPAGESSTACK);
			itsObjectMapSlot = new PidSlot(itsPage, POS_OBJECTMAP);
			
			itsSharedPagesSlots = new PidSlot[N_LOG2_OBJECTSPERPAGE];
			for(int i=0;i<itsSharedPagesSlots.length;i++)
				itsSharedPagesSlots[i] = new PidSlot(itsPage, POS_SHAREDPAGES_START+4*i);
		}

		public IntSlot getCountSlot()
		{
			return itsCountSlot;
		}

		public PidSlot getEmptyPagesStackSlot()
		{
			return itsEmptyPagesSlot;
		}

		public PidSlot getObjectMapSlot()
		{
			return itsObjectMapSlot;
		}

		public PidSlot getIncompleteSharedPagesStackSlot(int aObjectsPerPage_Log2)
		{
			return itsSharedPagesSlots[aObjectsPerPage_Log2];
		}
	}
	
	
	/**
	 * Maintains a stack of page ids.
	 * Format: 
	 *   previous page id: int
	 *   next page id: int
	 *   count: int
	 *   ids: int[]
	 * @author gpothier
	 */
	private class PageStack
	{
		private static final int POS_PREV_ID = 0;
		private static final int POS_NEXT_ID = 4;
		private static final int POS_COUNT = 8;
		private static final int POS_DATA = 12;
		
		private static final int ITEMS_PER_PAGE = (PagedFile.PAGE_SIZE - POS_DATA)/4;
		
		private final PidSlot itsCurrentPageSlot;
		private Page itsCurrentPage;
		
		private PidSlot itsNextPageSlot = new PidSlot();
		private PidSlot itsPrevPageSlot = new PidSlot();
		private IntSlot itsCountSlot = new IntSlot();

		public PageStack(PidSlot aCurrentPageSlot)
		{
			itsCurrentPageSlot = aCurrentPageSlot;
			setCurrentPage(itsCurrentPageSlot.getPage(true));
		}
		
		private void setCurrentPage(Page aPage)
		{
			itsCurrentPage = aPage;
			itsNextPageSlot.setup(itsCurrentPage, POS_NEXT_ID);
			itsPrevPageSlot.setup(itsCurrentPage, POS_PREV_ID);
			itsCountSlot.setup(itsCurrentPage, POS_COUNT);
			
			itsCurrentPageSlot.setPage(aPage);
		}
		
		public boolean isEmpty()
		{
			return itsCountSlot.get() == 0 && ! itsPrevPageSlot.hasPage();
		}
		
		public void push(int aPid)
		{
			int theCount = itsCountSlot.get();
			if (theCount >= ITEMS_PER_PAGE)
			{
				Page theNextPage = itsNextPageSlot.getPage(false);
				if (theNextPage == null)
				{
					theNextPage = itsFile.create();
					theNextPage.writeInt(POS_PREV_ID, itsCurrentPage.getPageId());
					itsNextPageSlot.setPage(theNextPage);
				}
				setCurrentPage(theNextPage);
				theCount = itsCountSlot.get();
				assert theCount == 0;
			}
			
			itsCurrentPage.writeInt(POS_DATA + 4*theCount, aPid);
			itsCountSlot.set(theCount+1);
		}
		
		public int pop()
		{
			int theCount = itsCountSlot.get();
			if (theCount <= 0)
			{
				Page thePrevPage = itsPrevPageSlot.getPage(false);
				if (thePrevPage == null) return 0;
				else
				{
					setCurrentPage(thePrevPage);
					theCount = itsCountSlot.get();
					assert theCount == ITEMS_PER_PAGE;
				}
			}
			
			theCount--;
			itsCountSlot.set(theCount);
			return itsCurrentPage.readInt(POS_DATA + 4*theCount);
		}
		
		public int peek()
		{
			int theCount = itsCountSlot.get();
			if (theCount <= 0)
			{
				Page thePrevPage = itsPrevPageSlot.getPage(false);
				setCurrentPage(thePrevPage);
				theCount = itsCountSlot.get();
				assert theCount == ITEMS_PER_PAGE;
			}
			
			theCount--;
			return itsCurrentPage.readInt(POS_DATA + 4*theCount);
		}
	}
	
	/**
	 * A slot that permits to store a pointer to a data structure that
	 * stores the accesses of a particular object.
	 * There are two kinds of such data structures:
	 * - For objects that have few accesses, a shared page is used
	 * - For objects that have many accesses, a {@link DeltaBTree} is used.
	 * To differentiate the structures we alter the page id that is actually
	 * stored by this slot. We extend {@link PidSlot} so that the {@link DeltaBTree}
	 * can store its current root page without being concerned about how to store
	 * the page id.
	 * @author gpothier
	 */
	public static class ObjectPageSlot extends PidSlot
	{
		public ObjectPageSlot(PageIOStream aStream)
		{
			super(aStream.getPage(), aStream.getPos());
		}
		
		@Override
		public Page getPage(boolean aCreateIfNull)
		{
			// This method should be called only if the store for the object is a delta BTree
			assert ! aCreateIfNull;
			int thePid = getPid() >>> 1;
			return getFile().get(thePid);
		}
		
		@Override
		public void setPage(Page aPage)
		{
			// This method should be called only if the store for the object is a delta BTree
			setPid(aPage.getPageId() << 1);
		}

		@Override
		public int getPid()
		{
			return super.getPid();
		}

		@Override
		public void setPid(int aPid)
		{
			super.setPid(aPid);
		}
		
		public boolean isNull()
		{
			return getPid() == 0;
		}
		
		public boolean isSharedPage()
		{
			return (getPid() & 0x1) != 0;
		}
		
		public boolean isBTree()
		{
			return (getPid() & 0x1) != 0;
		}
		
		public SharedPage getSharedPage(OnDiskIndex aIndex)
		{
			int thePid = getPid() >>> 1;
			Page thePage = getFile().get(thePid);
			return aIndex.new SharedPage(thePage);
		}
		
		public void setSharedPage(SharedPage aPage)
		{
			setPid((aPage.getPageId() << 1) + 1);
		}
		
		public DeltaBTree getBTree()
		{
			return new DeltaBTree("oas", getFile(), this);
		}
		
		public DeltaBTree toBTree()
		{
			Page theRoot = getFile().create();
			setPid(theRoot.getPageId() << 1);
			return new DeltaBTree("oas", getFile(), this);
		}
	}
	

	
	public class SharedPage
	{
		private Page itsPage;
		
		/**
		 * An index into {@link OnDiskIndex#OBJECTS_PER_SHAREDPAGE}, that indicates 
		 * the maximum number of objects stored in this page.
		 */
		private final int itsLogMaxCount;
		
		/**
		 * Number of objects currently stored in this page 
		 */
		private UnsignedByteSlot itsCurrentCount;
		
		private BooleanSlot itsMarkedFree;
		
		private long itsLastObjectFieldId = 0;
		private int itsLastIndex = -1;
		
		public SharedPage(Page aPage)
		{
			itsPage = aPage;
			itsLogMaxCount = aPage.readByte(0);
			assert itsLogMaxCount >= 0 && itsLogMaxCount < N_LOG2_OBJECTSPERPAGE : ""+itsLogMaxCount;
			itsCurrentCount = new UnsignedByteSlot(aPage, 1);
			itsMarkedFree = new BooleanSlot(aPage, 2);
			if (LOG) Utils.println("Shared page (<init>) %d: %d/%d", getPageId(), getCurrentCount(), getMaxCount());
			check();
		}
		
		private void check()
		{
			assert itsPage.readByte(3) == 0;
		}
		
		private void checkCount()
		{
			int theCount = 0;
			for(int i=0;i<getMaxCount();i++)
			{
				long theId = getId(i);
				if (theId != 0) theCount++;
			}
			assert theCount == getCurrentCount(): theCount+", "+getCurrentCount();
		}
		
		public boolean isMarkedFree()
		{
			return itsMarkedFree.get();
		}
		
		public void markFree(boolean aFree)
		{
			itsMarkedFree.set(aFree);
		}
		
		public int getPageId()
		{
			return itsPage.getPageId();
		}
		
		public int getLogMaxCount()
		{
			return itsLogMaxCount;
		}
		
		/**
		 * Maximum number of ids in the page
		 */
		public int getMaxCount()
		{
			return OnDiskIndex.OBJECTS_PER_SHAREDPAGE[itsLogMaxCount];
		}
		
		/**
		 * Current number of ids in the page.
		 */
		public int getCurrentCount()
		{
			return itsCurrentCount.get();
		}
		
		/**
		 * Whether this page has free id slots. 
		 */
		public boolean hasFreeIds()
		{
			return getCurrentCount() < getMaxCount();
		}
		
		public void setCurrentCount(int aCount)
		{
			itsCurrentCount.set(aCount);
			check();
		}
		
		private int getIdsOffset()
		{
			return 4;
		}
		
		private int getCountsOffset()
		{
			return getIdsOffset()+getMaxCount()*8;
		}
		
		private int getValuesOffset()
		{
			return getCountsOffset()+getMaxCount();
		}
		
		/**
		 * Size of a single value
		 */
		private int getValueSize()
		{
			return 12;
		}
		
		/**
		 * Maximum number of values for each id
		 */
		private int getValuesCount()
		{
			return ((PagedFile.PAGE_SIZE-getValuesOffset())/getMaxCount())/getValueSize();
		}
		
		private int getValuesOffset(int aIndex)
		{
			return getValuesOffset()+aIndex*getValueSize()*getValuesCount();
		}
		
		private long getId(int aIndex)
		{
			return itsPage.readLong(getIdsOffset()+8*aIndex);
		}
		
		private void setId(int aIndex, long aId)
		{
			if (aId == 0 && aIndex == itsLastIndex)
			{
				assert getId(aIndex) == itsLastObjectFieldId;
				itsLastObjectFieldId = 0;
				itsLastIndex = -1;
			}
			itsPage.writeLong(getIdsOffset()+8*aIndex, aId);
			check();
			checkCount();
		}
		
		/**
		 * Number of values corresponding to the id at the given index.
		 */
		private int getValuesCount(int aIndex)
		{
			return itsPage.readByte(getCountsOffset()+aIndex) & 0xff;
		}
		
		private void setValuesCount(int aIndex, int aCount)
		{
			itsPage.writeByte(getCountsOffset()+aIndex, aCount);
			check();
		}
		
		/**
		 * Returns the index of the given id.
		 * If it is not found and there are still empty indexes,
		 * an empty index is used.
		 */
		public int indexOf(long aObjectFieldId)
		{
			assert aObjectFieldId != 0;
			if (aObjectFieldId == itsLastObjectFieldId) return itsLastIndex;
			
			itsLastObjectFieldId = aObjectFieldId;
			int theFreeIndex = -1;
			for(int i=0;i<getMaxCount();i++)
			{
				long theId = getId(i);
				if (theId == 0 && theFreeIndex == -1) theFreeIndex = i;
				else if (theId == aObjectFieldId) 
				{
					itsLastIndex = i;
					return i;
				}
			}
			if (theFreeIndex >= 0)
			{
				setCurrentCount(getCurrentCount()+1);
				setId(theFreeIndex, aObjectFieldId);
				itsLastIndex = theFreeIndex;
				return theFreeIndex;
			}
			assert ! hasFreeIds();
			itsLastIndex = -1;
			return -1;
		}
		
		public Object addTuple(ObjectPageSlot aSlot, long aObjectFieldId, long aBlockId, int aThreadId)
		{
			int theIndex = indexOf(aObjectFieldId);
			if (theIndex == -1) throw new RuntimeException("Shared page has no free indexes");
			assert getCurrentCount() > 0;
		
			int theCount = getValuesCount(theIndex);
			if (theCount == getValuesCount()) return dumpToLargerStore(aSlot, theIndex, aObjectFieldId, aBlockId, aThreadId);
			else
			{
				int theOffset = getValuesOffset(theIndex) + theCount*getValueSize();
				itsPage.writeLI(theOffset, aBlockId, aThreadId);
				setValuesCount(theIndex, theCount+1);
				check();
				return null;
			}
		}
		
		private Object dumpToLargerStore(ObjectPageSlot aSlot, int aIndex, long aObjectFieldId, long aBlockId, int aThreadId)
		{
			if (itsLogMaxCount < OBJECTS_PER_SHAREDPAGE.length-1)
			{
				// Dump to a larger shared page
				SharedPage theSharedPage = getSharedPage(itsLogMaxCount+1);
				int theValuesCount = getValuesCount(aIndex);
				int theValuesOffset = getValuesOffset(aIndex);
				for(int i=0;i<theValuesCount;i++)
				{
					long theBlockId = itsPage.readLong(theValuesOffset);
					theValuesOffset += 8;
					int theThreadId = itsPage.readInt(theValuesOffset);
					theValuesOffset += 4;
					Object theDump = theSharedPage.addTuple(null, aObjectFieldId, theBlockId, theThreadId);
					assert theDump == null;
				}
				Object theDump = theSharedPage.addTuple(null, aObjectFieldId, aBlockId, aThreadId);
				assert theDump == null;
				setValuesCount(aIndex, 0);
				setCurrentCount(getCurrentCount()-1);
				setId(aIndex, 0);
				aSlot.setSharedPage(theSharedPage);
				check();

				if (! theSharedPage.hasFreeIds()) unfreeSharedPage(theSharedPage);
				freeSharedPage(this);
				
				if (LOG) Utils.println("Moved object %d from p.%d [%d/%d] to p.%d [%d/%d]", 
						aObjectFieldId, 
						getPageId(), 
						getCurrentCount(), getMaxCount(),
						theSharedPage.getPageId(),
						theSharedPage.getCurrentCount(), theSharedPage.getMaxCount());
				
				return theSharedPage;
			}
			else
			{
				assert getCurrentCount() > 0;
				DeltaBTree theBTree = aSlot.toBTree();
				int theValuesCount = getValuesCount(aIndex);
				int theValuesOffset = getValuesOffset(aIndex);
				for(int i=0;i<theValuesCount;i++)
				{
					long theBlockId = itsPage.readLong(theValuesOffset);
					theValuesOffset += 8;
					int theThreadId = itsPage.readInt(theValuesOffset);
					theValuesOffset += 4;
					theBTree.insertLeafTuple(theBlockId, theThreadId);
				}
				theBTree.insertLeafTuple(aBlockId, aThreadId);

				setValuesCount(aIndex, 0);
				setCurrentCount(getCurrentCount()-1);
				setId(aIndex, 0);
				check();

				freeSharedPage(this);

				if (LOG) Utils.println("Moved object %d from page %d to btree", aObjectFieldId, getPageId());

				return theBTree;
			}
		}
		
		@Override
		public String toString()
		{
			return String.format(
					"ids off.: %d, counts off.: %d, values off.: %d, values count: %d, max count: %d", 
					getIdsOffset(),
					getCountsOffset(),
					getValuesOffset(),
					getValuesCount(),
					getMaxCount());
		}
	}
	
	public class ObjectAccessStore 
	{
		/**
		 * Id of the object/field whose accesses are stored here.
		 * Negative value means read-only.
		 */
		private long itsObjectFieldId;
		
		private ObjectPageSlot itsSlot;
		
		private SharedPage itsSharedPage;
		private DeltaBTree itsDeltaBTree;

		public ObjectAccessStore(long aObjectFieldId, ObjectPageSlot aSlot)
		{
			itsObjectFieldId = aObjectFieldId;
			itsSlot = aSlot;
			
			if (itsSlot.isNull())
			{
				assert itsObjectFieldId > 0;
				itsSharedPage = getSharedPage(0);
				itsSlot.setSharedPage(itsSharedPage);
				assert itsSharedPage.hasFreeIds();
				itsDeltaBTree = null;
				if (LOG) Utils.println("Object %d on p.%d [%d/%d] (initial)", aObjectFieldId, itsSharedPage.getPageId(), itsSharedPage.getCurrentCount(), itsSharedPage.getMaxCount());
			}
			else if (itsSlot.isSharedPage())
			{
				// Shared page
				itsSharedPage = itsSlot.getSharedPage(OnDiskIndex.this);
				if (LOG) Utils.println("Object %d on p.%d [%d/%d]", aObjectFieldId, itsSharedPage.getPageId(), itsSharedPage.getCurrentCount(), itsSharedPage.getMaxCount());
				assert itsSharedPage.indexOf(aObjectFieldId) != -1;
				itsDeltaBTree = null;
			}
			else
			{
				// DeltaBTree
				itsDeltaBTree = itsSlot.getBTree();
				if (LOG) Utils.println("Object %d on btree", aObjectFieldId);
				itsSharedPage = null;
			}
		}

		public long getObjectFieldId()
		{
			return itsObjectFieldId;
		}
		
		public void append(long[] aBlockIds, int[] aThreadIds, int aOffset, int aCount)
		{
			assert itsObjectFieldId > 0;
			while(aCount > 0)
			{
				if (itsSharedPage != null)
				{
					Object theDump = itsSharedPage.addTuple(itsSlot, itsObjectFieldId, aBlockIds[aOffset], aThreadIds[aOffset]);
					if (! itsSharedPage.hasFreeIds()) unfreeSharedPage(itsSharedPage);
	
					if (theDump instanceof DeltaBTree)
					{
						itsDeltaBTree = (DeltaBTree) theDump;
						itsSharedPage = null;
					}
					else if (theDump instanceof SharedPage) 
					{
						itsSharedPage = (SharedPage) theDump;
						itsDeltaBTree = null;
					}
					else assert theDump == null;
					
					aOffset++;
					aCount--;
				}
				else
				{
					itsDeltaBTree.insertLeafTuples(aBlockIds, aThreadIds, aOffset, aCount);
					break;
				}
			}				
		}
		
		public int[] getThreadIds(long aBlockId)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * Returns the ids of threads that accessed the object in the block that
		 * preceedes the given block.
		 */
		public int[] getPrevThreadIds(long aBlockId)
		{
			throw new UnsupportedOperationException();
		}
		
		private void flush()
		{
			if (itsDeltaBTree != null) itsDeltaBTree.flush();
		}
	}
	
	private class ObjectAccessStoreCache extends MRUBuffer<Long, ObjectAccessStore>
	{
		public ObjectAccessStoreCache()
		{
			super(64);
		}
		
		@Override
		protected Long getKey(ObjectAccessStore aValue)
		{
			return aValue.getObjectFieldId();
		}

		@Override
		protected ObjectAccessStore fetch(Long aId)
		{
			long theId = aId;
			return createStore(theId);
		}
		
		@Override
		protected void dropped(ObjectAccessStore aValue)
		{
			if (aValue.getObjectFieldId() > 0) aValue.flush();
		}
	}

}
