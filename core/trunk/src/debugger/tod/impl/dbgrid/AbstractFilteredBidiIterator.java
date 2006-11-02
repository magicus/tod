/*
 * Created on Nov 2, 2006
 */
package tod.impl.dbgrid;

import zz.utils.AbstractFilteredIterator;

/**
 * Reimplementation of {@link AbstractFilteredIterator} for {@link BidiIterator}s
 * @author gpothier
 */
public abstract class AbstractFilteredBidiIterator<I, O> extends AbstractBidiIterator<O>
{
	protected static final Object REJECT = new Object();
	
	private BidiIterator<I> itsIterator;
	
	public AbstractFilteredBidiIterator(BidiIterator<I> aIterator)
	{
		itsIterator = aIterator;
	}
	
	protected abstract Object transform(I aInput);

	@Override
	protected O fetchNext()
	{
		while (itsIterator.hasNext())
		{
			I theInput = itsIterator.next();
			Object theOutput = transform(theInput);
			if (theOutput != REJECT) return (O) theOutput;
		}
		
		return null;
	}

	@Override
	protected O fetchPrevious()
	{
		while (itsIterator.hasPrevious())
		{
			I theInput = itsIterator.previous();
			Object theOutput = transform(theInput);
			if (theOutput != REJECT) return (O) theOutput;
		}
		
		return null;
	}
	
	
}
