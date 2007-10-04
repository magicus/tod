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

import reflex.lib.pom.POMGroupDef;
import reflex.lib.pom.POMScheduler;
import reflex.lib.pom.Request;
import reflex.lib.pom.RequestIterator;

/**
 * This POM scheduler ensures that all calls to the database are serialized.
 * @author gpothier
 */
public class Scheduler extends POMScheduler implements POMGroupDef
{
	private Request itsExecutingRequest;
	
	public Scheduler()
	{
		new DeadlockDetectorThread().start();
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
	}
	
	@Override
	protected void scheduling(Request aReq)
	{
		System.out.println(String.format(
				"[Scheduler] Scheduler (%s) - executing %s on "+Thread.currentThread().getName(),
				this,
				aReq));

		if (itsExecutingRequest != null) throw new IllegalStateException();
		itsExecutingRequest = aReq;
	}
	
	@Override
	protected void leave(Request aReq)
	{
		if (itsExecutingRequest != aReq) throw new IllegalStateException();
		itsExecutingRequest = null;
		System.out.println("[Scheduler] Scheduler.leave() on "+Thread.currentThread().getName());
	}
	
	public Object getGroup(Object aObject)
	{
		return "dbgrid";
	}

	private class DeadlockDetectorThread extends Thread
	{
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
			System.out.println("[Scheduler] Deadlock detected");
			System.out.println("  Current request: "+itsExecutingRequest+" on "+itsExecutingRequest.getThread());
			System.out.println("  Attempting to interrupt.");
			itsExecutingRequest.getThread().interrupt();
		}
	}
}
