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
package tod.core.database.structure.tree;

import java.util.Comparator;

import tod.core.database.structure.ILocationInfo;
import zz.utils.tree.SimpleTreeNode;

/**
 * Compares packages and classes.
 * Packages are always before classes, otherwise lexicographic order is used.
 * @author gpothier
 */
public class PackageComparator implements Comparator
{
	public static PackageComparator PACKAGE = new PackageComparator(true);
	public static PackageComparator CLASS = new PackageComparator(false);

	/**
	 * If true, compares against package names (package names always appear before
	 * class names).
	 */
	private boolean itsForPackage;
	
	private PackageComparator(boolean aForPackage)
	{
		itsForPackage = aForPackage;
	}
	
	public int compare(Object o1, Object o2)
	{
		SimpleTreeNode<ILocationInfo> node = (SimpleTreeNode<ILocationInfo>) o1;
		String name = (String) o2;
		
		ILocationInfo l = node.pValue().get();
		boolean p = l instanceof PackageInfo;
		
		if (p != itsForPackage) return p ? 1 : -1;
		else return l.getName().compareTo(name);
	}
}