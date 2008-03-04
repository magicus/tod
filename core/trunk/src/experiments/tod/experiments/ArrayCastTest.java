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
package tod.experiments;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import tod.BenchBase;
import tod.BenchBase.BenchResults;
import tod.utils.ArrayCast;

public class ArrayCastTest
{
	private static final int n = 1000000;
	
	public static void main(String[] args)
	{
		BenchResults theResults;
		theResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				testNIO();
			}
		});
		
		System.out.println(theResults);

		theResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				testB2I();
			}
		});

		System.out.println(theResults);
	}
	
	private static void testNIO()
	{
		ByteBuffer theByteBuffer = ByteBuffer.allocateDirect(4096);
		theByteBuffer.order(ByteOrder.nativeOrder());
		IntBuffer theIntBuffer = theByteBuffer.asIntBuffer();
		
		int[] theBuffer = new int[1024];
		
		for(int i=0;i<n;i++)
		{
			theIntBuffer.position(0);
			theIntBuffer.get(theBuffer);
		}
	}
	
	private static void testNIO2()
	{
		byte[] theByteBuffer = new byte[4096];
		int[] theIntBuffer = new int[1024];
		
		for(int i=0;i<n;i++)
		{
			ByteBuffer theByteBufferW = ByteBuffer.wrap(theByteBuffer);
			theByteBufferW.order(ByteOrder.nativeOrder());
			IntBuffer theIntBufferW = theByteBufferW.asIntBuffer();

			theIntBufferW.position(0);
			theIntBufferW.get(theIntBuffer);
		}
	}
	
	private static void testB2I()
	{
		byte[] theByteBuffer = new byte[4096];
		int[] theIntBuffer = new int[1024];

		for(int i=0;i<n;i++)
		{
			ArrayCast.b2i(theByteBuffer, theIntBuffer);
		}
	}
}
