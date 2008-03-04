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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import tod.BenchBase;
import tod.BenchBase.BenchResults;

public class NativeLog
{
	static
	{
		System.loadLibrary("native-stream");
	}
	

	public static native void log(int i, byte b, short c, long d);
	
	public static void main(String[] args)
	{
		final int n = 100000000;
		final ByteArrayOutputStream theStream = new ByteArrayOutputStream(50);
		final DataOutputStream theDataStream = new DataOutputStream(theStream);
		
//		BenchResults r1 = BenchBase.benchmark(new Runnable()
//		{
//			public void run()
//			{
//				try
//				{
//					for(int i=0;i<n;i++)
//					{
//						theDataStream.writeInt(0x12345678);
//						theDataStream.writeByte(0x12);
//						theDataStream.writeShort(0x2468);
//						theDataStream.writeLong(0x1234567890abcdefL);
//						theStream.reset();
//					}
//				}
//				catch (IOException e)
//				{
//					throw new RuntimeException(e);
//				}
//			}
//		});
//		
//		System.out.println("DataOutputStream: "+r1);
		
		final B a = new B();
		
		BenchResults r2 = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				for(int i=0;i<n;i++)
				{
					a.log(0x12345678, (byte) 0x12, (short) 0x2468, 0x1234567890abcdefL);
				}
			}
		});
		
		System.out.println("Native: "+r2);
	}
	
	private static abstract class A
	{
		public abstract void log(int i, byte b, short c, long d);
	}
	
	private static class B extends A
	{
		@Override
		public void log(int a, byte b, short c, long d)
		{
			NativeLog.log(a, b, c, d);
		}
	}
	
}
