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
