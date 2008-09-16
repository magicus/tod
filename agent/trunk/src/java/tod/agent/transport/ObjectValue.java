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
package tod.agent.transport;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the value of some object. It is always possible to
 * deserialize an {@link ObjectValue} in the database's or client's JVM,
 * whereas it would not necessarily be possible to deserialize the actual object
 * (for classes that are not part of the JDK).  
 * @author gpothier
 */
public class ObjectValue implements Serializable
{
	private static final long serialVersionUID = 2201231697046128985L;

	private String itsClassName;
	private FieldValue[] itsFields;
	private boolean itsThrowable;
	
	private static final Set<Class<?>> itsPortableClasses = new HashSet<Class<?>>(Arrays.asList(
			String.class,
			Long.class, Integer.class, Short.class, Character.class, 
			Byte.class, Double.class, Float.class, Boolean.class,
			Throwable.class,
			StackTraceElement.class,
			Exception.class,
			RuntimeException.class));
	
	private ObjectValue(String aClassName, boolean aThrowable)
	{
		itsClassName = aClassName;
		itsThrowable = aThrowable;
	}
	
	public FieldValue[] getFields()
	{
		return itsFields;
	}

	private void setFields(FieldValue[] aFields)
	{
		itsFields = aFields;
	}

	public String getClassName()
	{
		return itsClassName;
	}

	/**
	 * Whether the represented object is a {@link Throwable}.
	 */
	public boolean isThrowable()
	{
		return itsThrowable;
	}
	
	/**
	 * Returns the value for the (first encountered match of the) given field. 
	 */
	public Object getFieldValue(String aFieldName)
	{
		for (FieldValue theValue : itsFields)
		{
			if (theValue.fieldName.equals(aFieldName)) return theValue.value;
		}
		return null;
	}

	/**
	 * Returns a user-readable representation of the object.
	 */
	public String asString()
	{
		return asString(1);
	}
	
	public String asString(int aLevel)
	{
		if (aLevel == 0) return "";
		StringBuilder theBuilder = new StringBuilder();
		for (FieldValue theFieldValue : itsFields)
		{
			theBuilder.append(theFieldValue.asString(aLevel-1));
			theBuilder.append(' ');
		}
		return theBuilder.toString();
	}
	
	@Override
	public String toString()
	{
		return "ObjectValue ["+asString()+"]";
	}
	
	private static class FieldValue implements Serializable
	{
		private static final long serialVersionUID = -1201541697676890987L;		
		
		public final String fieldName;
		public final Object value;

		public FieldValue(String aFieldName, Object aValue)
		{
			fieldName = aFieldName;
			value = aValue;
		}
		
		public String asString(int aLevel)
		{
			String theValueString;
			if (value instanceof ObjectValue)
			{
				ObjectValue theObjectValue = (ObjectValue) value;
				theValueString = theObjectValue.asString(aLevel);
			}
			else theValueString = ""+value;
			
			return fieldName+"='"+theValueString+"'";
		}
	}

	/**
	 * Converts an object to an {@link ObjectValue}, using reflection to obtain field values.
	 */
	private static ObjectValue convert(Object aObject, Map<Object, ObjectValue> aMapping)
	{
		Class<?> theClass = aObject.getClass();
		ObjectValue theResult = new ObjectValue(theClass.getName(), aObject instanceof Throwable);
		aMapping.put(aObject, theResult);
		
		List<FieldValue> theFieldValues = new ArrayList<FieldValue>();
		
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
		return ensurePortable(aObject, new IdentityHashMap<Object, ObjectValue>());
	}
	
	private static Object ensurePortable(Object aObject, Map<Object, ObjectValue> aMapping)
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
