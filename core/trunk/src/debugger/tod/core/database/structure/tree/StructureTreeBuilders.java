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

import java.util.Map;
import java.util.StringTokenizer;

import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IStructureDatabase;
import zz.utils.tree.SimpleTree;
import zz.utils.tree.SimpleTreeNode;

/**
 * Provides various utiliy methods to create trees of some
 * structural nodes.
 * @author gpothier
 */
public class StructureTreeBuilders
{
	/**
	 * Creates a tree of packages and classes. 
	 * Classes can optionally have their members as children
	 * @return
	 */
	public static SimpleTree<ILocationInfo> createClassTree(
			IStructureDatabase aStructureDatabase,
			boolean aShowFields, 
			boolean aShowBehaviors)
	{
		IClassInfo[] theClasses = aStructureDatabase.getClasses();
		
		SimpleTree<ILocationInfo> theTree = new SimpleTree<ILocationInfo>()
		{
			protected SimpleTreeNode<ILocationInfo> createRoot()
			{
				return new PackageNode(this, new PackageInfo("Classes"));
			}
		};
		PackageNode theRoot = (PackageNode) theTree.getRoot();
		
		for (IClassInfo theClass : theClasses)
		{
			String theName = theClass.getName();
			StringTokenizer theTokenizer = new StringTokenizer(theName, ".");
			
			PackageNode theCurrentNode = theRoot;
			while (theTokenizer.hasMoreTokens())
			{
				String theToken = theTokenizer.nextToken();
				if (theTokenizer.hasMoreTokens())
				{
					// Token is still part of package name
					theCurrentNode = theCurrentNode.getPackageNode(theToken);
				}
				else
				{
					// We reached the class name
					theCurrentNode.addClassNode(theClass, aShowFields, aShowBehaviors);
				}
			}
		}
		
		return theTree;
	}

	/**
	 * Creates a tree of packages, aspects and optionally advices. 
	 */
	public static SimpleTree<ILocationInfo> createAspectTree(
			IStructureDatabase aStructureDatabase,
			boolean aShowAdvices)
	{
		Map<String, IAspectInfo> theAspectInfoMap = aStructureDatabase.getAspectInfoMap();
		
		SimpleTree<ILocationInfo> theTree = new SimpleTree<ILocationInfo>()
		{
			protected SimpleTreeNode<ILocationInfo> createRoot()
			{
				return new PackageNode(this, new PackageInfo("Aspects"));
			}
		};
		PackageNode theRoot = (PackageNode) theTree.getRoot();
		
		for (IAspectInfo theAspect : theAspectInfoMap.values())
		{
			String theName = theAspect.getName();
			StringTokenizer theTokenizer = new StringTokenizer(theName, ".");
			
			PackageNode theCurrentNode = theRoot;
			while (theTokenizer.hasMoreTokens())
			{
				String theToken = theTokenizer.nextToken();
				if (theTokenizer.hasMoreTokens())
				{
					// Token is still part of package name
					theCurrentNode = theCurrentNode.getPackageNode(theToken);
				}
				else
				{
					// We reached the class name
					theCurrentNode.addAspectNode(theAspect, aShowAdvices);
				}
			}
		}
		
		return theTree;
			}
	
}
