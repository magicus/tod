package tod.impl.dbgrid.dbnode;

import zz.utils.bit.BitStruct;

/**
 * A tuple codec is able to serialized and deserialize tuples in a {@link BitStruct}
 * @author gpothier
 */
public abstract class TupleCodec<T>
{
	/**
	 * Returns the size (in bits) of each tuple.
	 */
	public abstract int getTupleSize();
	
	/**
	 * Reads a tuple from the given struct.
	 */
	public abstract T read(BitStruct aBitStruct);
	
	/**
	 * Writes the tuple to the given struct.
	 */
	public abstract void write(BitStruct aBitStruct, T aTuple);
	
	/**
	 * Indicates if a tuple is null.
	 * A null tuple typically indicates the end of a page.
	 */
	public abstract boolean isNull(T aTuple);
}