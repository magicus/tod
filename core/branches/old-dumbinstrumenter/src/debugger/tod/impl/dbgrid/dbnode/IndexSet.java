/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.dbnode;

import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.dbnode.file.IndexTuple;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;

/**
 * A set of indexes for a given attribute. Within a set,
 * there is one index per possible attribute value.
 * @author gpothier
 */
public abstract class IndexSet<T extends IndexTuple>
{
	private HierarchicalIndex<T>[] itsIndexes;
	
	/**
	 * Name of this index set (for monitoring)
	 */
	private final String itsName;
	
	private HardPagedFile itsFile;
	
	public IndexSet(String aName, HardPagedFile aFile, int aIndexCount)
	{
		itsName = aName;
		itsFile = aFile;
		itsIndexes = new HierarchicalIndex[aIndexCount];
		Monitor.getInstance().register(this);
	}

	/**
	 * Creates a new index for this set.
	 */
	protected abstract HierarchicalIndex<T> createIndex(String aName, HardPagedFile aFile);
	
	/**
	 * Retrieved the index corresponding to the specified... index.
	 */
	public HierarchicalIndex<T> getIndex(int aIndex)
	{
		HierarchicalIndex<T> theIndex = itsIndexes[aIndex];
		if (theIndex == null)
		{
			theIndex = createIndex(itsName+"-"+aIndex, itsFile);
			itsIndexes[aIndex] = theIndex;
		}
		
		return theIndex;
	}

	public void addTuple(int aIndex, T aTuple)
	{
		getIndex(aIndex).add(aTuple);
	}
	
	@Probe(key = "index count", aggr = AggregationType.SUM)
	public long getIndexCount()
	{
		return itsIndexes.length;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName()+": "+itsName;
	}
}
