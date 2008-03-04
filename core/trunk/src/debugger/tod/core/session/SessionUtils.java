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

import java.lang.reflect.Constructor;
import java.net.URI;

import tod.core.config.TODConfig;
import tod.impl.local.LocalSession;
import tod.utils.CountTODServer;
import zz.utils.Utils;

/**
 * Utilies to manage sessions.
 * @author gpothier
 */
public class SessionUtils
{
	/**
	 * Returns the class of session that should be created for the
	 * specified config.
	 */
	public static Class<? extends ISession> getSessionClass(TODConfig aConfig)
	{
		String theType = aConfig.get(TODConfig.SESSION_TYPE);
		String theClassName;
		if (TODConfig.SESSION_MEMORY.equals(theType)) theClassName = LocalSession.class.getName();
		else if (TODConfig.SESSION_LOCAL.equals(theType)) theClassName = "tod.impl.dbgrid.LocalGridSession";
		else if (TODConfig.SESSION_REMOTE.equals(theType)) theClassName = "tod.impl.dbgrid.RemoteGridSession";
		else if (TODConfig.SESSION_COUNT.equals(theType)) theClassName = CountTODServer.class.getName();
		else throw new RuntimeException("Not handled: "+theType);

		try
		{
			return (Class) Class.forName(theClassName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Creates a session for the specified config.
	 */
	public static ISession createSession(TODConfig aConfig)
	{
		try
		{
			Class theSessionClass = getSessionClass(aConfig);
			Constructor theConstructor = 
				theSessionClass.getConstructor(URI.class, TODConfig.class);
			
			return (ISession) theConstructor.newInstance(null, aConfig);
		}
		catch (Exception e)
		{
			throw new SessionCreationException(
					"Cannot create session: "+Utils.getRootCause(e).getMessage(), 
					e);
		}
	}
	
}
