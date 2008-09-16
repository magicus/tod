/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.core.session;

import java.lang.reflect.Constructor;
import java.net.URI;

import tod.core.config.TODConfig;
import tod.impl.local.LocalSession;
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
		else if (TODConfig.SESSION_COUNT.equals(theType)) throw new UnsupportedOperationException("Reimplement if needed");
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
	 * Returns the class of the root session (considering delegated sessions).
	 */
	public static Class<? extends ISession> getSessionClass(ISession aSession)
	{
		while(aSession != null)
		{
			if (aSession instanceof DelegatedSession)
			{
				DelegatedSession theSession = (DelegatedSession) aSession;
				aSession = theSession.getDelegate();
			}
			else
			{
				return aSession.getClass();
			}
		}
		throw new RuntimeException("Invalid session");
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
