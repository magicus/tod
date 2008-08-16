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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import tod.core.DebugFlags;
import tod.tools.monitoring.MonitoringClient.MonitorId;

public class MonitoringServer extends UnicastRemoteObject
implements RIMonitoringServer
{
	private static MonitoringServer INSTANCE;
	static
	{
		try
		{
			INSTANCE = new MonitoringServer();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves the singleton instance.
	 */
	public static MonitoringServer get()
	{
		return INSTANCE;
	}
	
	private RIMonitoringClient itsClient;
	private Map<MonitorId, TaskMonitor> itsMonitorsMap =
		new HashMap<MonitorId, TaskMonitor>();
	
	private MonitoringServer() throws RemoteException
	{
	}

	public void monitorCancelled(MonitorId aId)
	{
		if (DebugFlags.TRACE_MONITORING) System.out.println("Monitor cancelled: "+aId);
		TaskMonitor theMonitor = itsMonitorsMap.get(aId);
		if (theMonitor == null) throw new RuntimeException("Not monitor for id: "+aId);
		theMonitor.cancel();
	}

	public void setClient(RIMonitoringClient aClient)
	{
		assert itsClient == null;
		itsClient = aClient;
	}
	
	/**
	 * Assigns a monitor to a monitor id.
	 */
	public void assign(MonitorId aId, TaskMonitor aMonitor)
	{
		if (DebugFlags.TRACE_MONITORING) System.out.println("Assigning monitor "+aId);
		assert aId != null;
		assert aMonitor != null;
		itsMonitorsMap.put(aId, aMonitor);
	}
	
	/**
	 * Removes the monitor assigned to the given id.
	 */
	public void delete(MonitorId aId)
	{
		if (DebugFlags.TRACE_MONITORING) System.out.println("Deleting monitor "+aId);
		TaskMonitor theMonitor = itsMonitorsMap.remove(aId);
		if (theMonitor == null) throw new RuntimeException("No monitor for id: "+aId);		
	}
}
