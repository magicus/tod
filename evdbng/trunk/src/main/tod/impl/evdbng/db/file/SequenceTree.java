/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db.file;

import tod.impl.evdbng.db.file.TupleFinder.NoMatch;

/**
 * A BTree that conceptually stores tuples of the form <key, s> where
 * the s in consecutive tuples are consecutive. Thanks to this constraint,
 * the s doesn't actually need to be stored, as it is equal to the position
 * of the tuple in the BTree.
 * We assume that the sequence starts at 0.
 * @author gpothier
 */
public class SequenceTree extends BTree<SimpleTuple>
{
	public SequenceTree(String aName, PagedFile aFile)
	{
		super(aName, aFile);
	}

	@Override
	protected TupleBufferFactory<SimpleTuple> getTupleBufferFactory()
	{
		return TupleBufferFactory.SIMPLE;
	}

	/**
	 * Adds a tuple to this btree.
	 */
	public void add(long aKey)
	{
		addLeafKey(aKey);
	}
	
	/**
	 * Returns the position of the tuple associated with the given key.
	 * @param aNoMatch Indicates the behavior when no exact match is found:
	 * <li> If aNoMatch is null, the method returns -1
	 * <li> Otherwise, the position of the previous or next tuple is returned 
	 */
	public long getTuplePosition(long aKey, NoMatch aNoMatch)
	{
		TupleIterator<SimpleTuple> theIterator = getTupleIterator(
				aKey, 
				aNoMatch != null ? aNoMatch : NoMatch.AFTER);
		
		SimpleTuple theTuple = theIterator.peekNext();
		if (aNoMatch == null && theTuple.getKey() != aKey) return -1;
		else return theIterator.getNextTupleIndex();
	}
}
