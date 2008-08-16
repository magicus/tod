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

import tod.tools.monitoring.ITaskMonitor;
import tod.tools.monitoring.TaskMonitor;
import tod.tools.monitoring.TaskMonitoring;
import zz.utils.list.NakedLinkedList;
import zz.utils.list.NakedLinkedList.Entry;

/**
 * 
 * @author gpothier
 */
public class JobGroup implements IJobScheduler
{
	private final JobScheduler itsScheduler;
	private final NakedLinkedList<JobWrapper> itsJobs = new NakedLinkedList<JobWrapper>();

	public JobGroup(JobScheduler aScheduler)
	{
		itsScheduler = aScheduler;
	};
	
	public synchronized void cancelAll()
	{
		while(itsJobs.size() > 0)
		{
			Entry<JobWrapper> theEntry = itsJobs.getFirstEntry();
			theEntry.getValue().cancel();
			itsJobs.remove(theEntry);
		}
	}

	public synchronized ITaskMonitor submit(JobPriority aPriority, Runnable aRunnable)
	{
		TaskMonitor theMonitor = TaskMonitoring.createMonitor();
		JobWrapper theWrapper = new JobWrapper(theMonitor, aRunnable);
		Entry<JobWrapper> theEntry = itsJobs.createEntry(theWrapper);
		theWrapper.setEntry(theEntry);
		itsJobs.addLast(theEntry);
		
		itsScheduler.submit(aPriority, theWrapper, theMonitor);
		return theMonitor;
	}
	
	private synchronized void remove(Entry<JobWrapper> aEntry)
	{
		if (aEntry.isAttached()) itsJobs.remove(aEntry);
	}

	/**
	 * Wraps the real job so as to be able to remove it from the list
	 * when completed.
	 * @author gpothier
	 */
	private class JobWrapper implements Runnable
	{
		private Entry<JobWrapper> itsEntry;
		private final Runnable itsRealJob;
		private final TaskMonitor itsMonitor;

		public JobWrapper(TaskMonitor aMonitor, Runnable aRealJob)
		{
			itsMonitor = aMonitor;
			itsRealJob = aRealJob;
		}
		
		public void setEntry(Entry<JobWrapper> aEntry)
		{
			itsEntry = aEntry;
		}
		
		public void cancel()
		{
			itsMonitor.cancel();
		}

		public void run()
		{
			try
			{
				itsRealJob.run();
			}
			finally
			{
				remove(itsEntry);
			}
		}
	}
}
