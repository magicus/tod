/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid.bench;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.transport.DummyCollector;
import tod.impl.bci.asm.ASMLocationPool;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dbnode.DatabaseNode;

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

	/**
	 * Standard setup of a grid master that waits for a number
	 * of database nodes to connect
	 */
	public static GridMaster setupMaster(String[] args) throws Exception
	{
		int theExpectedNodes = 0;
		if (args.length > 0)
		{
			theExpectedNodes = Integer.parseInt(args[0]);
		}
		
		return setupMaster(theExpectedNodes);
	}
		
	/**
	 * Standard setup of a grid master that waits for a number
	 * of database nodes to connect
	 */
	public static GridMaster setupMaster(int aExpectedNodes) throws Exception
	{
		System.out.println("Expecting "+aExpectedNodes+" nodes");
		
		LocationRegistrer theLocationRegistrer = new LocationRegistrer();
		new ASMLocationPool(theLocationRegistrer, new File("/home/gpothier/tmp/tod"));
		GridMaster theMaster = new GridMaster(theLocationRegistrer);
		
		Registry theRegistry = LocateRegistry.createRegistry(1099);
		
		theRegistry.bind(GridMaster.RMI_ID, theMaster);
		
		System.out.println("Bound master");

		if (aExpectedNodes > 0)
		{
			while (theMaster.getNodeCount() < aExpectedNodes)
			{
				Thread.sleep(1000);
				System.out.println("Found "+theMaster.getNodeCount()+"/"+aExpectedNodes+" nodes.");
			}
		}
		else new DatabaseNode(true);

		return theMaster;
	}
	

}
