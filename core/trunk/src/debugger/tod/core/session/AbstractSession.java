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
package tod.core.session;

import java.net.URI;

import tod.core.config.TODConfig;

public abstract class AbstractSession implements ISession
{
	private TODConfig itsConfig;
	private final URI itsUri;

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
	
}
