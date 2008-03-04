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
package tod.test;

import java.util.ArrayList;
import java.util.List;

/**
 * Benchmarks for measuring instrumentation and communication overhead.
 * @author gpothier
 */
public class Benchmark
{
	public static void main(String[] args)
	{
		new BenchComputations();
		new OwnMethods();
		new JavaMethods();
	}
	
	private static abstract class Bench
	{
		private String itsName;
		private int itsCount;

		public Bench(String aName, int aCount)
		{
			itsName = aName;
			itsCount = aCount;
			
			start();
		}
		
		public void start()
		{
			measure();
			
			int n=10;
			long total = 0;
			for (int i=0;i<n;i++)
			{
				long t = measure();
				System.out.println(String.format("%s[%d]: %.3fs", itsName, i+1, 0.001f*t));
				total += t;
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			System.out.println(String.format("%s: %.3fs", itsName, 0.001f*total/n));
		}
		
		private long measure()
		{
			long t0 = System.currentTimeMillis();
			for (int i=0;i<itsCount;i++) run();
			long t1 = System.currentTimeMillis();

			return t1-t0;
		}
		
		protected abstract void run();
		
	}
	
	private static class BenchComputations extends Bench
	{

		public BenchComputations()
		{
			super("Computations", 1);
		}
		
		
		@Override
		protected void run()
		{
			int a=1, b=2, c=3;
			double d=1, e=2, f=3;
			
			for (int i=0;i<10000000;i++)
			{
				a *= i;
				b += i*5;
				c = (int)(a-b+e);
				
				d = c*i;
				e = d*f*f*f*f*f*f*f*f;
				f = i+b;
			}
		}
	}
	
	private static class OwnMethods extends Bench
	{
		public OwnMethods()
		{
			super("Own methods", 100000000);
		}
		
		@Override
		protected void run()
		{
			int a = m1(10, 20);
			int b = m2(1, 2);
			int c = a+b;
		}
		
		private int m1(int a, int b)
		{
			return a+b;
		}
		
		public int m2(int a, int b)
		{
			int c = 0;
			for (int i=0;i<b;i++)
			{
				c += a+i;
			}
			return c;
		}
	}
	

	private static class JavaMethods extends Bench
	{
		public JavaMethods()
		{
			super("Java methods", 1000000);
		}
		
		@Override
		protected void run()
		{
			List l = new ArrayList();
			l.add("toto"+Math.sin(0.5));
		}
	}
}
