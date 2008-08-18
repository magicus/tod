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
package tod.tools.monitoring;

import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

import tod.core.DebugFlags;
import tod.tools.monitoring.MonitoringClient.MonitorId;

public aspect RemoteMonitoringManager
{
	pointcut monitoredCall(Remote aTarget, MonitorId aId): 
		call(* Remote+.*(MonitorId, ..)) 
		&& !within(tod.tools.monitoring.**)
		&& args(aId, ..) && target(aTarget);
	
	/**
	 * Obtains an id for the current monitor and transmits this id
	 * to the server side.
	 */
	Object around(Remote aTarget, MonitorId aId): monitoredCall(aTarget, aId)
	{
		if (aTarget instanceof UnicastRemoteObject)
		{
			// This is a local object
			return proceed(aTarget, aId);
		}

		MonitorId theRealId = null;
		try
		{
			TaskMonitor theMonitor = TaskMonitor.current();

			if (theMonitor != null)
			{
				RIMonitoringServer theServer = RMIGroupManager.get().getServer(aTarget);
				theRealId = MonitoringClient.get().createId(theMonitor, theServer);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		
		try
		{
			return proceed(aTarget, theRealId);
		}
		finally
		{
			if (theRealId != null) MonitoringClient.get().destroyId(theRealId);
		}
	}
	
	pointcut monitoredExec(Remote aSubject, MonitorId aId): 
		execution(* Remote+.*(MonitorId, ..)) 
		&& !within(tod.tools.monitoring.**)
		&& args(aId, ..) && this(aSubject);
	
	/**
	 * Receives an id from the client and initializes a monitor.
	 */
	before(Remote aSubject, MonitorId aId): monitoredExec(aSubject, aId)
	{
		if (aSubject instanceof UnicastRemoteObject)
		{
			// This is a local object
			return;
		}

		try
		{
			TaskMonitor theMonitor = TaskMonitoring.start();
			MonitoringServer.get().assign(aId, theMonitor);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Cleans up the monitor for this call.
	 */
	after(Remote aSubject, MonitorId aId): monitoredExec(aSubject, aId)
	{
		if (aSubject instanceof UnicastRemoteObject)
		{
			// This is a local object
			return;
		}

		try
		{
			MonitoringServer.get().delete(aId);
			TaskMonitoring.stop();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	pointcut remoteCall(Remote aTarget): 
//		call(Remote+ Remote+.*(..))
//		&& !call(* java..*(..)) // avoids trapping calls to Registry
		call(@RemoteLinker Remote+ *.*(..))
		&& target(aTarget);
	
	/**
	 * Set up the Remote objects groups.
	 */
	after(Remote aTarget) returning(Remote aResult): remoteCall(aTarget)
	{
		try
		{
			if (DebugFlags.TRACE_MONITORING) System.out.println("[RemoteMonitoringManager] At: "+thisJoinPoint.toLongString());
			
			if (aTarget instanceof UnicastRemoteObject)
			{
				if (DebugFlags.TRACE_MONITORING) System.out.println("[RemoteMonitoringManager] Object is local, skipping: "+aTarget);
			}
			else
			{
				if (DebugFlags.TRACE_MONITORING) System.out.println("[RemoteMonitoringManager] Linking "+aTarget+" -> "+aResult);
				RMIGroupManager.get().addLink(aTarget, aResult);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
}
