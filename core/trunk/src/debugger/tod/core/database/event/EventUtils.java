/*
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
package tod.core.database.event;

import java.util.HashSet;
import java.util.Set;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;

/**
 * Provides utility methods related to events
 * @author gpothier
 */
public class EventUtils
{
	private static final IgnorableExceptions IGNORABLE_EXCEPTIONS = new IgnorableExceptions();
	
	public static String getVariableName(ILocalVariableWriteEvent aEvent)
	{
		IBehaviorInfo theInfo = aEvent.getParent().getExecutedBehavior();
		
		int theBytecodeIndex = aEvent.getOperationBytecodeIndex();
		short theVariableIndex = aEvent.getVariable().getIndex();
		
		// 35 is the size of the instrumentation
		LocalVariableInfo theLocalVariableInfo = theInfo != null ?
				theInfo.getLocalVariableInfo(theBytecodeIndex+35, theVariableIndex)
                : null;
                
		String theName = theLocalVariableInfo != null ? 
				theLocalVariableInfo.getVariableName() 
				: "$("+aEvent.getOperationBytecodeIndex()+", "+theVariableIndex+")";


		return theName;
	}
	
	/**
	 * Indicates if the given exception is ignorable.
	 * Ignorable exceptions include:
	 * <li>Exceptions generated by the standard classloading mechanism</li>
	 */
	public static boolean isIgnorableException (IExceptionGeneratedEvent aEvent)
	{
		return IGNORABLE_EXCEPTIONS.isIgnorableException(aEvent);
	}
	
	private static class IgnorableExceptions
	{
		private Set<String> itsIgnorableExceptions = new HashSet<String>();

		public IgnorableExceptions()
		{
			ignore("java.lang.ClassLoader", "findBootstrapClass");
			ignore("java.net.URLClassLoader$1", "run");
			ignore("java.net.URLClassLoader", "findClass");
			ignore("sun.misc.URLClassPath", "getLoader");
			ignore("sun.misc.URLClassPath$JarLoader", "getJarFile");
		}
		
		private void ignore (String aType, String aBehavior)
		{
			itsIgnorableExceptions.add (aType+"."+aBehavior);
		}
		
		public boolean isIgnorableException (IExceptionGeneratedEvent aEvent)
		{
			IBehaviorInfo theBehavior = aEvent.getOperationBehavior();
			if (theBehavior == null) return true; // TODO: this is temporary
			
			ITypeInfo theType = theBehavior.getType();
			return itsIgnorableExceptions.contains(theType.getName()+"."+theBehavior.getName());
		}
	}
}
