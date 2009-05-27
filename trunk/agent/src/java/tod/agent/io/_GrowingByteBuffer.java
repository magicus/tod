/*
 * Created on Jan 13, 2009
 */
package tod.agent.io;

/**
 * A byte buffer that grows as needed
 * @author gpothier
 */
public class _GrowingByteBuffer extends _ByteBuffer
{
	protected _GrowingByteBuffer(byte[] aBytes)
	{
		super(aBytes);
	}

	public static _GrowingByteBuffer allocate(int aSize)
	{
		return new _GrowingByteBuffer(new byte[aSize]);
	}

	public static _GrowingByteBuffer wrap(byte[] aBytes)
	{
		return new _GrowingByteBuffer(aBytes);
	}
	
	@Override
	protected void checkRemaining(int aRequested)
	{
		if (aRequested > remaining())
		{
			if (limit() != capacity()) throw new _BufferOverflowException();
			byte[] theNewBuffer = new byte[capacity()*2];
			System.arraycopy(array(), 0, theNewBuffer, 0, capacity());
			_array(theNewBuffer);
			limit(theNewBuffer.length);
		}
	}
	
	
}
