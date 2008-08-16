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
import java.rmi.RemoteException;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Manages groups of {@link Remote} objects.
 * The representative of the group is an instance of {@link RIMonitoringServerProvider}.
 * Whenever a call to a method of an object of the group returns a remote object,
 * this object is made part of the group.
 * The idea is that each group should correspond to one remote JVM.
 * @author gpothier
 */
public class RMIGroupManager
{
	private static RMIGroupManager INSTANCE = new RMIGroupManager();

	/**
	 * Retrieves the singleton instance.
	 */
	public static RMIGroupManager get()
	{
		return INSTANCE;
	}
	
	private RMIGroupManager()
	{
	}

	
	/**
	 * Maps remote objects to their group representative.
	 */
	private Map<Remote, RIMonitoringServerProvider> itsGroupdMap =
		new WeakHashMap<Remote, RIMonitoringServerProvider>();
	
	private Map<RIMonitoringServerProvider, RIMonitoringServer> itsProvidersMap =
		new WeakHashMap<RIMonitoringServerProvider, RIMonitoringServer>();
	
	/**
	 * Retrieves the server corresponding to the given provider.
	 * If this is the first time we see this server, we register the
	 * local {@link MonitoringClient}.
	 */
	private RIMonitoringServer getServer(RIMonitoringServerProvider aProvider)
	{
		RIMonitoringServer theServer = itsProvidersMap.get(aProvider);
		if (theServer == null)
		{
			try
			{
				theServer = aProvider.getMonitoringServer();
				theServer.setClient(MonitoringClient.get());
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
			itsProvidersMap.put(aProvider, theServer);
		}
		return theServer;
	}
	
	
	/**
	 * Adds a link from called to result, meaning result was returned 
	 * by a call to called. 
	 */
	public void addLink(Remote aCalled, Remote aResult)
	{
		if (aCalled instanceof RIMonitoringServerProvider)
		{
			RIMonitoringServerProvider theProvider = (RIMonitoringServerProvider) aCalled;
			itsGroupdMap.put(aResult, theProvider);
		}
		else 
		{
			RIMonitoringServerProvider theProvider = itsGroupdMap.get(aCalled);
			if (theProvider == null) throw new RuntimeException("Called remote object "+aCalled+", which was not registered.");
			itsGroupdMap.put(aResult, theProvider);
		}
	}
	
	/**
	 * Returns the server provided by the representative of the given remote object, if available.
	 */
	public RIMonitoringServer getServer(Remote aObject)
	{
		RIMonitoringServerProvider theProvider;
		if (aObject instanceof RIMonitoringServerProvider) theProvider = (RIMonitoringServerProvider) aObject;
		else theProvider = itsGroupdMap.get(aObject);
		
		if (theProvider == null) throw new RuntimeException("Remote object not registered: "+aObject);
		return getServer(theProvider);
	}
}
