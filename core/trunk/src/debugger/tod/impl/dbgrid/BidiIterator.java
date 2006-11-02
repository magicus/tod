/*
 * Created on Nov 2, 2006
 */
package tod.impl.dbgrid;

/**
 * Interface for bidirectional iterators.
 * @author gpothier
 */
public interface BidiIterator<T>
{
	/**
	 * Retrieves the next elements and moves the pointer.
	 */
	public T next();
	
	/**
	 * Returns true if a next element is available.
	 */
	public boolean hasNext();
	
	/**
	 * Returns the element that would be returned by {@link #next()}, without
	 * moving the pointer, or null if no element is available.
	 */
	public T peekNext();
	
	
	public T previous();
	public boolean hasPrevious();
	public T peekPrevious();
}
