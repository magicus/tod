package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
import zz.utils.bit.BitStruct;
import zz.utils.bit.ByteBitStruct;

/**
 * A tuple codec is able to serialized and deserialize tuples in a {@link BitStruct}
 * @author gpothier
 */
public abstract class TupleCodec<T extends Tuple>
{
	/**
	 * Returns the size (in bits) of each tuple.
	 * Subclasses must override this method to add the size of their tuple's
	 * own attributes to the total
	 */
	public int getTupleSize()
	{
		return 0;
	}
	
	/**
	 * Reads a tuple from the given struct.
	 */
	public abstract T read(BitStruct aBitStruct);
	
	public final void write(BitStruct aBitStruct, Tuple aTuple)
	{
		aTuple.writeTo(aBitStruct);
	}
}