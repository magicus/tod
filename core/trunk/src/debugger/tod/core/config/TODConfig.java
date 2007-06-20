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
package tod.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import tod.agent.AgentConfig;
import tod.agent.ConfigUtils;
import zz.utils.PublicCloneable;

/**
 * Instances of this class contain configuration options for a TOD session.
 * @author gpothier
 */
public class TODConfig extends PublicCloneable implements Serializable
{
	private static final long serialVersionUID = 4959079097346687404L;
	
	private static final String HOME = System.getProperty("user.home");

	/**
	 * Defines levels of "detail" for configuration options.
	 * @author gpothier
	 */
	public static enum ConfigLevel
	{
		NORMAL(1), ADVANCED(2), DEBUG(3), NEVER(Integer.MAX_VALUE);
		
		private int itsValue;

		private ConfigLevel(int aValue)
		{
			itsValue = aValue;
		}

		/**
		 * Whether this level also includes the specified level.
		 * eg. {@link #DEBUG} includes {@link #NORMAL} but
		 * the opposite is false.
		 */
		public boolean accept(ConfigLevel aLevel)
		{
			return aLevel.itsValue <= itsValue;
		}
	}
	
	public static final IntegerItem AGENT_VERBOSE = new IntegerItem(
			ConfigLevel.ADVANCED,
			"agent-verbose",
			"Agent - verbose",
			"Defines the verbosity level of the native agent. " +
			"0 means minimal verbosity, greater values increase verbosity.",
			0);
	
	public static final BooleanItem AGENT_SKIP_CORE_CLASSE = new BooleanItem(
			ConfigLevel.DEBUG,
			"agent-skipCoreClasses",
			"Agent - skip core classes",
			"If true, the agent will not instrument core classes, independently of " +
			"class filter settings.",
			true);
	
	public static final StringItem AGENT_CACHE_PATH = new StringItem(
			ConfigLevel.NORMAL,
			"agent-cache-path",
			"Agent - class cache path",
			"Defines the path where the native agent stores instrumented classes.",
			HOME+File.separatorChar+"tmp"+File.separatorChar+"tod");
	
	public static final BooleanItem AGENT_CAPTURE_EXCEPTIONS = new BooleanItem(
			ConfigLevel.DEBUG,
			"agent-captureExceptions",
			"Agent - capture exceptions",
			"If true, the native agent sets up a callback that captures " +
			"exceptions.",
			true);
	
	public static final StringItem INSTRUMENTER_LOCATIONS_FILE = new StringItem(
			ConfigLevel.NORMAL,
			"locations-file",
			"Instrumenter - locations file",
			"Defines the file that contains location data for the debugged application.",
			HOME+"/tmp/tod/locations.bin");
	
	public static final StringItem SCOPE_GLOBAL_FILTER = new StringItem(
			ConfigLevel.DEBUG,
			"scope-globalFilter",
			"Scope - global filter",
			"Global class filter for instrumentation. " +
			"Used mainly to shield TOD agent classes from instrumentation. " +
			"Classes that do no pass this filter are not touched by any kind " +
			"of instrumentation and are not registered in the trace database. " +
			"There should not be any reason to modify it.",
			"[-tod.agent.** -tod.core.**]");
	
	public static final StringItem SCOPE_TRACE_FILTER = new StringItem(
			ConfigLevel.NORMAL,
			"trace-filter",
			"Scope - trace filter",
			"Tracing class filter for instrumentation. " +
			"Classes that do no pass this filter are not instrumented " +
			"but are registered in the structure database.",
			"[-java.** -javax.** -sun.** -com.sun.** -org.ietf.jgss.** -org.omg.** -org.w3c.** -org.xml.**]");
	
	public static final StringItem CLIENT_HOST_NAME = new StringItem(
			ConfigLevel.NORMAL,
			AgentConfig.PARAM_HOST,
			"Client - host name",
			"Host name given to the debugged program's JVM.",
			"tod-1");
	
	public static final StringItem COLLECTOR_HOST = new StringItem(
			ConfigLevel.DEBUG,
			"collector-host",
			"Collector - host",
			"Host to which the debugged program should send events.",
			"localhost");
	
	public static final IntegerItem COLLECTOR_JAVA_PORT = new IntegerItem(
			ConfigLevel.DEBUG,
			"collector-javaPort",
			"Collector - Java port",
			"Port to which the Java portion of the TOD agent should connect.",
			8058);
	
	public static final IntegerItem COLLECTOR_NATIVE_PORT = new IntegerItem(
			ConfigLevel.DEBUG,
			"collector-nativePort",
			"Collector - native port",
			"Port to which the native portion of the TOD agent should connect.",
			8059);
	
	public static final String GRID_IMPL_UNIFORM = "uniform";
	public static final String GRID_IMPL_GROUPED_INDEXES = "grpIdx";
	
