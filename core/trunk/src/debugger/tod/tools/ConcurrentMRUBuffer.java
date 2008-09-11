package tod.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import zz.utils.ArrayStack;
import zz.utils.cache.MRUBuffer;
import zz.utils.cache.SyncMRUBuffer;
import zz.utils.list.NakedLinkedList.Entry;

/**
 * A concurrent variation of {@link MRUBuffer} that provides a high throughput
 * in presence of concurrent accesses.
 * This is unlike {@link SyncMRUBuffer}, that preserves the exact semantics of {@link MRUBuffer}
 * but can be a bottleneck if several threads do access the buffer at the same time.
 * 
 * TODO: This is a work in progress, only a few methods are supported.
 * @author gpothier
 *
 * @param <K>
 * @param <V>
 */
public abstract class ConcurrentMRUBuffer<K, V> extends MRUBuffer<K, V>
{
	private final int itsQueueSize;
	
	private final ReentrantReadWriteLock itsLock = new ReentrantReadWriteLock();
	private final List<Stacks<V>> itsStacks = new ArrayList<Stacks<V>>();
	private final ThreadLocal<Stacks<V>> itsLocalStacks = new ThreadLocal<Stacks<V>>()
	{
		@Override
		protected Stacks<V> initialValue()
		{
			Stacks<V> theStacks = new Stacks<V>(itsQueueSize);
			synchronized(itsStacks)
			{
				itsStacks.add(theStacks);
			}
			
			return theStacks;
		}
	};
	
	public ConcurrentMRUBuffer(int aCacheSize, boolean aUseMap, int aQueueSize)
	{
		super(aCacheSize, aUseMap);
		assert aCacheSize > aQueueSize*8 : String.format("cache: %d, queue: %d", aCacheSize, aQueueSize);
		itsQueueSize = aQueueSize;
	}

	public ConcurrentMRUBuffer(int aCacheSize, int aQueueSize)
	{
		super(aCacheSize);
		assert aCacheSize > aQueueSize*8;
		itsQueueSize = aQueueSize;
	}
	
	@Override
	public Entry<V> add(V aValue)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void drop(K aKey)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void dropAll()
	{
		try
		{
			itsLock.writeLock().lock();
			commit();
			
			super.dropAll();
		}
		finally
		{
			itsLock.writeLock().unlock();
		}
	}

	@Override
	public Entry<V> getEntry(K aKey, boolean aFetch)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void use(Entry<V> aEntry)
	{
		Stacks<V> theStacks = itsLocalStacks.get();
		
		try
		{
			itsLock.readLock().lock();
			if (theStacks.useStack.isEmpty() || theStacks.useStack.peek() != aEntry)
				theStacks.useStack.push(aEntry);
		}
		finally
		{
			itsLock.readLock().unlock();
		}
		
		if (theStacks.useStack.isFull()) commit(theStacks);
	}

	private void commit(Stacks<V> aStacks)
	{
		try
		{
			itsLock.writeLock().lock();
			while(! aStacks.useStack.isEmpty()) super.use(aStacks.useStack.pop());
		}
		finally
		{
			itsLock.writeLock().unlock();
		}
	}
	
	private void commit()
	{
		for (Stacks<V> theStacks : itsStacks) commit(theStacks);
	}
	
	private static class Stacks<V>
	{
		public final ArrayStack<Entry<V>> useStack;
		
		private Stacks(int aQueueSize)
		{
			useStack = new ArrayStack<Entry<V>>(aQueueSize);
		}
	}

}
