/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.database;

import java.util.NoSuchElementException;


public abstract class AbstractBidiIterator<T> implements IBidiIterator<T>
{
	private boolean itsNextReady;
	private T itsNext;
	
	private boolean itsPreviousReady;
	private T itsPrevious;
	
	private int itsOffset;
	
	public AbstractBidiIterator()
	{
		this(false);
	}
	
	
	/**
	 * Creates an new iterator.
	 * @param aExhausted if true, the iterator is created exhausted and cannot 
	 * return any element
	 */
	public AbstractBidiIterator(boolean aExhausted)
	{
		itsNext = itsPrevious = null;
		itsNextReady = itsPreviousReady = aExhausted;
		itsOffset = 0;
	}
	
	protected void reset()
	{
		itsNext = itsPrevious = null;
		itsNextReady = itsPreviousReady = false;
		itsOffset = 0;		
	}
	
	/**
	 * Fetches the next element and moves the low-level internal pointer
	 * of the iterator. The offset between the low-level internal pointer
	 * and the logical pointer of the iterator is stored in {@link #itsOffset}.
	 * @return Null if no next element is available
	 */
	protected abstract T fetchNext();
	
	
	/**
	 * Fetches the previous element. Symmetric of {@link #fetchNext()}.
	 */
	protected abstract T fetchPrevious();
	
	/**
	 * Reads the next tuple
	 */
	private void readNext()
	{
		if (itsNextReady)
		{
			itsPrevious = itsNext;
			itsPreviousReady = true;
		}
		else itsPreviousReady = false;
		
		itsNext = fetchNext();
		
		itsNextReady = true;
	}

	/**
	 * Reads the previous tuple
	 */
	private void readPrevious()
	{
		if (itsPreviousReady)
		{
			itsNext = itsPrevious;
			itsNextReady = true;
		}
		else itsNextReady = false;
		
		itsPrevious = fetchPrevious();
		
		itsPreviousReady = true;
	}
	
	public boolean hasNext()
	{
		return peekNext() != null;
	}

	public int getOffset()
	{
		return itsOffset;
	}
	
	public T peekNext()
	{
		if (itsOffset == -1) 
		{
			readNext();
			itsNextReady = false;
			itsOffset = 0;
		}
		
		if (! itsNextReady) 
		{
			readNext();
			if (itsNext != null) itsOffset = 1;
		}
		
		return itsNext;
	}

	public T next()
	{
		if (itsOffset == -1) 
		{
			readNext();
			itsNextReady = false;
		}
		itsOffset = 0;
		
		if (! itsNextReady) readNext();
		if (itsNext == null) 
		{
			throw new NoSuchElementException();
		}
		
		T theResult = itsNext;
		itsNextReady = false;
		
		return theResult;
	}

	public boolean hasPrevious()
	{
		return peekPrevious() != null;
	}

	public T peekPrevious()
	{
		if (itsOffset == 1) 
		{
			readPrevious();
			itsPreviousReady = false;
			itsOffset = 0;
		}
		
		if (! itsPreviousReady) 
		{
			readPrevious();
			if (itsPrevious != null) itsOffset = -1;
		}
		
		return itsPrevious;
	}
	
	public T previous()
	{
		if (itsOffset == 1) 
		{
			readPrevious();
			itsPreviousReady = false;
		}
		itsOffset = 0;
		
		if (! itsPreviousReady) readPrevious();
		if (itsPrevious == null) throw new NoSuchElementException();
		
		T theResult = itsPrevious;
		itsPreviousReady = false;
		
		return theResult;
	}
}
