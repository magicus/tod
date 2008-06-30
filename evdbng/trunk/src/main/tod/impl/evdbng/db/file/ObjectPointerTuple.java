package tod.impl.evdbng.db.file;

import tod.impl.evdbng.db.ObjectsDatabase;

/**
 * A tuple that stores information to retrieve an object in
 * {@link ObjectsDatabase}.
 * @author gpothier
 */
public class ObjectPointerTuple extends Tuple
{
	private int itsPageId;
	private int itsOffset;
	
	public ObjectPointerTuple(long aKey, int aPageId, int aOffset)
	{
		super(aKey);
		itsPageId = aPageId;
		itsOffset = aOffset;
	}
	
	public int getOffset()
	{
		return itsOffset;
	}

	public int getPageId()
	{
		return itsPageId;
	}
}