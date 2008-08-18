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

/**
 * An entity that permits to schedule jobs.
 * @author gpothier
 */
public interface IJobScheduler
{
	/**
	 * Submits a job for asynchronous execution.
	 * @param aPriority The priority of the job. Jobs with higher priority 
	 * are always executed before jobs with lower priority, even if they
	 * are submitted later.
	 * See {@link JobPriority}.
	 * 
	 * @return a {@link ITaskMonitor} that can be used to track the job's
	 * progress and to cancel the job.
	 */
	public ITaskMonitor submit(JobPriority aPriority, Runnable aRunnable);
	
	/**
	 * Same as {@link #submit(JobPriority, Runnable)}, but specifying an existing monitor.
	 */
	public void submit(JobPriority aPriority, Runnable aRunnable, TaskMonitor aMonitor);
	
	/**
	 * Cancel all the jobs of this scheduler.
	 */
	public void cancelAll();
	
	
	public enum JobPriority
	{
		/**
		 * The default priority. Use if you don't know what priority to use.
		 */
		DEFAULT(0),
		
		/**
		 * Priority for jobs that have been explicitly requested by the user.
		 */
		EXPLICIT(1),
		
		/**
		 * Priority for auxilliary jobs that have not been explicitly requested by the user.
		 */
		AUTO(-1);
		
		private final int itsValue;

		private JobPriority(int aValue)
		{
			itsValue = aValue;
		}
		
		int getValue()
		{
			return itsValue;
		}
	}
}
