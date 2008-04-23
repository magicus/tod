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
package tod.core.session;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import tod.core.config.TODConfig;

public abstract class AbstractSession implements ISession
{
	private TODConfig itsConfig;
	private final URI itsUri;
	private final Set<IProgramLaunch> itsLaunches = new HashSet<IProgramLaunch>();

	public AbstractSession(URI aUri, TODConfig aConfig)
	{
		itsUri = aUri;
		itsConfig = aConfig;
	}

	public URI getUri()
	{
		return itsUri;
	}

	public TODConfig getConfig()
	{
		return itsConfig;
	} 
	
	public void setConfig(TODConfig aConfig)
	{
		itsConfig = aConfig;
	}

	public ConnectionInfo getConnectionInfo()
	{
		return new ConnectionInfo(
				getConfig().get(TODConfig.COLLECTOR_HOST), 
				getConfig().get(TODConfig.COLLECTOR_PORT));
	}

	public Set<IProgramLaunch> getLaunches()
	{
		return itsLaunches;
	}
}
