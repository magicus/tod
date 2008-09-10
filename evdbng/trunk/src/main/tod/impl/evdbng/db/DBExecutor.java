package tod.impl.evdbng.db;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import tod.impl.evdbng.DebuggerGridConfigNG;
import zz.utils.Utils;

/**
 * Manages the executor for database tasks. The executor uses a thread pool
 * to leverage multicore machines.
 * Each task submitted to the executor belongs to a group (represented by an
 * integer, usually the hash code of the "owner" of the tasks); the executor
 * guarantees that all the tasks from a given group will always be executed by
 * the same thread. (note that this required guarantee prevents us from using 
 * {@link ThreadPoolExecutor}).
 * @author gpothier
 *
 */
public class DBExecutor
{
	private static final Worker[] itsWorkers = new Worker[DebuggerGridConfigNG.DB_THREADS];
	
	static
	{
		Utils.println("DBExecutor - using %d threads", DebuggerGridConfigNG.DB_THREADS);
		for(int i=0;i<DebuggerGridConfigNG.DB_THREADS;i++) itsWorkers[i] = new Worker(i);
	}
	
	private static Throwable itsThrown = null;
//	private static int itsCount = 0;
	
	private static void checkThrown()
	{
		Throwable theThrown = itsThrown;
		if (theThrown != null) 
		{
			itsThrown = null;
			throw new RuntimeException(theThrown);
		}
	}
	
	public static void submit(DBTask aTask)
	{
		checkThrown();
		
		// Queue the task
		int theQueue = aTask.getGroup() % DebuggerGridConfigNG.DB_THREADS;
		itsWorkers[theQueue].submit(aTask);
//		itsCount++;
//		if (itsCount % 1024 == 0) System.out.println("executor: "+itsCount);
	}
	
	/**
	 * Submits the given task for execution and waits for it to complete.
	 */
	public static void submitAndWait(DBTask aTask)
	{
		int theQueue = aTask.getGroup() % DebuggerGridConfigNG.DB_THREADS;
		itsWorkers[theQueue].submit(aTask);
		NotifyTask theNotifyTask = new NotifyTask();
		itsWorkers[theQueue].submit(theNotifyTask);
		theNotifyTask.waitRun();
	}
	
	private static final class Worker extends Thread
	{
		private final ArrayBlockingQueue<DBTask> itsQueue = new ArrayBlockingQueue<DBTask>(128);
		
		/**
		 * Index of this worker within the pool
		 */
		private final int itsIndex;
		
		public Worker(int aIndex)
		{
			super("DB worker "+aIndex);
			itsIndex = aIndex;
			start();
		}

		public void submit(DBTask aTask) 
		{
			try
			{
				itsQueue.put(aTask);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void run()
		{
			while(true)
			{
				try
				{
					DBTask theTask = itsQueue.take();
					theTask.run();
				}
				catch (Throwable e)
				{
					itsThrown = e;
				}
			}
		}
	}
	
	
	public static abstract class DBTask 
	{
		/**
		 * Returns the id of the group to which the task belongs.
		 */
		public abstract int getGroup();

		/**
		 * Executes the task.
		 */
		public abstract void run();
	}
	
	/**
	 * A task that sends {@link #notifyAll()} to itself when run.
	 * @author gpothier
	 *
	 */
	private static class NotifyTask extends DBTask
	{
		@Override
		public int getGroup()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public synchronized void run()
		{
			notifyAll();
		}
		
		/**
		 * Waits until the task is run.
		 */
		public synchronized void waitRun()
		{
			try
			{
				wait();
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		
	}
}
