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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import tod.core.config.TODConfig;
import tod.impl.local.LocalSessionFactory;

/**
 * Manages the different session types available.
 * @author gpothier
 */
public class SessionTypeManager
{
	private static SessionTypeManager INSTANCE = new SessionTypeManager();

	public static SessionTypeManager getInstance()
	{
		return INSTANCE;
	}

	private SessionTypeManager()
	{
		// Register known session types.
		registerType("tod-dbgrid-remote", "tod.impl.dbgrid.RemoteGridSessionFactory");
		registerType("tod-dbgrid-local", "tod.impl.dbgrid.LocalGridSessionFactory");
		registerType("tod-memory", LocalSessionFactory.class.getName());
	}
	
	private final Map<String, SessionType> itsSchemaMap = 
		new HashMap<String, SessionType>();
	
	/**
	 * Registers a session type.
	 * @param aSchema The URL schema of the type.
	 * @param aClassName The class that implements the type.
	 */
	public void registerType(String aSchema, String aClassName)
	{
		SessionType theSessionType = new SessionType(aSchema, aClassName);
		itsSchemaMap.put(aSchema, theSessionType);
	}
	
	/**
	 * Returns the {@link ISession} subclass that handles the given schema. 
	 */
	public ISession createSession(String aSchema, URI aUri, TODConfig aConfig)
	{
		try
		{
			System.out.println(String.format("Creating session [%s:%s]", aSchema, aUri));
			SessionType theSessionType = itsSchemaMap.get(aSchema);
			ISessionFactory theFactory = theSessionType.getFactory();
			return theFactory.create(aUri, aConfig);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static class SessionType
	{
		/**
		 * Name of the class that implements the session type
		 */
		public final String factoryClassName;
		
		/**
		 * URL schema for the session type.
		 */
		public final String schema;

		public SessionType(String aSchema, String aFactoryClassName)
		{
			schema = aSchema;
			factoryClassName = aFactoryClassName;
		}
		
		public ISessionFactory getFactory()
		{
			try
			{
				Class<ISessionFactory> theClass = (Class) Class.forName(factoryClassName);
				return theClass.newInstance();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
