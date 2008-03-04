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
package tod;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.ITypeInfo;

/**
 * @author gpothier
 */
public class Util
{
	public static final int TOD_REGISTRY_PORT = 10098;
	
	/**
	 * Path to the development eclipse workspace.
	 * It is used during development to avoid rebuilding jars. 
	 * If null, the development workspace is not available.
	 */
	public static final String workspacePath = System.getProperty("dev.path");
	
	public static void ensureSize (List<?> aList, int aSize)
	{
		while (aList.size() <= aSize) aList.add (null);
	}
	
	/**
	 * Retrieves the package name of the given class
	 */
	public static String getPackageName(String aFullyQualifiedName)
	{
		int theIndex = aFullyQualifiedName.lastIndexOf('.');
		if (theIndex == -1) return "";
		else return aFullyQualifiedName.substring(0, theIndex);
	}
	
	/**
	 * Strips the package name from the given class.
	 */
	public static String getSimpleName(String aFullyQualifiedName)
	{
		int theIndex = aFullyQualifiedName.lastIndexOf('.');
		
		String theName = theIndex == -1 ?
				aFullyQualifiedName
				: aFullyQualifiedName.substring(theIndex+1);
		
		return theName.replace('$', '.');
	}
	
	/**
	 * Transforms a JVM class descriptor into a normal, source-level class name.
	 */
	public static String jvmToScreen(String aName)
	{
		return aName.replace('/', '.');
	}
	
	public static String getSimpleInnermostName(String aFullyQualifiedName)
	{
		int theIndex = Math.max(
				aFullyQualifiedName.lastIndexOf('.'),
				aFullyQualifiedName.lastIndexOf('$'));
		
		String theName = theIndex == -1 ?
				aFullyQualifiedName
				: aFullyQualifiedName.substring(theIndex+1);
		
		return theName;
	}
	
	/**
	 * Returns the name of the class,
	 * with the '$' changed to a '.' in the case
	 * of an inner class.
	 */
	public static String getPrettyName(String aFullyQualifiedName)
	{
		return aFullyQualifiedName.replace('$', '.');
	}
	
	public static Registry getRegistry()
	{
        // Check if we use an existing registry of if we create a new one.
        Registry theRegistry = null;
        try
		{
        	theRegistry = LocateRegistry.getRegistry(TOD_REGISTRY_PORT);
			if (theRegistry != null) theRegistry.unbind("dummy");
		}
		catch (RemoteException e)
		{
            theRegistry = null;
		}
        catch(NotBoundException e)
        {
        	System.out.println("Found existing registry");
            // Ignore - we were able to reach the registry, which is all we wanted
        }
        
        if (theRegistry == null) 
        {
            try
			{
            	System.out.println("Creating new registry");
				LocateRegistry.createRegistry(TOD_REGISTRY_PORT);
				theRegistry = LocateRegistry.getRegistry("localhost", TOD_REGISTRY_PORT);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
        }

        return theRegistry;
	}
	
	/**
	 * Returns the name of the behavior plus its arguments.
	 */
	public static String getFullName(IBehaviorInfo aBehavior)
	{
		StringBuilder theBuilder = new StringBuilder(aBehavior.getName());
		theBuilder.append('(');
		boolean theFirst = true;
		for (ITypeInfo theType : aBehavior.getArgumentTypes())
		{
			if (theFirst) theFirst = false;
			else theBuilder.append(", ");
			theBuilder.append(theType.getName());
		}
		theBuilder.append(')');
		return theBuilder.toString();
	}
	
	/**
	 * Returns the full name of the given member, including the parameters if
	 * it is a behavior. 
	 */
	public static String getFullName(IMemberInfo aMember)
	{
		if (aMember instanceof IBehaviorInfo)
		{
			IBehaviorInfo theBehavior = (IBehaviorInfo) aMember;
			return getFullName(theBehavior);
		}
		else if (aMember instanceof IFieldInfo)
		{
			IFieldInfo theField = (IFieldInfo) aMember;
			return theField.getName();
		}
		else throw new RuntimeException("Not handled: "+aMember);
	}
	

}
