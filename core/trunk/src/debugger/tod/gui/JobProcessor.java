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
package tod.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import zz.utils.Future;

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
	 * Returns a Future whose value is retrieved by the specified job. 
	 */
	public <R> Future<R> getFuture(Job<R> aJob)
	{
		FutureJob<R> theFuture = new FutureJob<R>();
		submit(aJob, theFuture);
		return theFuture;
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
		catch (Throwable t)
		{
			System.err.println("[JobUpdater] Exception in Job: "+aJob);
			t.printStackTrace();
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
	
	private static class FutureJob<R> extends Future<R> implements IJobListener<R>
	{
		public FutureJob()
		{
			super(false);
		}
		
		public void jobFinished(R aResult)
		{
			done(aResult);
		}

		@Override
		protected R fetch() throws Throwable
		{
			throw new UnsupportedOperationException();
		}
	}
}
