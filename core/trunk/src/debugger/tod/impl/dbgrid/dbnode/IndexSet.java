/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of indexes for a given attribute. Within a set,
 * there is one index per possible attribute value.
 * @author gpothier
 */
public abstract class IndexSet<K, T extends HierarchicalIndex.Tuple>
{
	private Map<K, HierarchicalIndex<T>> itsMap = new HashMap<K, HierarchicalIndex<T>>();
	
	private PagedFile itsFile;
	
	public IndexSet(PagedFile aFile)
	{
		itsFile = aFile;
	}

	/**
	 * Creates a new index for this set.
	 */
	protected abstract HierarchicalIndex<T> createIndex(PagedFile aFile);

	public void addTuple(K aKey, T aTuple)
	{
		HierarchicalIndex<T> theIndex = itsMap.get(aKey);
		if (theIndex == null)
		{
			theIndex = createIndex(itsFile);
			itsMap.put(aKey, theIndex);
		}
		
		theIndex.add(aTuple);
	}
}