	public static final StringItem GRID_IMPLEMENTATION = new StringItem(
			ConfigLevel.ADVANCED,
			"grid-impl",
			"Grid implementation",
			"Specifies the type of grid implementation. One of:\n" +
			" - "+GRID_IMPL_UNIFORM+": each index can be split across all nodes. " +
					"Memory requirements do not depend on the number of nodes.\n" +
			" - "+GRID_IMPL_GROUPED_INDEXES+": indexes are not split across " +
					"nodes, which reduces the memory requirements as more nodes " +
					"are available.",
			ConfigUtils.readString("grid-impl", GRID_IMPL_UNIFORM));
	
	public static final String SESSION_MEMORY = "memory";
	public static final String SESSION_LOCAL = "local";
	public static final String SESSION_REMOTE = "remote";
	
	public static final StringItem SESSION_TYPE = new StringItem(
			ConfigLevel.NORMAL,
			"session-type",
			"Session type",
			"Specifies the type of database to use for the debugging " +
			"session. One of:\n" +
			" - "+SESSION_MEMORY+": Events are stored in memory, " +
					"in the Eclipse process. " +
					"This is the less scalable option. The maximum number " +
					"of events depends on the amount of heap memory allocated " +
					"the the JVM that runs Eclipse.\n" +
			" - "+SESSION_LOCAL+": Events are stored on the hard disk. " +
					"This option provides good scalability but " +
					"performance may be a problem for large traces.\n" +
			" - "+SESSION_REMOTE+": Events are stored in a dedicated " +
					"distributed database. " +
					"This option provides good scalability and performance " +
					"(depending on the size of the database cluster). " +
					"The database cluster must be set up and the " +
					"'Collector host' option must indicate the name of " +
					"the grid master.\n",
			ConfigUtils.readString("session-type", SESSION_LOCAL));
	
	public static final SizeItem LOCAL_SESSION_HEAP = new SizeItem(
			ConfigLevel.NORMAL,
			"localSessionHeap",
			"Local session heap size",
			"Specifies the amount of heap memory to allocate for local debugging session.",
			"256m");
	
	public static final BooleanItem INDEX_STRINGS = new BooleanItem(
			ConfigLevel.NORMAL,
			"index-strings",
			"Index strings",
			"Whether strings should be indexed by the database. " +
			"This has an impact on overall recording performance.",
			false);
	
	public static final IntegerItem MASTER_TIMEOUT = new IntegerItem(
			ConfigLevel.ADVANCED,
			"master-timeout",
			"Master timeout",
			"The time (in seconds) the database should wait for clients to connect" +
			"before exiting. A value of 0 means no timeout.",
			0);
	
	/**
	 * Contains all available configuration items.
	 */
	public static final Item[] ITEMS = getItems();
	
