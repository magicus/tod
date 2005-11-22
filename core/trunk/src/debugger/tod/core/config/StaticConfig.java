/*
 * Created on Oct 9, 2004
 */
package tod.core.config;

import java.io.IOException;

import reflex.api.hookset.ClassSelector;
import reflex.lib.logging.core.impl.mop.Config;
import reflex.tools.parsers.ParseException;
import reflex.tools.selectors.AllCS;
import reflex.tools.selectors.WorkingSetClassSelector;
import tod.core.ILogCollector;
import tod.core.PrintLogCollector;
import tod.core.transport.SocketCollector;
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
		setLogCollector(new PrintLogCollector());
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
			
			int theCollectorPort = ConfigUtils.readInt(PARAM_COLLECTOR_PORT, 0);
			if (theCollectorPort != 0)
			{
				setLogCollector(new SocketCollector("localhost", theCollectorPort));
			}
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Exception reading StaticConfig", e);
		}
		catch (IOException e)
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
	 * Sets the collector that will receive log events.
	 */
	public void setLogCollector (ILogCollector aCollector)
	{
		checkState();
		Config.COLLECTOR = aCollector;
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
