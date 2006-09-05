package btree;
/*
 * Created on May 26, 2006
 */

/**
 * Interface of a simple dictionary
 */
public interface Dict
{
	/**
	 * Retrieve the value associated with a given key.
	 * @return The value associated with the key, or null if not found.
	 */
	public Long get(long aKey);

	/**
	 * Sets the value associated with a given key.
	 * @return True if the key is added, false if it was updated
	 */
	public boolean put(long aKey, long aValue);

	/**
	 * Removes any association with the given key.
	 * @return True if the key was removed, false if it was not present
	 */
	public boolean remove(long aKey);
}