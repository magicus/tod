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
package tod.impl.dbgrid;

import java.util.Map;
import java.util.WeakHashMap;

import reflex.lib.pom.POMGroupDef;
import reflex.lib.pom.ReentrantPOMScheduler;
import reflex.lib.pom.Request;
import reflex.lib.pom.RequestIterator;
import tod.core.database.browser.ILogBrowser;
import tod.core.session.ISessionMonitor;
import tod.utils.TODUtils;

/**
 * This POM scheduler ensures that all calls to the database are serialized.
 * @author gpothier
 */
public class Scheduler extends ReentrantPOMScheduler 
implements POMGroupDef, ISessionMonitor
{
	private static Map<ILogBrowser, Scheduler> itsGroupsMap = new WeakHashMap<ILogBrowser, Scheduler>();
	
	/**
	 * Returns the scheduler used by the group whose key is the specified log browser.
	 */
	public static Scheduler get(ILogBrowser aLogBrowser)
	{
		return itsGroupsMap.get(aLogBrowser);
	}
	
	private Request itsExecutingRequest;
	private int itsActiveTaskCount = 0;
	
	public Scheduler()
	{
//		new DeadlockDetectorThread().start();
	}

	@Override
	protected void setGroup(Object aGroup)
	{
		super.setGroup(aGroup);
		itsGroupsMap.put((ILogBrowser) aGroup, this);
	}
	
	public int getQueueSize()
	{
		return super.getQueueSize()+itsActiveTaskCount;
	}
	
	@Override
	protected void schedule()
	{
		
		TODUtils.log(2,"[Scheduler] Schedule... req: "+itsExecutingRequest+" on "+Thread.currentThread().getName());
		RequestIterator theIterator = iterator();
		while (theIterator.hasNext())
		{
			Request theRequest = theIterator.next();
			TODUtils.log(2,"[Scheduler] Request: "+theRequest);
		}
		if (itsExecutingRequest == null) {
			executeOldest();
			TODUtils.log(2,"[Scheduler] Schedule done on "+Thread.currentThread().getName());
			itsActiveTaskCount++; 
		}
		
	}
	
	@Override
	protected void scheduling(Request aReq)
	{
		if (aReq.isReentering())
		{
			System.out.println("");

			TODUtils.logf(2,
					"[Scheduler] Scheduler (%s) - reentering request %s on "+Thread.currentThread().getName(),
					this,
					aReq);
					
		}
		else
		{
			TODUtils.logf(2,
					"[Scheduler] Scheduler (%s) - executing %s on "+Thread.currentThread().getName(),
					this,
					aReq);
			if (itsExecutingRequest != null) throw new IllegalStateException();
			itsExecutingRequest = aReq;
		}
	}
	
	@Override
	protected void leave(Request aReq)
	{
		if (itsExecutingRequest != aReq) throw new IllegalStateException();
		itsExecutingRequest = null;
		TODUtils.log(2,"[Scheduler] Scheduler.leave() on "+Thread.currentThread().getName());
		
		itsActiveTaskCount--;
	}
	
	public Object getGroup(Object aObject)
	{
		IScheduled theScheduled = (IScheduled) aObject;
		return theScheduled.getKey();
	}

	private class DeadlockDetectorThread extends Thread
	{
		public DeadlockDetectorThread()
		{
			super("DeadlockDetector");
		}

		@Override
		public void run()
		{
			try
			{
				Request theLastRequest = null;
				int theCount = 0;
				while(true)
				{
					if (itsExecutingRequest != null)
					{
						if (itsExecutingRequest == theLastRequest)
						{
							theCount++;
							if (theCount > 10) deadlock();
						}
						else
						{
							theCount = 0;
							theLastRequest = itsExecutingRequest;
						}
					}
					else
					{
						theLastRequest = null;
						theCount = 0;
					}
					Thread.sleep(5000);
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		private void deadlock()
		{
			Request theRequest = itsExecutingRequest;
			if (theRequest != null)
			{
				TODUtils.log(0,"[Scheduler] Deadlock detected");
				TODUtils.log(0,"  Current request: "+theRequest+" on "+theRequest.getThread());
				
				// Removed that: might terminate the JobProcessor thread (gp)
//				TODUtils.log(0,"  Attempting to interrupt.");
//				theRequest.getThread().interrupt();
			}
		}
	}
}
