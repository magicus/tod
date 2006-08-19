/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid.bench;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class BenchBase
{
	private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
	
	static
	{
		threadMXBean.setThreadCpuTimeEnabled(true);
	}
	
	public static BenchResults benchmark(Runnable aRunnable)
	{
		long t0 = System.currentTimeMillis();
		long c0 = threadMXBean.getCurrentThreadCpuTime();
		long u0 = threadMXBean.getCurrentThreadUserTime();

		aRunnable.run();
		
		long t1 = System.currentTimeMillis();
		long c1 = threadMXBean.getCurrentThreadCpuTime();
		long u1 = threadMXBean.getCurrentThreadUserTime();

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
