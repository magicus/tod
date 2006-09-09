/*
 * Created on Sep 6, 2006
 */
package tod.impl.dbgrid.bench;

import org.junit.Test;

import tod.impl.dbgrid.bench.BenchBase.BenchResults;

public class BenchTimestamp
{
	@Test public void bench()
	{
		final int n = 1000000;
		
		BenchResults theMilisResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				for(int i=0;i<n;i++) System.currentTimeMillis();
			}
		});
		
		System.out.println("Milis: "+theMilisResults);
		
		BenchResults theNanosResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				for(int i=0;i<n;i++) System.nanoTime();
			}
		});
		
		System.out.println("Nanos: "+theNanosResults);
	}
}
