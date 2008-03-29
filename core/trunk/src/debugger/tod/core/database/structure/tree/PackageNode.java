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

import java.util.Collections;

import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.ILocationInfo;
import zz.utils.tree.SimpleTree;

public class PackageNode extends LocationNode
{
	public PackageNode(SimpleTree<ILocationInfo> aTree, PackageInfo aValue)
	{
		super(aTree, false, aValue);
	}
	
	/**
	 * Retrieves the package node corresponding to the given name,
	 * creating it if needed.
	 */
	public PackageNode getPackageNode(String aName)
	{
		int theIndex = Collections.binarySearch(
				pChildren().get(),
				aName, 
				PackageComparator.PACKAGE);
		
		if (theIndex >= 0) 
		{
			// return existing node
			return (PackageNode) pChildren().get(theIndex);
		}
		else
		{
			// create new node
			PackageInfo thePackage = new PackageInfo(aName);
			PackageNode theNode = new PackageNode(getTree(), thePackage);
			pChildren().add(-theIndex-1, theNode);
			return theNode;
		}
	}
	
	/**
	 * Retrieves the class node corresponding to the given name.
	 */
	public ClassNode getClassNode(String aName)
	{
		int theIndex = Collections.binarySearch(
				pChildren().get(), 
				aName,
				PackageComparator.CLASS);
		
		if (theIndex < 0) throw new RuntimeException("Class node not found: "+aName); 
		return (ClassNode) pChildren().get(theIndex);
	}
	
	/**
	 * Adds a new class node
	 */
	public ClassNode addClassNode(IClassInfo aClassInfo, boolean aShowFields, boolean aShowBehaviors)
	{
		int theIndex = Collections.binarySearch(
				pChildren().get(), 
				aClassInfo.getName(),
				PackageComparator.CLASS);
		
		if (theIndex >= 0) throw new RuntimeException("Class node already exists: "+aClassInfo); 

		ClassNode theNode = new ClassNode(getTree(), aClassInfo, aShowFields, aShowBehaviors);
		pChildren().add(-theIndex-1, theNode);
		return theNode;
	}
	
	
	/**
	 * Adds a new class node
	 */
	public AspectNode addAspectNode(IAspectInfo aAspect, boolean aShowAdvices)
	{
		int theIndex = Collections.binarySearch(
				pChildren().get(), 
				aAspect.getName(),
				PackageComparator.CLASS);
		
		if (theIndex >= 0) throw new RuntimeException("Aspect node already exists: "+aAspect); 
		
		AspectNode theNode = new AspectNode(getTree(), aAspect, aShowAdvices);
		pChildren().add(-theIndex-1, theNode);
		return theNode;
	}
	
}