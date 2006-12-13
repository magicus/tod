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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.agent.ConfigUtils;

/**
 * Instances of this class contain configuration options for a TOD session.
 * @author gpothier
 */
public class TODConfig
{
	public static final IntegerItem AGENT_VERBOSE = new IntegerItem(
			"agent.verbose",
			"Agent - verbose",
			"Defines the verbosity level of the native agent.",
			ConfigUtils.readInt("agent-verbose", 0));
	
	public static final BooleanItem AGENT_SKIP_CORE_CLASSE = new BooleanItem(
			"agent.skipCoreClasses",
			"Agent - skip core classes",
			"If true, the agent will not instrument core classes, independently of" +
			"class filter settings.",
			true);
	
	public static final StringItem AGENT_CACHE_PATH = new StringItem(
			"agent.cachePath",
			"Agent - class cache path",
			"Defines the path where the native agent stores instrumented classes.",
			ConfigUtils.readString("classes-cache-path", "/tmp/tod"));
	
	public static final StringItem INSTRUMENTER_LOCATIONS_FILE = new StringItem(
			"instrumenter.locationsFile",
			"Instrumenter - locations file",
			"Defines the file that contains location data for the debugged application.",
			ConfigUtils.readString("locations-file", "/tmp/tod/tod-locations"));
	
	public static final StringItem SCOPE_GLOBAL_FILTER = new StringItem(
			"scope.globalFilter",
			"Scope - global filter",
			"Global class filter for instrumentation. " +
			"Used mainly to shield TOD agent classes from instrumentation. " +
			"Classes that do no pass this filter are not touched by any kind " +
			"of instrumentation and are not registered in the trace database.",
			"[-tod.agent.** -tod.core.**]");
	
	public static final StringItem SCOPE_TRACE_FILTER = new StringItem(
			"scope.traceFilter",
			"Scope - trace filter",
			"Tracing class filter for instrumentation. " +
			"Classes that do no pass this filter are not instrumented " +
			"but are registered in the structure database.",
			ConfigUtils.readString("trace-filter", "[-java.** -javax.** -sun.** -com.sun.**]"));
	
	public static final StringItem COLLECTOR_HOST = new StringItem(
			"collector.host",
			"Collector - host",
			"Host to which the debugged program should send events.",
			"localhost");
	
	public static final IntegerItem COLLECTOR_JAVA_PORT = new IntegerItem(
			"collector.javaPort",
			"Collector - Java port",
			"Port to which the Java portion of the TOD agent should connect.",
			8058);
	
	public static final IntegerItem COLLECTOR_NATIVE_PORT = new IntegerItem(
			"collector.nativePort",
			"Collector - native port",
			"Port to which the native portion of the TOD agent should connect.",
			8059);
	
	
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
	
	private Map<String, String> itsMap = new HashMap<String, String>();
	
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
		private final ItemType<T> itsType;
		private final String itsName;
		private final String itsDescription;
		private final T itsDefault;
		private final String itsKey;
		
		public Item(ItemType<T> aType, String aKey, String aName, String aDescription, T aDefault)
		{
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
	}
	
	public static class BooleanItem extends Item<Boolean>
	{
		public BooleanItem(String aKey, String aName, String aDescription, Boolean aDefault)
		{
			super(ItemType.ITEM_TYPE_BOOLEAN, aKey, aName, aDescription, aDefault);
		}
	}
	
	public static class StringItem extends Item<String>
	{
		public StringItem(String aKey, String aName, String aDescription, String aDefault)
		{
			super(ItemType.ITEM_TYPE_STRING, aKey, aName, aDescription, aDefault);
		}
	}
	
	public static class IntegerItem extends Item<Integer>
	{
		public IntegerItem(String aKey, String aName, String aDescription, Integer aDefault)
		{
			super(ItemType.ITEM_TYPE_INTEGER, aKey, aName, aDescription, aDefault);
		}
	}
}
