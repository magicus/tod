/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid.bench;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class BenchBase
{
	private static ThreadMXBean threadMXBean;
	
	static
	{
		try
		{
			threadMXBean = ManagementFactory.getThreadMXBean();
			threadMXBean.setThreadCpuTimeEnabled(true);
		}
		catch (Throwable e)
		{
			threadMXBean = null;
		}
	}
	
	public static BenchResults benchmark(Runnable aRunnable)
	{
		long t0;
		long c0 = 0;
		long u0 = 0;
		long t1;
		long c1 = 0;
		long u1 = 0;
		
		t0 = System.currentTimeMillis();
		if (threadMXBean != null)
		{
			c0 = threadMXBean.getCurrentThreadCpuTime();
			u0 = threadMXBean.getCurrentThreadUserTime();
		}

		aRunnable.run();
		
		t1 = System.currentTimeMillis();
		if (threadMXBean != null)
		{
			c1 = threadMXBean.getCurrentThreadCpuTime();
			u1 = threadMXBean.getCurrentThreadUserTime();
		}

		return new BenchResults(t1-t0, c1-c0, u1-u0);
	}
	
	public static class BenchResults
	{
		/**
		 * Total execution time, in milliseconds
		 */
		public final long totalTime;
		
		/**
		 * CPU time, nanoseconds
		 */
		public final long cpuTime;
		
		/**
		 * CPU user time, nanoseconds.
		 */
		public final long userTime;
		
		public BenchResults(long aTotalTime, long aCpuTime, long aUserTime)
		{
			totalTime = aTotalTime;
			cpuTime = aCpuTime;
			userTime = aUserTime;
		}
		
		@Override
		public String toString()
		{
			return "total: "+totalTime+"ms, cpu: "+(cpuTime/1000000)+"ms, user: "+(userTime/1000000)+"ms";
		}
		
	}


}
