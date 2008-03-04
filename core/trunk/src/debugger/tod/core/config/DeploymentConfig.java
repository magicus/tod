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
package tod.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration options for deployment (native agent version, database version...).
 * Note that deployment options can be overridden by system properties. 
 * @author gpothier
 */
public class DeploymentConfig
{
	public static final String AGENTNAME_PROPERTY = "tod.agent.name";
	public static final String DATABASECLASS_PROPERTY = "tod.db.class";
	
	private static String itsAgentName;
	private static String itsDatabaseClassName;
	
	static
	{
		Properties theProperties = readProperties();
		
		itsAgentName = System.getProperty(
				AGENTNAME_PROPERTY,
				theProperties.getProperty(AGENTNAME_PROPERTY));
		
		itsDatabaseClassName = System.getProperty(
				DATABASECLASS_PROPERTY, 
				theProperties.getProperty(DATABASECLASS_PROPERTY));
	}
	
	public static Properties readProperties()
	{
		try
		{
			InputStream theStream = DeploymentConfig.class.getResourceAsStream("/config.properties");
			Properties theProperties = new Properties();
			theProperties.load(theStream);
			return theProperties;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Returns the name of the native agent to use.
	 */
	public static String getNativeAgentName()
	{
		return itsAgentName;
	}

	/**
	 * Returns the name of the database class.
	 */
	public static String getDatabaseClass()
	{
		return itsDatabaseClassName;
	}
}
