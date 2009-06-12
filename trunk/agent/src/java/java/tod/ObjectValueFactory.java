/*
 * Created on Dec 14, 2008
 */
package java.tod;

import java.lang.reflect.Field;
import java.tod.util._ArrayList;
import java.tod.util._IdentityHashMap;

import tod2.agent.ObjectValue;
import tod2.agent.ObjectValue.FieldValue;


/**
 * This is part of a trick to avoid loading _IdentityHashMap and similar
 * in non-agent VMs.
 * @author gpothier
 */
public class ObjectValueFactory
{
	/**
	 * Ensures that the specified object graph is portable, converting nodes to {@link ObjectValue}
	 * as needed.
	 */
	public static Object convert(Object aObject)
	{
		if (isPortable(aObject)) return aObject;
		else return convert(aObject, new _IdentityHashMap<Object, ObjectValue>(), 2);
	}
	
	/**
	 * Converts an object to an {@link ObjectValue}, using reflection to obtain field values.
	 */
	private static ObjectValue toObjectValue(Object aObject, _IdentityHashMap<Object, ObjectValue> aMapping, int aDepth)
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
					theMapped = convert(theValue, aMapping, aDepth-1);
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
	
	private static Object convert(Object aObject, _IdentityHashMap<Object, ObjectValue> aMapping, int aDepth)
	{
		if (aDepth == 0) return null;
		
		assert ! aMapping.containsKey(aObject);
		Object theResult;
		
		if (aObject == null) theResult = null;
		else if (isPortable(aObject)) theResult = aObject;
		else 
		{
			ObjectValue theObjectValue = toObjectValue(aObject, aMapping, aDepth);
			aMapping.put(aObject, theObjectValue);
			theResult = theObjectValue;
		}
		
		
		return theResult;
	}

	private static boolean isPortable(Object aObject)
	{
		return (aObject instanceof String) || (aObject instanceof Number) || (aObject instanceof Boolean);
	}

}
