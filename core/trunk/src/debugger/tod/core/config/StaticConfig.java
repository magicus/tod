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
package tod.core.config;

import tod.core.config.ClassSelector.AllCS;
import tod.core.config.ClassSelector.WorkingSetClassSelector;
import tod.tools.parsers.ParseException;
import tod.utils.ConfigUtils;

/**
 * Permits to define the static configuration of the logging
 * system. This configuration must be set up before the logging
 * weaving starts. 
 * <p>
 * Lets the user select which packages/classes should be subject to
 * logging, and which kind of events can be logged. Classes and
 * events excluded from the static config cannot be activated 
 * at runtime; however, classes and events included in the static
 * config can be activated and deactivated at any moment at runtime.
 * @author gpothier
 */
public class StaticConfig
{
	private static StaticConfig INSTANCE = new StaticConfig();

	public static StaticConfig getInstance()
	{
		return INSTANCE;
	}

	private StaticConfig()
	{
	}
	
	/**
	 * Indicates if it is still possible to change the static 
	 * config.
	 */
	private boolean itsFrozen = false;
	
	public static final String PARAM_LOG_METHODS = "log-methods";
	private boolean itsLogMethods = true;
	
	public static final String PARAM_LOG_INSTANTIATIONS = "log-instantiations";	
	private boolean itsLogInstantiations = true;
	
	public static final String PARAM_LOG_FIELDWRITE = "log-fieldwrite";
	private boolean itsLogFieldWrite = true;

	public static final String PARAM_LOG_LOCALVARIABLEWRITE = "log-localvariablewrite";
	private boolean itsLogLocalVariableWrite = true;
	
	public static final String PARAM_LOG_PARAMETERS = "log-parameters";
	private boolean itsLogParameters = true;
	
	public static final String PARAM_COLLECTOR_PORT = "collector-port";
	
	public static final String PARAM_LOGGING_WORKINGSET = "logging-workingset";
	public static final String PARAM_IDENTIFICATION_WORKINGSET = "identification-workingset";

	private ClassSelector itsLoggingClassSelector = AllCS.getInstance();
	private ClassSelector itsIdentificationClassSelector = AllCS.getInstance();
	
	/**
	 * Reads static config from system properties.
	 */
	public void readConfig()
	{
		checkState();
		try
		{
			itsLogFieldWrite = ConfigUtils.readBoolean(PARAM_LOG_FIELDWRITE, itsLogFieldWrite);
			itsLogInstantiations = ConfigUtils.readBoolean(PARAM_LOG_INSTANTIATIONS, itsLogInstantiations);
			itsLogMethods = ConfigUtils.readBoolean(PARAM_LOG_METHODS, itsLogMethods);
			itsLogParameters = ConfigUtils.readBoolean(PARAM_LOG_PARAMETERS, itsLogParameters);
			
			String theLoggingWorkingSet = ConfigUtils.readString(PARAM_LOGGING_WORKINGSET, null);
			if (theLoggingWorkingSet != null)
				itsLoggingClassSelector = new WorkingSetClassSelector(theLoggingWorkingSet);
			
			String theIdentificationWorkingSet = ConfigUtils.readString(PARAM_IDENTIFICATION_WORKINGSET, null);
			if (theIdentificationWorkingSet != null)
				itsIdentificationClassSelector = new WorkingSetClassSelector(theIdentificationWorkingSet);
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Exception reading StaticConfig", e);
		}
	}
	
	public void checkState()
	{
		if (itsFrozen) throw new IllegalStateException("Cannot make changes to static config after weaving");
	}
	
	/**
	 * Indicates if field write access should be logged.
	 */
	public boolean getLogFieldWrite()
	{
		return itsLogFieldWrite;
	}

	/**
	 * Indicates if field write access should be logged.
	 */
	public void setLogFieldWrite(boolean aLogFieldWrite)
	{
		checkState();
		itsLogFieldWrite = aLogFieldWrite;
	}
	
	/**
	 * Indicates if local variables access should be logged.
	 */
	public boolean getLogLocalVariableWrite()
	{
		return itsLogLocalVariableWrite;
	}

	/**
	 * Indicates if local variables access should be logged.
	 */
	public void setLogLocalVariableWrite(boolean aLogLocalVariableWrite)
	{
		itsLogLocalVariableWrite = aLogLocalVariableWrite;
	}

	/**
	 * Indicates if method enter/exit events should be
	 * logged. 
	 */
	public boolean getLogMethods()
	{
		return itsLogMethods;
	}

	/**
	 * Indicates if method enter/exit events should be
	 * logged. 
	 */
	public void setLogMethods(boolean aLogMethods)
	{
		checkState();
		itsLogMethods = aLogMethods;
	}

	/**
	 * Indicates if instantiations should be logged.
	 */
	public boolean getLogInstantiations()
	{
		return itsLogInstantiations;
	}
	
	/**
	 * Indicates if instantiations should be logged.
	 */
	public void setLogInstantiations(boolean aLogInstantiations)
	{
		checkState();
		itsLogInstantiations = aLogInstantiations;
	}

	/**
	 * Idicates if method and constructor parameters
	 * should be logged.
	 */
	public boolean getLogParameters()
	{
		return itsLogParameters;
	}
	
	/**
	 * Idicates if method and constructor parameters
	 * should be logged.
	 */
	public void setLogParameters(boolean aLogParameters)
	{
		itsLogParameters = aLogParameters;
	}
	
	/**
	 * Returns the class selector that filters the classes
	 * that must be identifiable.
	 */
	public ClassSelector getIdentificationClassSelector()
	{
		return itsIdentificationClassSelector;
	}

	/**
	 * Sets the class selector that filters the classes
	 * that must be identifiable.
	 */
	public void setIdentificationClassSelector(ClassSelector aIdentificationClassSelector)
	{
		itsIdentificationClassSelector = aIdentificationClassSelector;
	}

	/**
	 * Returns the class selector that filters the classes
	 * that must be logged.
	 */
	public ClassSelector getLoggingClassSelector()
	{
		return itsLoggingClassSelector;
	}

	/**
	 * Sets the class selector that filters the classes
	 * that must be logged.
	 */
	public void setLoggingClassSelector(ClassSelector aLoggingClassSelector)
	{
		itsLoggingClassSelector = aLoggingClassSelector;
	}

	/**
	 * Not part of the API.
	 * Prevent further changes to the static config.
	 */
	public void freeze ()
	{
		itsFrozen = true;
	}
	
}