	private static Item[] getItems()
	{
		try
		{
			List<Item> theItems = new ArrayList<Item>();
			Field[] theFields = TODConfig.class.getDeclaredFields();
			
			for (Field theField : theFields)
			{
				if (Item.class.isAssignableFrom(theField.getType()))
				{
					Item theItem = (Item) theField.get(null);
					theItems.add(theItem);
				}
			}
			
			return theItems.toArray(new Item[theItems.size()]);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private HashMap<String, String> itsMap = new HashMap<String, String>();
	
	/**
	 * Sets the value for an option item
	 */
	public <T> void set(Item<T> aItem, T aValue)
	{
		itsMap.put(aItem.getKey(), aItem.getOptionString(aValue));
	}

	/**
	 * Retrieves the value for an option item. 
	 */
	public <T> T get(Item<T> aItem)
	{
		String theString = itsMap.get(aItem.getKey());
		return theString != null ?
				aItem.getOptionValue(theString)
				: aItem.getDefault();
	}

	@Override
	public TODConfig clone()
	{
		TODConfig theClone = (TODConfig) super.clone();
		theClone.itsMap = (HashMap<String, String>) itsMap.clone();
		return theClone;
	}
	
	public static abstract class ItemType<T>
	{
		public static final ItemType<String> ITEM_TYPE_STRING = new ItemType<String>()
		{
			@Override
			public String getName()
			{
				return "string";
			}

			@Override
			public String getString(String aValue)
			{
				return aValue;
			}

			@Override
			public String getValue(String aString)
			{
				return aString;
			}
		};
		
		public static final ItemType<Integer> ITEM_TYPE_INTEGER = new ItemType<Integer>()
		{
			@Override
			public String getName()
			{
				return "integer";
			}
			
			@Override
			public String getString(Integer aValue)
			{
				return Integer.toString(aValue);
			}

			@Override
			public Integer getValue(String aString)
			{
				return Integer.parseInt(aString);
			}
		};
		
		public static final ItemType<Boolean> ITEM_TYPE_BOOLEAN = new ItemType<Boolean>()
		{
			@Override
			public String getName()
			{
				return "boolean";
			}
			
			@Override
			public String getString(Boolean aValue)
			{
				return Boolean.toString(aValue);
			}

			@Override
			public Boolean getValue(String aString)
			{
				return Boolean.parseBoolean(aString);
			}
		};
		
		public static final ItemType<Long> ITEM_TYPE_SIZE = new ItemType<Long>()
		{
			@Override
			public String getName()
			{
				return "boolean";
			}
			
			@Override
			public String getString(Long aValue)
			{
				return Long.toString(aValue);
			}

			@Override
			public Long getValue(String aString)
			{
				return ConfigUtils.readSize(aString);
			}
		};

		
		public abstract String getName();
		
		/**
		 * Transforms an option value to a string.
		 */
		public abstract String getString(T aValue);
		
		/**
		 * Transforms a string to an option value
		 */
		public abstract T getValue(String aString);

	}
	
	public static class Item<T>
	{
		private final ConfigLevel itsLevel;
		private final ItemType<T> itsType;
		private final String itsName;
		private final String itsDescription;
		private final T itsDefault;
		private final String itsKey;
		
		public Item(
				ConfigLevel aLevel, 
				ItemType<T> aType, 
				String aKey, 
				String aName, 
				String aDescription, 
				T aDefault)
		{
			itsLevel = aLevel;
			itsType = aType;
			itsKey = aKey;
			itsName = aName;
			itsDescription = aDescription;
			itsDefault = aDefault;
		}

		public ItemType<T> getType()
		{
			return itsType;
		}

		public String getKey()
		{
			return itsKey;
		}

		public String getName()
		{
			return itsName;
		}

		public String getDescription()
		{
			return itsDescription;
		}

		public T getDefault()
		{
			return itsDefault;
		}

		/**
		 * Transforms an option value to a string.
		 */
		public String getOptionString(T aValue)
		{
			return itsType.getString(aValue);
		}
		
		/**
		 * Transforms a string to an option value
		 */
		public T getOptionValue(String aString)
		{
			return itsType.getValue(aString);
		}
		
		/**
		 * Returns a java option string representing the specified value.
		 * Eg.: -Dxxx=yyy
		 */
		public String javaOpt(T aValue)
		{
			return "-D"+getKey()+"="+getOptionString(aValue);
		}
		
		/**
		 * Creates a java option representing the value of this item in
		 * the specified config.
		 * Eg.: -Dxxx=yyy
		 */
		public String javaOpt(TODConfig aConfig)
		{
			return javaOpt(aConfig.get(this));
		}

		
	}
	
	public static class BooleanItem extends Item<Boolean>
	{
		public BooleanItem(
				ConfigLevel aLevel,
				String aKey, 
				String aName, 
				String aDescription, 
				Boolean aDefault)
		{
			super(
					aLevel, 
					ItemType.ITEM_TYPE_BOOLEAN, 
					aKey, 
					aName, 
					aDescription,
					ConfigUtils.readBoolean(aKey, aDefault));
		}
	}
	
	public static class StringItem extends Item<String>
	{
		public StringItem(
				ConfigLevel aLevel, 
				String aKey,
				String aName, 
				String aDescription, 
				String aDefault)
		{
			super(
					aLevel, 
					ItemType.ITEM_TYPE_STRING,
					aKey, 
					aName,
					aDescription,
					ConfigUtils.readString(aKey, aDefault));
		}
	}
	
	public static class IntegerItem extends Item<Integer>
	{
		public IntegerItem(
				ConfigLevel aLevel, 
				String aKey, 
				String aName, 
				String aDescription, 
				Integer aDefault)
		{
			super(
					aLevel, 
					ItemType.ITEM_TYPE_INTEGER, 
					aKey, 
					aName, 
					aDescription, 
					ConfigUtils.readInt(aKey, aDefault));
		}
	}
	
	public static class SizeItem extends Item<Long>
	{
		public SizeItem(
				ConfigLevel aLevel, 
				String aKey, 
				String aName, 
				String aDescription, 
				String aDefault)
		{
			super(
					aLevel,
					ItemType.ITEM_TYPE_SIZE, 
					aKey, 
					aName, 
					aDescription, 
					ConfigUtils.readSize(aKey, aDefault));
		}
	}
	
	/**
	 * Reads a {@link TODConfig} from a properties file.
	 */
	public static TODConfig fromProperties(File aFile)
	{
		try
		{
			Properties theProperties = new Properties();
			theProperties.load(new FileInputStream(aFile));
			return fromProperties(theProperties);
		}
		catch (FileNotFoundException e)
		{
			System.err.println("File not found: "+aFile+", using default configuration.");
			return new TODConfig();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads a {@link TODConfig} from a properties file.
	 */
	public static TODConfig fromProperties(Properties aProperties)
	{
		TODConfig theConfig = new TODConfig();
		for(Item theItem : ITEMS)
		{
			String theValue = aProperties.getProperty(theItem.getKey());
			if (theValue != null) theConfig.set(theItem, theItem.getOptionValue(theValue));
		}
		return theConfig;
	}
}
