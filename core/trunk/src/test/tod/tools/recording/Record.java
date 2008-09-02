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
package tod.tools.recording;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import zz.utils.Utils;

/**
 * An history record of database access.
 * @author gpothier
 */
public class Record implements Serializable
{
	private static final long serialVersionUID = 2484097571786422998L;
	
	private int itsThreadId;
	private ProxyObject itsTarget;
	private MethodSignature itsMethod;
	private Object[] itsArgs;
	private Object itsResult;
	
	public Record(
			int aThreadId,
			ProxyObject aTarget,
			MethodSignature aMethod,
			Object[] aArgs,
			Object aResult)
	{
		itsThreadId = aThreadId;
		itsTarget = aTarget;
		itsMethod = aMethod;
		itsArgs = aArgs;
		itsResult = aResult;
	}

	private Object resolve(final Map<Integer, Object> aObjectsMap, Object aObject)
	{
		return map(aObject, new IMapper()
		{
			public Object map(Object aSource)
			{
				if (aSource instanceof ProxyObject)
				{
					ProxyObject theProxy = (ProxyObject) aSource;
					return theProxy.resolve(aObjectsMap);
				}
				else return aSource;
			}
		});
	}
	
	/**
	 * "Fixes" the argument so that arrays are of the proper type.
	 */
	private Object fixArg(Object aArg, Class aExpectedType)
	{
		if (aExpectedType.isArray())
		{
			int theLength = Array.getLength(aArg);
			Object theResult = Array.newInstance(aExpectedType.getComponentType(), theLength);
			for(int i=0;i<theLength;i++) Array.set(theResult, i, Array.get(aArg, i));
			return theResult;
		}
		else return aArg;
	}
	
	private Object[] fixArgs(Object[] aArgs, Method aMethod)
	{
		Object[] theResult = new Object[aArgs.length];
		Class<?>[] theParameterTypes = aMethod.getParameterTypes();
		
		for (int i=0;i<theResult.length;i++)
		{
			theResult[i] = fixArg(aArgs[i], theParameterTypes[i]); 
		}
		
		return theResult;
	}
	
	public void process(Map<Integer, Object> aObjectsMap) throws Exception
	{
		Object theTarget = resolve(aObjectsMap, itsTarget);
		Class<?> theClass = theTarget.getClass();
		Method theMethod = itsMethod.findMethod(theClass);
		Object theResult = null;
		try
		{
			theMethod.setAccessible(true);
			theResult = theMethod.invoke(
					theTarget, 
					fixArgs((Object[]) resolve(aObjectsMap, itsArgs), theMethod));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		registerResult(aObjectsMap, itsResult, theResult);
	}
	
	private void registerResult(
			Map<Integer, Object> aObjectsMap, 
			Object aFormalResult, 
			Object aActualResult)
	{
		if (aFormalResult == null) return;
		else if (aFormalResult instanceof ProxyObject)
		{
			ProxyObject theProxy = (ProxyObject) aFormalResult;
			aObjectsMap.put(theProxy.getId(), aActualResult);
		}
		else if (aFormalResult.getClass().getComponentType() != null
				&& ! aFormalResult.getClass().getComponentType().isPrimitive())
		{
			int theLength = Array.getLength(aFormalResult);
			Object[] theActualArray = toArray(aActualResult);
			for(int i=0;i<theLength;i++)
			{
				registerResult(
						aObjectsMap, 
						Array.get(aFormalResult, i), 
						Array.get(theActualArray, i));
			}
		}
	}
	
	private Object[] toArray(Object aObject)
	{
		if (aObject == null) return null;
		else if (aObject.getClass().isArray()) return (Object[]) aObject;
		else if (aObject instanceof Iterable)
		{
			Iterable theIterable = (Iterable) aObject;
			List theList = new ArrayList();
			Utils.fillCollection(theList, theIterable);
			return theList.toArray();
		}
		else throw new RuntimeException("not handled: "+aObject);
	}
	
	private String format(Object aObject)
	{
		if (aObject == null) return "null";
		else if (aObject.getClass().isArray())
		{
			StringBuilder theBuilder = new StringBuilder("[");
			int theLength = Array.getLength(aObject);
			for(int i=0;i<theLength;i++)
			{
				theBuilder.append(format(Array.get(aObject, i)));
				theBuilder.append(", ");
			}
			
			return theBuilder.toString();
		}
		else return ""+aObject;
	}
	
	@Override
	public String toString()
	{
		return String.format(
				"Record [th: %d, tgt: %d, m: %s, args: %s] -> %s",
				itsThreadId,
				itsTarget.getId(), 
				itsMethod,
				Arrays.asList(itsArgs),
				format(itsResult));
	}
	
	public Object map(Object aSource, IMapper aMapper)
	{
		if (aSource == null) return null;
		else if (aSource.getClass().isArray())
		{
			if (aSource.getClass().getComponentType().isPrimitive()) return aSource;
			else
			{
				int theLength = Array.getLength(aSource);
				Object theArray = Array.newInstance(aSource.getClass().getComponentType(), theLength);
				for(int i=0;i<theLength;i++)
				{
					Array.set(theArray, i, map(Array.get(aSource, i), aMapper));
				}
				
				return theArray;
			}
		}
		else return aMapper.map(aSource);
	}
	
	public interface IMapper
	{
		public Object map(Object aSource);
	}
	
	/**
	 * An object that is not yet known but that should be
	 * known by the time it is used.
	 * @author gpothier
	 */
	public static class ProxyObject implements Serializable
	{
		private static final long serialVersionUID = 558471669354712358L;
		private int itsId;
		
		public ProxyObject(int aId)
		{
			itsId = aId;
		}

		public Object resolve(Map<Integer, Object> aObjectsMap)
		{
			Object theObject = aObjectsMap.get(itsId);
			assert theObject != null : "No object for id "+itsId;
			return theObject;
		}
		
		public int getId()
		{
			return itsId;
		}
		
		@Override
		public String toString()
		{
			return "Proxy ["+itsId+"]";
		}
	}

	public static class MethodSignature implements Serializable
	{
		private static final long serialVersionUID = 1500258777457112475L;
		private String itsMethodName;
		private String[] itsArgTypeNames;
		
		public MethodSignature(String aMethodName, Class[] aArgTypes)
		{
			itsMethodName = aMethodName;
			itsArgTypeNames = new String[aArgTypes.length];
			for(int i=0;i<itsArgTypeNames.length;i++)
			{
				itsArgTypeNames[i] = aArgTypes[i].getName();
			}
			
		}
		
		public MethodSignature(String aMethodName, String[] aArgTypes)
		{
			itsMethodName = aMethodName;
			itsArgTypeNames = aArgTypes;
		}
		
		public Method findMethod(Class aClass) throws Exception
		{
			Class[] theArgClasses = new Class[itsArgTypeNames.length];
			for(int i=0;i<theArgClasses.length;i++)
			{
				theArgClasses[i] = forName(itsArgTypeNames[i]);
			}
			
			return aClass.getMethod(itsMethodName, theArgClasses);
		}
		
		private Class forName(String aName) throws ClassNotFoundException
		{
			if ("long".equals(aName)) return long.class;
			else if ("int".equals(aName)) return int.class;
			else if ("boolean".equals(aName)) return boolean.class;
			else return Class.forName(aName);
		}
		
		@Override
		public String toString()
		{
			return ""+itsMethodName+Arrays.asList(itsArgTypeNames);
		}
	}
}