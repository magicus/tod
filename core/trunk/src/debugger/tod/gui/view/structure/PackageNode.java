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
package tod.gui.view.structure;

import java.util.Collections;

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
	public ClassNode addClassNode(IClassInfo aClassInfo)
	{
		int theIndex = Collections.binarySearch(
				pChildren().get(), 
				aClassInfo.getName(),
				PackageComparator.CLASS);
		
		if (theIndex >= 0) throw new RuntimeException("Class node already exists: "+aClassInfo); 

		ClassNode theNode = new ClassNode(getTree(), aClassInfo);
		pChildren().add(-theIndex-1, theNode);
		return theNode;
	}
	
}