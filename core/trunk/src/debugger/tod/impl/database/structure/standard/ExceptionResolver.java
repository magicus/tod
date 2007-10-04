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
package tod.impl.database.structure.standard;

import java.util.HashMap;
import java.util.Map;

import tod.core.database.structure.IExceptionResolver;

/**
 * Standard implementation of {@link IExceptionResolver}.
 * @author gpothier
 */
public class ExceptionResolver implements IExceptionResolver
{
	private final Map<String, ClassInfo> itsClasses = new HashMap<String, ClassInfo>();
	
	public int getBehaviorId(String aClassName, String aMethodName, String aMethodSignature)
	{
		ClassInfo theClassInfo = getClassInfo(aClassName);
		if (theClassInfo == null) 
		{
			System.err.println("[ExceptionResolver] Behavior not found: "+aClassName+"."+aMethodName+"("+aMethodSignature+")");
			return 0;
		}
		
		return theClassInfo.getBehavior(aMethodName, aMethodSignature, true);
	}

	protected ClassInfo getClassInfo(String aClassName)
	{
		return itsClasses.get(aClassName);
	}

	/**
	 * Registers the specified behavior in this database.
	 */
	public void registerBehavior(String aClassName, String aMethodName, String aMethodSignature, int aId)
	{
		ClassInfo theClassInfo = getClassInfo(aClassName);
		if (theClassInfo == null)
		{
			theClassInfo = new ClassInfo(aClassName);
			itsClasses.put(aClassName, theClassInfo);
		}
		
		theClassInfo.registerBehavior(aMethodName, aMethodSignature, aId);
	}
	
	public void registerBehavior(BehaviorInfo aBehaviorInfo)
	{
		registerBehavior(aBehaviorInfo.className, aBehaviorInfo.behaviorName, aBehaviorInfo.behaviorSignature, aBehaviorInfo.id);
	}
	
	public void registerBehaviors(BehaviorInfo[] aBehaviorInfos)
	{
		for (BehaviorInfo theBehaviorInfo : aBehaviorInfos)
		{
			registerBehavior(theBehaviorInfo);
		}
	}
	
	protected static class ClassInfo
	{
		private final String itsClassName;
		private Map<String, Integer> itsBehaviorMap = new HashMap<String, Integer>();
		
		public ClassInfo(String aClassName)
		{
			itsClassName = aClassName;
		}

		public void registerBehavior(String aMethodName, String aMethodSignature, int aId)
		{
			itsBehaviorMap.put(getKey(aMethodName, aMethodSignature), aId);
		}
		
		public int getBehavior(String aMethodName, String aMethodSignature, boolean aFailIfAbsent)
		{
			Integer theId = itsBehaviorMap.get(getKey(aMethodName, aMethodSignature));
			if (theId == null && aFailIfAbsent) throw new RuntimeException("Behavior not found in class: "+itsClassName+"."+aMethodName+"("+aMethodSignature+")");

			return theId != null ? theId : 0;
		}
		
		public static String getKey(String aMethodName, String aMethodSignature)
		{
			return aMethodName+"|"+aMethodSignature;
		}
	}
	
	public static class BehaviorInfo
	{
		public final String className;
		public final String behaviorName;
		public final String behaviorSignature;
		public final int id;
		
		public BehaviorInfo(String aClassName, String aBehaviorName, String aBehaviorSignature, int aId)
		{
			className = aClassName;
			behaviorName = aBehaviorName;
			behaviorSignature = aBehaviorSignature;
			id = aId;
		}
	}
}
