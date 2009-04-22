/*
 * Created on Aug 19, 2006
 */
package tod.impl.dbgrid.dbnode.file;

import static tod.impl.dbgrid.DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
import zz.utils.bit.BitStruct;

public abstract class IndexTupleCodec<T extends IndexTuple> extends TupleCodec<T>
{
	@Override
	public int getTupleSize()
	{
		return EVENT_TIMESTAMP_BITS;
	}
	
	@Override
	public final void write(BitStruct aBitStruct, T aTuple)
	{
		aTuple.writeTo(aBitStruct);
	}
	
	@Override
	public final boolean isNull(T aTuple)
	{
		return aTuple.getTimestamp() == 0;
	}
}