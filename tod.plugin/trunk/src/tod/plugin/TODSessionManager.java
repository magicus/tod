/*
TOD plugin - Eclipse pluging for TOD
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.plugin;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jdt.core.IJavaProject;

import tod.core.config.TODConfig;
import tod.core.session.ISession;
import tod.impl.local.LocalSession;
import zz.utils.properties.IProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;


/**
 * Manages a pool of debugging sessions.
 * @author gpothier
 */
public class TODSessionManager
{
	private static TODSessionManager INSTANCE = new TODSessionManager();
	
	private IRWProperty<DebuggingSession> pCurrentSession = new SimpleRWProperty<DebuggingSession>(this);
	
	public static TODSessionManager getInstance()
	{
		return INSTANCE;
	}
	
	private TODSessionManager()
	{
	}
	
	/**
	 * This propety contains the curent TOD session.
	 */
	public IProperty<DebuggingSession> pCurrentSession()
	{
		return pCurrentSession;
	}

	/**
	 * Obtains a free, clean collector session.
	 */
	public DebuggingSession createSession(IJavaProject aJavaProject)
	{
		ISession theSession;
		try
		{
			DebuggingSession thePreviousSession = pCurrentSession.get();
			if (thePreviousSession != null) thePreviousSession.disconnect();
			
			TODConfig theConfig = new TODConfig();
			theSession = new LocalSession(theConfig, new URI("file:/home/gpothier/tmp/ASM"));
			DebuggingSession theDebuggingSession = new DebuggingSession(theSession, aJavaProject);
			
			pCurrentSession.set(theDebuggingSession);
			return theDebuggingSession;
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
}
