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
package tod.tools.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import tod.tools.monitoring.ITaskMonitor;
import tod.tools.monitoring.TaskMonitor;
import tod.tools.monitoring.TaskMonitoring;
import tod.tools.monitoring.TaskMonitor.TaskCancelledException;
import zz.utils.properties.IProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

public class JobScheduler extends Thread
implements IJobScheduler
{
	private BlockingQueue<Job> itsQueuedJobs = new PriorityBlockingQueue<Job>();
	private Job itsCurrentJob;
	
	private IRWProperty<Integer> pQueueSize = new SimpleRWProperty<Integer>(this, 0);
	
	public JobScheduler()
	{
		super("JobScheduler");
		start();
	}

	private void updateQueueSize()
	{
		pQueueSize.set(itsQueuedJobs.size() + (itsCurrentJob != null ? 1 : 0));
	}
	
	public void cancelAll()
	{
		List<Job> theJobs = new ArrayList<Job>();
		itsQueuedJobs.drainTo(theJobs);
		for (Job theJob : theJobs) theJob.monitor.cancel();
		
		Job theCurrentJob = itsCurrentJob; // local var. for concurrency.
		if (theCurrentJob != null) theCurrentJob.monitor.cancel();
		
		updateQueueSize();
	}

	public ITaskMonitor submit(JobPriority aPriority, Runnable aRunnable)
	{
		TaskMonitor theMonitor = TaskMonitoring.createMonitor();
		submit(aPriority, aRunnable, theMonitor);
		return theMonitor;
	}
	
	void submit(JobPriority aPriority, Runnable aRunnable, TaskMonitor aMonitor)
	{
		itsQueuedJobs.offer(new Job(aPriority, aMonitor, aRunnable));
		updateQueueSize();
	}
	
	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				itsCurrentJob = itsQueuedJobs.take();

				updateQueueSize();

				if (itsCurrentJob.monitor.isCancelled()) continue;
				try
				{
					TaskMonitoring.start(itsCurrentJob.monitor);
					itsCurrentJob.runnable.run();
				}
				catch (TaskCancelledException e)
				{
					System.err.println("Task cancelled: "+itsCurrentJob);
				}
				catch (Throwable e)
				{
					System.err.println("Error while executing job: "+itsCurrentJob);
					e.printStackTrace();
				}
				finally
				{
					TaskMonitoring.stop();
					itsCurrentJob = null;
					updateQueueSize();
				}
			}
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * A property that holds the number of pending jobs.
	 */
	public IProperty<Integer> pQueueSize()
	{
		return pQueueSize;
	}
	
	private static class Job implements Comparable<Job>
	{
		private final JobPriority itsPriority;
		public final Runnable runnable;
		public final TaskMonitor monitor;

		public Job(JobPriority aPriority, TaskMonitor aMonitor, Runnable aRunnable)
		{
			itsPriority = aPriority;
			monitor = aMonitor;
			runnable = aRunnable;
		}

		public int compareTo(Job j)
		{
			return j.itsPriority.getValue() - itsPriority.getValue();
		}
	}
}
