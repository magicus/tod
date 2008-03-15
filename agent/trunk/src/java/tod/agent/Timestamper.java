/*
 * Created on Mar 14, 2008
 */
package tod.agent;

/**
 * A thread that maintains the current timestamp, with a granularity.
 * Permits to avoid too many system calls for obtaining the timestamp.
 * @author gpothier
 */
public class Timestamper extends Thread
{
	private static Timestamper INSTANCE = new Timestamper();

	private Timestamper()
	{
		setDaemon(true);
		start();
	}
	
	public transient static long t = System.nanoTime() << AgentConfig.TIMESTAMP_ADJUST_SHIFT;
	
	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				t = System.nanoTime() << AgentConfig.TIMESTAMP_ADJUST_SHIFT;
				sleep(50);
			}
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}
