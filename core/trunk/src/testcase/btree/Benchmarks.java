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
package btree;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Random;

import javax.swing.plaf.basic.BasicTreeUI;


public class Benchmarks
{
	
	
	private static ThreadMXBean threadMXBean;
	
	private static Times run(Runnable aRunnable)
	{
		long theTotal0 = System.nanoTime();
		long theCpu0 = threadMXBean.getCurrentThreadCpuTime();
		long theUser0 = threadMXBean.getCurrentThreadUserTime();

		aRunnable.run();
		
		long theTotal1 = System.nanoTime();
		long theCpu1 = threadMXBean.getCurrentThreadCpuTime();
		long theUser1 = threadMXBean.getCurrentThreadUserTime();
		
		Times theResult = new Times(theTotal1-theTotal0, theCpu1-theCpu0, theUser1-theUser0);
		System.out.println(theResult);
		return theResult;
	}
	
	/**
	 * Timing information of a task.
	 */
	private static class Times
	{
		public final float itsTotalTime;
		public final float itsCpuTime;
		public final float itsUserTime;
		
		public Times(long aTotalTimeNanos, long aCpuTimeNanos, long aUserTimeNanos)
		{
			itsTotalTime = aTotalTimeNanos / 1000000000f;
			itsCpuTime = aCpuTimeNanos / 1000000000f;
			itsUserTime = aUserTimeNanos / 1000000000f;
		}
		
		@Override
		public String toString()
		{
			return String.format(
					"Timings [total: %.3fs, cpu: %.3fs, user: %.3fs]", 
					itsTotalTime, 
					itsCpuTime,
					itsUserTime);
		}
	}

	public static void runBench(int aT, int aNOps)
	{
		System.out.println("Bench with t="+aT+" and nops="+aNOps);
		
		Config.setT(aT);

		
		File theFile = new File("/home/gpothier/tmp/btree");
		theFile.delete();
		
		BTree theTree;
		theTree = new BTree(theFile, false);
		run(new PutBenchCreate(theTree, aNOps));
		
		int theReadCount1 = theTree.getManager().getReadCount();
		int theWriteCount1 = theTree.getManager().getWriteCount();
		long theSize1 = theFile.length();
		
		theTree = new BTree(theFile, false);
		run(new PutBenchVerify(theTree, aNOps));

		int theReadCount2 = theTree.getManager().getReadCount();
		
		System.out.println("File size: "+theSize1);
		System.out.println("Reads 1: "+theReadCount1);
		System.out.println("Writes 1: "+theWriteCount1);
		System.out.println("Reads 2: "+theReadCount2);
		System.out.println("----");
		
		theFile.delete();
	}
	
	private static class PutBenchCreate implements Runnable
	{
		private BTree itsTree;
		private final int itsNOps;

		public PutBenchCreate(BTree aTree, int aNOps)
		{
			itsTree = aTree;
			itsNOps = aNOps;
		}
		
		public void run()
		{
			System.out.println("Creation");
			Random theRandom = new Random(0);

			long theKeysCount = 0;
			for (int i=0;i<itsNOps;i++)
			{
				long theKey = theRandom.nextLong() * 2;
				long theValue = value(theKey);
				if (itsTree.put(theKey, theValue)) theKeysCount++;
			}
			
			System.out.println("Keys count: "+theKeysCount);
		}
	}
	
	private static class PutBenchVerify implements Runnable
	{
		private BTree itsTree;
		private final int itsNOps;

		public PutBenchVerify(BTree aTree, int aNOps)
		{
			itsTree = aTree;
			itsNOps = aNOps;
		}
		
		public void run()
		{
			System.out.println("Verification");
			Random theRandom = new Random(0);

			for (int i=0;i<itsNOps;i++)
			{
				long theKey = theRandom.nextLong() * 2;
				long theExpectedValue = value(theKey);
				
				Long theValue = itsTree.get(theKey);
				assert theValue == theExpectedValue;
				
				theValue = itsTree.get(theKey+1);
				assert theValue == null;
			}

		}

	}
	
	public static void main(String[] args)
	{
		threadMXBean = ManagementFactory.getThreadMXBean();
		threadMXBean.setThreadCpuTimeEnabled(true);
		
		runBench(2,     100);
		runBench(10,    100);
		runBench(20,    100);
		runBench(50,    100);
		
		runBench(2,     1000);
		runBench(10,    1000);
		runBench(20,    1000);
		runBench(50,    1000);
		runBench(100,   1000);
		runBench(200,   1000);
		runBench(500,   1000);
		
		runBench(2,     10000);
		runBench(10,    10000);
		runBench(20,    10000);
		runBench(50,    10000);
		runBench(100,   10000);
		runBench(200,   10000);
		runBench(500,   10000);
		runBench(1000,  10000);
		
		runBench(2,     100000);
		runBench(10,    100000);
		runBench(20,    100000);
		runBench(50,    100000);
		runBench(100,   100000);
		runBench(200,   100000);
		runBench(500,   100000);
		runBench(1000,  100000);
	}
	
	/**
	 * We derive values from its keys so as to be able to verify
	 * associations.
	 */
	public static long value(long aKey)
	{
		return aKey*17;
	}
	
}
