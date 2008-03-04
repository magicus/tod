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
package tod.core.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a filter that accepts specific classes.
 * @author gpothier
 */
public class ClassFilter implements ClassSelector
{
	private List<ClassSelector> itsSelectors = 
		new ArrayList<ClassSelector>();

	/**
	 * Indicates if this filter is currently empty,
	 * ie. doesn't accept any class.
	 */
	public boolean isEmpty() 
	{
		return itsSelectors.isEmpty();
	}
	
	/**
	 * Enables logging for the specified package.
	 * @param aRecursive Whether to consider subpackages
	 */
	public void addPackage (String aPackageName, boolean aRecursive)
	{
		StaticConfig.getInstance().checkState();
		itsSelectors.add (new PackageCS(aPackageName, aRecursive));
	}
	
	/**
	 * Enables logging for the specified packages.
	 * @param aRecursive Whether to consider subpackages
	 */
	public void addPackages (String[] aPackageNames, boolean aRecursive)
	{
		StaticConfig.getInstance().checkState();
		itsSelectors.add (new PackageCS(aPackageNames, aRecursive));
	}

	/**
	 * Enables logging for the specified class.
	 */
	public void addClass (String aClassName)
	{
		StaticConfig.getInstance().checkState();
		itsSelectors.add (new NameCS(aClassName));
	}

	/**
	 * Enables logging for the specified classes.
	 */
	public void addClasses (String[] aClassNames)
	{
		StaticConfig.getInstance().checkState();
		itsSelectors.add (new NameCS(aClassNames));
	}
	
	public boolean accept(String aName)
	{
		for (ClassSelector theSelector : itsSelectors)
		{
			if (theSelector.accept(aName)) return true;
		}
		return false;
	}



}
