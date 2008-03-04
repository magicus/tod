/*
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


import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import tod.impl.database.BufferedBidiIterator;
import tod.impl.database.IBidiIterator;

public class TestBufferedIterator
{
	@Test public void testEndToEnd()
	{
		BaseIterator theBaseIterator = new BaseIterator(10, 20, 13);
		BufferedIterator theIterator = new BufferedIterator(theBaseIterator, 5);
		
		int theCurrent = 13;
		while(theIterator.hasNext())
		{
			Integer theNext = theIterator.next();
			Assert.assertEquals(theNext.intValue(), theCurrent);
			theCurrent++;
		}
		
		while(theIterator.hasPrevious())
		{
			Integer thePrevious = theIterator.previous();
			theCurrent--;
			Assert.assertEquals(thePrevious.intValue(), theCurrent);
		}
		
		while(theIterator.hasNext())
		{
			Integer theNext = theIterator.next();
			Assert.assertEquals(theNext.intValue(), theCurrent);
			theCurrent++;
		}
	}
	
	@Test public void testEarlyBack()
	{
		BaseIterator theBaseIterator = new BaseIterator(5, 20, 13);
		BufferedIterator theIterator = new BufferedIterator(theBaseIterator, 5);
		
		int theCurrent = 13;
		while(theCurrent < 19)
		{
			Integer theNext = theIterator.next();
			Assert.assertEquals(theNext.intValue(), theCurrent);
			theCurrent++;
		}
		
		while(theCurrent > 5)
		{
			Integer thePrevious = theIterator.previous();
			theCurrent--;
			Assert.assertEquals(thePrevious.intValue(), theCurrent);
		}
		
		while(theIterator.hasNext())
		{
			Integer theNext = theIterator.next();
			Assert.assertEquals(theNext.intValue(), theCurrent);
			theCurrent++;
		}
	}
	
	private static class BaseIterator implements IBidiIterator<Integer>
	{
		private int itsMin;
		private int itsMax;
		private int itsCurrent;
		
		public BaseIterator(int aMin, int aMax, int aCurrent)
		{
			itsMin = aMin;
			itsMax = aMax;
			itsCurrent = aCurrent;
		}

		public boolean hasNext()
		{
			return itsCurrent <= itsMax;
		}

		public boolean hasPrevious()
		{
			return itsCurrent > itsMin;
		}

		public Integer next()
		{
			assert hasNext();
			return itsCurrent++;
		}

		public Integer peekNext()
		{
			return hasNext() ? itsCurrent : null;
		}

		public Integer peekPrevious()
		{
			return hasPrevious() ? itsCurrent-1 : null;
		}

		public Integer previous()
		{
			assert hasPrevious();
			return --itsCurrent;
		}
	}
	
	private static class BufferedIterator extends BufferedBidiIterator<Integer[], Integer>
	{
		private IBidiIterator<Integer> itsBaseIterator;
		
		private int itsBufferSize;
		
		public BufferedIterator(IBidiIterator<Integer> aBaseIterator, int aBufferSize)
		{
			itsBaseIterator = aBaseIterator;
			itsBufferSize = aBufferSize;
		}

		@Override
		protected Integer[] fetchNextBuffer()
		{
			List<Integer> theList = new ArrayList<Integer>(itsBufferSize);
			for (int i=0;i<itsBufferSize;i++)
			{
				if (itsBaseIterator.hasNext()) theList.add(itsBaseIterator.next());
				else break;
			}

			return theList.size() > 0 ?
					theList.toArray(new Integer[theList.size()])
					: null;
		}

		@Override
		protected Integer[] fetchPreviousBuffer()
		{
			List<Integer> theList = new ArrayList<Integer>(itsBufferSize);
			for (int i=0;i<itsBufferSize;i++)
			{
				if (itsBaseIterator.hasPrevious()) theList.add(itsBaseIterator.previous());
				else break;
			}

			int theSize = theList.size();
			if (theSize == 0) return null;
			
			Integer[] theResult = new Integer[theSize];
			for (int i=0;i<theSize;i++) theResult[i] = theList.get(theSize-i-1);
			
			return theResult;
		}

		@Override
		protected Integer get(Integer[] aBuffer, int aIndex)
		{
			return aBuffer[aIndex];
		}

		@Override
		protected int getSize(Integer[] aBuffer)
		{
			return aBuffer.length;
		}
	}
	
}
