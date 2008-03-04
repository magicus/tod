/*
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
package tod.core.session;

import java.net.URI;

import javax.swing.JComponent;

import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;

/**
 * A session that delegates to another session.
 * @author gpothier
 */
public abstract class DelegatedSession implements ISession
{
	private ISession itsDelegate;

	public DelegatedSession(ISession aDelegate)
	{
		itsDelegate = aDelegate;
	}

	public ISession getDelegate()
	{
		return itsDelegate;
	}

	public TODConfig getConfig()
	{
		return itsDelegate.getConfig();
	}

	public void disconnect()
	{
		itsDelegate.disconnect();
	}

	public void flush()
	{
		itsDelegate.flush();
	}

	public ILogBrowser getLogBrowser()
	{
		return itsDelegate.getLogBrowser();
	}

	public ISessionMonitor getMonitor()
	{
		return itsDelegate.getMonitor();
	}

	public URI getUri()
	{
		return itsDelegate.getUri();
	}

	public JComponent createConsole()
	{
		return itsDelegate.createConsole();
	}

	public ConnectionInfo getConnectionInfo()
	{
		return itsDelegate.getConnectionInfo();
	}
	
	public boolean isAlive()
	{
		return itsDelegate.isAlive();
	}
}
