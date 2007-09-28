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
package tod;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

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

}
