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
	private int itsQueueSize = 0;
	
	public Scheduler()
	{
		new DeadlockDetectorThread().start();
	}

	@Override
	protected void setGroup(Object aGroup)
	{
		super.setGroup(aGroup);
		itsGroupsMap.put((ILogBrowser) aGroup, this);
	}
	
	public int getQueueSize()
	{
		return itsQueueSize;
	}
	
	@Override
	protected void schedule()
	{
		System.out.println("[Scheduler] Schedule... req: "+itsExecutingRequest+" on "+Thread.currentThread().getName());
		RequestIterator theIterator = iterator();
		while (theIterator.hasNext())
		{
			Request theRequest = theIterator.next();
			System.out.println("[Scheduler] Request: "+theRequest);
		}
		if (itsExecutingRequest == null) executeOldest();
		System.out.println("[Scheduler] Schedule done on "+Thread.currentThread().getName());
		
		itsQueueSize++;
	}
	
	@Override
	protected void scheduling(Request aReq)
	{
		if (aReq.isReentering())
		{
			System.out.println("");

			System.out.println(String.format(
					"[Scheduler] Scheduler (%s) - reentering request %s on "+Thread.currentThread().getName(),
					this,
					aReq));
		}
		else
		{
			System.out.println(String.format(
					"[Scheduler] Scheduler (%s) - executing %s on "+Thread.currentThread().getName(),
					this,
					aReq));
	
			if (itsExecutingRequest != null) throw new IllegalStateException();
			itsExecutingRequest = aReq;
		}
	}
	
	@Override
	protected void leave(Request aReq)
	{
		if (itsExecutingRequest != aReq) throw new IllegalStateException();
		itsExecutingRequest = null;
		System.out.println("[Scheduler] Scheduler.leave() on "+Thread.currentThread().getName());
		
		itsQueueSize--;
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
					Thread.sleep(1000);
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
				System.out.println("[Scheduler] Deadlock detected");
				System.out.println("  Current request: "+theRequest+" on "+theRequest.getThread());
				System.out.println("  Attempting to interrupt.");
				theRequest.getThread().interrupt();
			}
		}
	}
}
