/*
 * Created on Dec 14, 2008
 */
package java.tod;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import tod.agent.ObjectValue;
import tod.agent.ObjectValue.FieldValue;

/**
 * This is part of a trick to avoid loading _IdentityHashMap and similar
 * in non-agent VMs.
 * @author gpothier
 */
public class ObjectValueFactory
{
	private static final Set<Class<?>> itsPortableClasses = new HashSet<Class<?>>(Arrays.asList(
			String.class,
			Long.class, Integer.class, Short.class, Character.class, 
			Byte.class, Double.class, Float.class, Boolean.class,
			Throwable.class,
			StackTraceElement.class,
			Exception.class,
			RuntimeException.class));
	

	/**
	 * Converts an object to an {@link ObjectValue}, using reflection to obtain field values.
	 */
	private static ObjectValue convert(Object aObject, _IdentityHashMap<Object, ObjectValue> aMapping)
	{
		Class<?> theClass = aObject.getClass();
		ObjectValue theResult = new ObjectValue(theClass.getName(), aObject instanceof Throwable);
		aMapping.put(aObject, theResult);
		
		_ArrayList<FieldValue> theFieldValues = new _ArrayList<FieldValue>();
		
		while (theClass != null)
		{
			Field[] theFields = theClass.getDeclaredFields();
			for (Field theField : theFields)
			{
				boolean theWasAccessible = theField.isAccessible();
				theField.setAccessible(true);

				Object theValue;
				try
				{
					theValue = theField.get(aObject);
				}
				catch (Exception e)
				{
					theValue = "Cannot obtain field value: "+e.getMessage();
				}
				
				theField.setAccessible(theWasAccessible);
				
				Object theMapped = aMapping.get(theValue);
				if (theMapped == null)
				{
					theMapped = ensurePortable(theValue, aMapping);
					if (theMapped instanceof ObjectValue)
					{
						ObjectValue theObjectValue = (ObjectValue) theMapped;
						aMapping.put(theValue, theObjectValue);
					}
				}
				
				theFieldValues.add(new FieldValue(theField.getName(), theMapped));
			}
			
			theClass = theClass.getSuperclass();
		}
		
		theResult.setFields(theFieldValues.toArray(new FieldValue[theFieldValues.size()]));
		return theResult;
	}
	
	/**
	 * Ensures that the specified object graph is portable, converting it to an {@link ObjectValue}
	 * if necessary.
	 */
	public static Object ensurePortable(Object aObject)
	{
		return ensurePortable(aObject, new _IdentityHashMap<Object, ObjectValue>());
	}
	
	private static Object ensurePortable(Object aObject, _IdentityHashMap<Object, ObjectValue> aMapping)
	{
		assert ! aMapping.containsKey(aObject);
		Object theResult;
		
		if (aObject == null) theResult = null;
		else if (isPortable(aObject)) theResult = aObject;
		else 
		{
			ObjectValue theObjectValue = convert(aObject, aMapping);
			aMapping.put(aObject, theObjectValue);
			theResult = theObjectValue;
		}
		
		
		return theResult;
	}

	/**
	 * Determines if the given object is portable across JVMs (ie, part of the JDK).
	 */
	public static boolean isPortable(Object aObject)
	{
		return itsPortableClasses.contains(aObject.getClass());
	}


}
