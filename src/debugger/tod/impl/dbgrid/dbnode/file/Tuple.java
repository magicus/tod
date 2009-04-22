package tod.impl.dbgrid.dbnode.file;

import tod.impl.dbgrid.dbnode.file.HardPagedFile.Page;
import zz.utils.bit.BitStruct;

/**
 * A tuple is a kind of record of a fixed length (determined by
 * {@link TupleCodec#getTupleSize()}) that can be stored in a {@link Page}.
 * @author gpothier
 */
public abstract class Tuple
{
	/**
	 * Writes a serialized representation of this tuple to
	 * the specified struct.
	 * Subclasses should override to serialize additional attributes,
	 * and call super first.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
	}
	
	/**
	 * Indicates if this tuple is null.
	 * A null tuple typically indicates the end of a page.
	 */
	public final boolean isNull() {return false;}


}