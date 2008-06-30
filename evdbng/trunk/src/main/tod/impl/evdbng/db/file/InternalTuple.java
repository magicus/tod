/*
 * Created on Jan 22, 2008
 */
package tod.impl.evdbng.db.file;

/**
 * Tuple for internal nodes of the {@link BTree}
 * @author gpothier
 */
public class InternalTuple extends Tuple
{
	private int itsPageId;
	private long itsTupleCount;
	
	public InternalTuple(long aKey, int aPageId, long aTupleCount)
	{
		super(aKey);
		itsPageId = aPageId;
		itsTupleCount = aTupleCount;
	}

	public int getPageId()
	{
		return itsPageId;
	}

	public long getTupleCount()
	{
		return itsTupleCount;
	}
	
	
}
