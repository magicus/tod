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
package tod.core.server;

import tod.core.config.TODConfig;
import tod.utils.TODUtils;
import zz.utils.net.Server;
import zz.utils.properties.IProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * A TOD server accepts connections from debugged VMs and process instrumentation
 * requests as well as logged events.
 * The actual implementation of the instrumenter and database are left
 * to delegates.
 * @author gpothier
 */
public abstract class TODServer extends Server
{
	private TODConfig itsConfig;
	private IRWProperty<Boolean> pConnected = new SimpleRWProperty<Boolean>();
	
	public TODServer(TODConfig aConfig)
	{
		super(aConfig.get(TODConfig.COLLECTOR_PORT));
		TODUtils.logf(0, "TODServer on port: %d", getPort());

		itsConfig = aConfig;
	}
	
	public void setConfig(TODConfig aConfig)
	{
		itsConfig = aConfig;
	}

	public TODConfig getConfig()
	{
		return itsConfig;
	}
	
	/**
	 * This property indicates if the server is connected to a debuggee VM or not.
	 */
	public IProperty<Boolean> pConnected()
	{
		return pConnected;
	}
	
	/**
	 * Causes this server to stop accepting connections.
	 */
	@Override
	public void close()
	{
		System.out.println("Server disconnecting...");
		super.close();
		System.out.println("Server disconnected.");
	}
	
	/**
	 * Disconnects from all currently connected VMs.
	 * Subclasses should override and call super.
	 */
	public synchronized void disconnect()
	{
		disconnected();
	}

	/**
	 * This method is called when target VMs are disconnected.
	 */
	protected void disconnected()
	{
		pConnected.set(false);
	}

	/**
	 * This method is called when a client connects to the server when there
	 * are no open connections (ie, when the server passes from the "no client
	 * connected" state to the "client(s) connected" state).
	 */
	protected void connected()
	{
		pConnected.set(true);
	}
	
	/**
	 * Retrieves the {@link ITODServerFactory} to use for the given config.
	 */
	public static ITODServerFactory getFactory(TODConfig aConfig)
	{
		try
		{
			String theClassName = aConfig.get(TODConfig.SERVER_TYPE);
			Class<?> theClass = Class.forName(theClassName);
			return (ITODServerFactory) theClass.newInstance();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
