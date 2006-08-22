/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid.monitoring;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zz.utils.ListMap;

public class Monitor
{
	private static Monitor INSTANCE = new Monitor();
	private static MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

	public static Monitor getInstance()
	{
		return INSTANCE;
	}

	private Monitor()
	{
//		MonitorUI.showFrame(this);
		register(this); // The monitor provides system-wide information
		Thread thePrinterThread = new Thread()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						print(false);
						sleep(10000);
					}
					catch (InterruptedException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
		};
		thePrinterThread.setDaemon(true);
		thePrinterThread.setPriority(Thread.MAX_PRIORITY);
		thePrinterThread.start();
	}
	
	private List<Object> itsMonitoredObjects = new ArrayList<Object>();

	public synchronized void register(Object aMonitored)
	{
		itsMonitoredObjects.add(aMonitored);
//		System.out.println("Monitor registered: "+aMonitored);
	}
	
	public synchronized void unregister(Object aMonitored)
	{
		itsMonitoredObjects.remove(aMonitored);
	}
	
	/**
	 * Obtains and prints probe data.
	 */
	public void print(boolean aIndividual)
	{
		System.out.println("--- Monitor ---");
		
		System.out.println("heap mem: " + MEMORY_MX_BEAN.getHeapMemoryUsage());
		System.out.println("non-heap mem: " + MEMORY_MX_BEAN.getNonHeapMemoryUsage());

		
		List<KeyMonitorData> theData = getProbeData();
		Collections.sort(theData, new Comparator<KeyMonitorData>()
				{
					public int compare(KeyMonitorData aD1, KeyMonitorData aD2)
					{
						return aD1.key.compareTo(aD2.key);
					}
				});
		
		for (KeyMonitorData theMonitorData : theData)
		{
			System.out.println(theMonitorData.toString(aIndividual));
		}
		System.out.println("---------------");
		System.out.println();
	}
	
	/**
	 * Collects and aggregates all probe data from the registered objects.
	 */
	public List<KeyMonitorData> getProbeData()
	{
		List<KeyMonitorData> theData = new ArrayList<KeyMonitorData>();
		
		ListMap<String, ProbeInstance> theProbeInstances = getProbeInstances();
		for (Map.Entry<String, List<ProbeInstance>> theEntry : theProbeInstances.entrySet())
		{
			Object[] theValues = new Object[theEntry.getValue().size()];
			List<IndividualProbeValue> theIndividualValues = new ArrayList<IndividualProbeValue>();
			
			int i = 0;
			for (ProbeInstance theProbeInstance : theEntry.getValue())
			{
				Object theValue = theProbeInstance.getValue();
				
				theValues[i++] = theValue;
				
				theIndividualValues.add(new IndividualProbeValue(
						theProbeInstance.getInstanceName(),
						theValue));
			}
			
			AggregationType theAggregationType = theEntry.getValue().get(0).getAggregationType(); 
			
			Object theAggregateValue = theAggregationType.aggregate(theValues);
			theData.add(new KeyMonitorData(theEntry.getKey(), theAggregateValue, theIndividualValues));
		}
		
		return theData;
	}
	
	/**
	 * Collects all probe instances in the currently registered objects.
	 * @return
	 */
	private synchronized ListMap<String, ProbeInstance> getProbeInstances()
	{
		ListMap<String, ProbeInstance> theProbeInstances = new ListMap<String, ProbeInstance>();
		Map<String, Class> theValueTypes = new HashMap<String, Class>();
		Map<String, AggregationType> theAggregationTypes = new HashMap<String, AggregationType>();
		
		for (Object theMonitored : itsMonitoredObjects)
		{
			Class<? extends Object> theClass = theMonitored.getClass();
			Method[] theMethods = theClass.getMethods();
			for (Method theMethod : theMethods)
			{
				Probe theAnnotation = theMethod.getAnnotation(Probe.class);
				if (theAnnotation != null) 
				{
					// Check that the method is valid
					if (theMethod.getParameterTypes().length > 0)
					{
						throw new IllegalArgumentException("Probe method should have no argument");
					}
					
					String theKey = theAnnotation.key();
					if (theKey.length() == 0) theKey = theMethod.getName();
					
					ProbeInstance theProbeInstance = new ProbeInstance(theMonitored, theMethod, theAnnotation);
					
					// Check that all probes of a key have the same value type
					Class theRefValueType = theValueTypes.get(theKey);
					Class theValueType = theProbeInstance.getValueType();
					if (theRefValueType == null) theValueTypes.put(theKey, theValueType);
					else if (! theRefValueType.equals(theValueType)) 
					{
						throw new IllegalArgumentException("Incompatible value type for "+theMethod
								+". Expected "+theRefValueType
								+", found "+theValueType);
					}
					
					// Check that all probes of a key have the same aggregation type
					AggregationType theRefAggregationType = theAggregationTypes.get(theKey);
					AggregationType theAggregationType = theAnnotation.aggr();
					if (theRefAggregationType == null) theAggregationTypes.put(theKey, theAggregationType);
					else if (! theRefAggregationType.equals(theAggregationType)) 
					{
						throw new IllegalArgumentException("Incompatible aggregation type for "+theMethod
								+". Expected "+theRefAggregationType
								+", found "+theAggregationType);
					}
					
					theProbeInstances.add(theKey, theProbeInstance);
				}
			}
		}
		
		return theProbeInstances;
	}
	
	
	private static class ProbeInstance
	{
		private Object itsObject;
		private Method itsMethod;
		private Probe itsAnnotation;
		
		public ProbeInstance(Object aObject, Method aMethod, Probe aAnnotation)
		{
			itsObject = aObject;
			itsMethod = aMethod;
			itsAnnotation = aAnnotation;
			itsMethod.setAccessible(true);
		}
		
		/**
		 * Returns the type of this probe.
		 */
		public Class getValueType()
		{
			return itsMethod.getReturnType();
		}
		
		/**
		 * Returns the aggregation type of this probe.
		 */
		public AggregationType getAggregationType()
		{
			return itsAnnotation.aggr();
		}
		
		public Object getValue()
		{
			try
			{
				return itsMethod.invoke(itsObject);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public String getInstanceName()
		{
			return ""+itsObject;
		}
	}
	
	public static class KeyMonitorData
	{
		public final String key;
		public final Object aggregateValue;
		public final List<IndividualProbeValue> individualValues;
		
		public KeyMonitorData(
				String aKey, 
				Object aAggregateValue, 
				List<IndividualProbeValue> aIndividualValues)
		{
			key = aKey;
			aggregateValue = aAggregateValue;
			individualValues = aIndividualValues;
		}
		
		@Override
		public String toString()
		{
			return toString(false);
		}
		
		public String toString(boolean aIndividual)
		{
			StringBuilder theBuilder = new StringBuilder(key);
			theBuilder.append(": ");
			theBuilder.append(format(aggregateValue));
			theBuilder.append(" [");
			theBuilder.append(individualValues.size());
			theBuilder.append("]");
			if (aIndividual)
			{
				theBuilder.append('(');
				for (IndividualProbeValue theValue : individualValues) 
				{
					theBuilder.append(format(theValue));
					theBuilder.append(' ');
				}
				theBuilder.append(')');
			}
			
			return theBuilder.toString();
		}
	}
	
	private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#,###");
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");
	
	private static String format(Object aValue)
	{
		if (aValue instanceof Long)
		{
			Long theLong = (Long) aValue;
			return INTEGER_FORMAT.format(theLong);
		}
		else if (aValue instanceof Double)
		{
			Double theDouble = (Double) aValue;
			return DECIMAL_FORMAT.format(theDouble);
		}
		else return ""+aValue;
	}
	
	public static class IndividualProbeValue
	{
		public final String instanceName;
		public final Object value;
		
		public IndividualProbeValue(String aInstanceName, Object aValue)
		{
			instanceName = aInstanceName;
			value = aValue;
		}
		
		@Override
		public String toString()
		{
			return "'"+instanceName+"' ("+value+")";
		}
	}
}
