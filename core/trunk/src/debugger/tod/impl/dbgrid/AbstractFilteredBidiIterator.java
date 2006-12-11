/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".

Contact: gpothier -at- dcc . uchile . cl
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
