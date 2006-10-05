/*
 * Created on Oct 4, 2006
 */
package tod.experiments;

import java.util.Random;

import tod.agent.AgentUtils;

public class Timing
{
	public static void main(String[] args)
	{
		for (int i=0;i<5;i++)
		{
			new MyThread().start();
		}
	}
	
	private static class MyThread extends Thread
	{
		@Override
		public void run()
		{
			long last = 0;
			
			try
			{
				long i = 0;
				Random theRandom = new Random();
				while(true)
				{
					long t = AgentUtils.timestamp();
					if (t < last) System.out.println("ooo");
					last = t;
					
					Thread.sleep(theRandom.nextInt(2));
					
					if (i % 1000 == 0) System.out.println(i);
					i++;
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
