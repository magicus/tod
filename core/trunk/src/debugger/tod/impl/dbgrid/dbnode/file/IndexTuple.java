package tod.impl.dbgrid.dbnode.file;

import static tod.impl.dbgrid.DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
import zz.utils.bit.BitStruct;

/**
 * Base class for all index tuples. Only contains the timestamp.
 */
public class IndexTuple extends Tuple
{
	private long itsTimestamp;

	public IndexTuple(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	public IndexTuple(BitStruct aBitStruct)
	{
		itsTimestamp = aBitStruct.readLong(EVENT_TIMESTAMP_BITS);
	}

	protected void set(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	/**
	 * Writes a serialized representation of this tuple to
	 * the specified struct.
	 * Subclasses should override to serialize additional attributes,
	 * and call super first.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
		aBitStruct.writeLong(getTimestamp(), EVENT_TIMESTAMP_BITS);
	}
	
	/**
	 * Returns the timestamp of this tuple.
	 */
	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	@Override
	public String toString()
	{
		return String.format("%s: t=%d",
				getClass().getSimpleName(),
				getTimestamp());
	}
}