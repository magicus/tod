/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import zz.utils.ITask;

/**
 * Permits to process jobs asynchronously. Clients submit jobs
 * and are notified upon completion. Jobs can also be cancelled.
 * <br/>
 * Job processors can be organized in a hierarchy so that a child
 * job processor can be cancelled ({@link #cancelAll()}) without
 * affecting sibling processors.
 * @author gpothier
 */
public class JobProcessor extends Thread
{
	private JobProcessor itsParent;
	private LinkedList<Job> itsJobs = new LinkedList<Job>();
	private List<JobProcessor> itsChildren = new ArrayList<JobProcessor>();
	
	private Semaphore itsSemaphore = new Semaphore(1);
	
	/**
	 * Creates a root job processor.
	 */
	public JobProcessor()
	{
		super("JobProcessor");
		start();
	}

	/**
	 * Creates a child job processor.
	 */
	public JobProcessor(JobProcessor aParent)
	{
		itsParent = aParent;
		itsParent.addChild(this);
	}
	
	/**
	 * Detaches this job processor from its parent
	 */
	public void detach()
	{
		itsParent.removeChild(this);
		itsParent = null;
	}
	
	private void addChild(JobProcessor aChild)
	{
		itsChildren.add(aChild);
	}

	private void removeChild(JobProcessor aChild)
	{
		itsChildren.remove(aChild);
	}
	
	/**
	 * Submits a new job to this processor, with no listener.
	 */
	public <R> void submit(Job<R> aJob)
	{
		submit(aJob, null);
	}

	/**
	 * Executes a task once the job processor has finished executing
	 * the current job. This method blocks until the job is finished. 
	 */
	public <R> void runNow(Job<R> aJob)
	{
		getRoot().runJob(aJob);
	}
	
	
	private JobProcessor getRoot()
	{
		JobProcessor theRoot = this;
		while(theRoot.itsParent != null) theRoot = theRoot.itsParent;
		return theRoot;
	}
	
	/**
	 * Submits a new job to this processor.
	 */
	public <R> void submit(Job<R> aJob, IJobListener<R> aListener)
	{
		synchronized (getRoot())
		{
			aJob.setup(this, aListener);
			itsJobs.addLast(aJob);
//			System.out.println("Job queue size: "+itsJobs.size());
			
			getRoot().notifyAll();			
		}
	}
	
	private <R> void cancel(Job<R> aJob)
	{
		synchronized (getRoot())
		{
			itsJobs.remove(aJob);
		}
	}
	
	private <R> void boost(Job<R> aJob)
	{
		synchronized (getRoot())
		{
			if (itsJobs.remove(aJob)) 
			{
				itsJobs.addFirst(aJob);
				getRoot().notifyAll();
			}
		}
	}
	
	/**
	 * Cancels all scheduled jobs
	 */
	public void cancelAll()
	{
		synchronized (getRoot())
		{
			itsJobs.clear();
			for (JobProcessor theChild : itsChildren) theChild.cancelAll();
		}
	}
	
	private Job getNextJob()
	{
		synchronized (getRoot())
		{
			if (itsJobs.isEmpty())
			{
				for (JobProcessor theChild : itsChildren)
				{
					Job theJob = theChild.getNextJob();
					if (theJob != null) return theJob;
				}
				return null;
			}
			else
			{
				return itsJobs.removeFirst();
			}
		}
	}
	
	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				Job theJob = getNextJob();
				if (theJob != null) runJob(theJob);
				else 
				{
					synchronized (getRoot())
					{
						wait();
					}
				}
			}
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void runJob(Job aJob)
	{
//		System.out.println("[JobUpdater] Processing job: "+aJob);
		try
		{
			itsSemaphore.acquire();
			aJob.doRun();
		}
		catch (Exception e)
		{
			System.err.println("[JobUpdater] Exception in Job: "+aJob);
			e.printStackTrace();
		}
		finally
		{
			itsSemaphore.release();
		}
//		System.out.println("[JobUpdater] Job queue size: "+itsJobs.size());		
	}
	
	public static abstract class Job<R>
	{
		private JobProcessor itsProcessor;
		private IJobListener<R> itsListener;
		private boolean itsCancelled = false;
		private boolean itsCompleted = false;
		
		private void setup(JobProcessor aProcessor, IJobListener<R> aListener)
		{
			assert itsProcessor == null;
			itsProcessor = aProcessor;
			itsListener = aListener;
		}
		
		private void doRun()
		{
			R theResult = run();
			if (itsListener != null) itsListener.jobFinished(theResult);
		}
		
		/**
		 * Executes this job
		 */
		public abstract R run();

		/**
		 * Indicates if this job has been cancelled.
		 * Subclasses can check this flag in their implementation
		 * of {@link #run()}.
		 */
		public final boolean isCancelled()
		{
			return itsCancelled;
		}
		
		/**
		 * Cancels this job. If the job is already executing, it might
		 * not be actually cancelled.
		 */
		public final void cancel()
		{
			itsProcessor.cancel(this);
			itsCancelled = true;
		}
		
		/**
		 * Boosts the priority of this job.
		 */
		public final void boost()
		{
			itsProcessor.boost(this);
		}
		
	}
	
	public interface IJobListener<R>
	{
		public void jobFinished(R aResult);
	}
}
