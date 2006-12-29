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
package tod.core.database.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.database.browser.ILocationsRepository;


/**
 * Permits to build a tree of location info nodes.
 * @author gpothier
 */
public class LocationTreeBuilder
{
	private static Map<ILocationsRepository, LocationTreeBuilder> itsInstances = 
		new HashMap<ILocationsRepository, LocationTreeBuilder>();
	
	/**
	 * Returns a builder for the specified registerer.
	 * There is one shared instance for each registerer. 
	 */
	public static LocationTreeBuilder getInstance (ILocationsRepository aLocationTrace)
	{
		LocationTreeBuilder theInstance = itsInstances.get(aLocationTrace);
		if (theInstance == null)
		{
			theInstance = new LocationTreeBuilder(aLocationTrace);
			itsInstances.put(aLocationTrace, theInstance);
		}
		
		return theInstance;
	}
	

	
	
	private Map<String, PackageNode> itsPackageMap =
		new HashMap<String, PackageNode>();
	
	private Map<ITypeInfo, TypeNode> itsTypeNodesMap =
		new HashMap<ITypeInfo, TypeNode>();
	
	private PackageNode itsRootNode;

	private final ILocationsRepository itsLocationTrace;

	private LocationTreeBuilder(ILocationsRepository aLocationTrace)
	{
		itsLocationTrace = aLocationTrace;
		rebuild();
	}
	
	/**
	 * Cleans and fully updates this builder.
	 */
	public void rebuild ()
	{
		itsPackageMap.clear();
		itsTypeNodesMap.clear();
		itsRootNode = null;
		
		buildTypes(itsLocationTrace.getTypes());
		buildMembers(itsLocationTrace.getBehaviours());
		buildMembers(itsLocationTrace.getFields());		
	}
	
	/**
	 * Builds the packages & types.
	 */
	private void buildTypes(Iterable<ITypeInfo> aTypes)
	{
		itsRootNode = new PackageNode("");
		itsPackageMap.put ("", itsRootNode);

		// Build the tree
		for (ITypeInfo theType : aTypes)
		{
			String thePackageName = getPackageName(theType.getName());
			PackageNode thePackageNode = getPackageNode(thePackageName);
			createTypeNode(thePackageNode, theType);
		}
	}
	
	/**
	 * Fills types nodes with members from the given list.
	 */
	private void buildMembers (Iterable<? extends IMemberInfo> aMembers)
	{
		for (IMemberInfo theMemberInfo : aMembers)
		{
			ITypeInfo theTypeInfo = theMemberInfo.getType();
			TypeNode theTypeNode = getTypeNode(theTypeInfo);
			theTypeNode.addChild(new MemberNode(theMemberInfo));
		}
	}
	
	private void createTypeNode (Node aParentNode, ITypeInfo aInfo)
	{
		TypeNode theNode = new TypeNode(aInfo);
		itsTypeNodesMap.put(aInfo, theNode);
		aParentNode.addChild(theNode);
	}
	
	/**
	 * Returns the type node that corresponds to the given type descriptor.
	 */
	public TypeNode getTypeNode (ITypeInfo aInfo)
	{
		return (TypeNode) itsTypeNodesMap.get(aInfo);
	}
	
	/**
	 * Returns the root package node of this builder.
	 */
	public PackageNode getRootNode()
	{
		return itsRootNode;
	}
	
	private PackageNode getPackageNode (String aPackageName)
	{
		PackageNode theNode = (PackageNode) itsPackageMap.get(aPackageName);
		
		if (theNode == null)
		{
			theNode = new PackageNode(aPackageName);
			itsPackageMap.put(aPackageName, theNode);
			
			String theParentPackageName = getPackageName(aPackageName);
			PackageNode theParentNode = getPackageNode(theParentPackageName);
			theParentNode.addChild(theNode);
		}
		
		return theNode;
	}
	
	private static String getPackageName (String aName)
	{
		int theIndex = aName.lastIndexOf('.');
		return theIndex == -1 ? "" : aName.substring(0, theIndex);
	}
	
	private static String getLastName (String aName)
	{
		int theIndex = aName.lastIndexOf('.');
		return theIndex == -1 ? aName : aName.substring(theIndex+1);
	}
	
	public static abstract class Node
	{
		private Node itsParent;
		private List<Node> itsChildren;
		
		public Node getParent()
		{
			return itsParent;
		}
		
		protected void setParent(Node aParent)
		{
			itsParent = aParent;
		}
		
		public void addChild (Node aNode)
		{
			if (itsChildren == null) itsChildren = new ArrayList<Node>();
			itsChildren.add(aNode);
			aNode.setParent(this);
		}
		
		public Iterable<Node> getChildren()
		{
			return itsChildren;
		}
		
		public Node getChild (int aIndex)
		{
			return (Node) itsChildren.get(aIndex);
		}
		
		public int getSize ()
		{
			return itsChildren != null ? itsChildren.size() : 0;
		}
		
		public int indexOf (Node aNode)
		{
			return itsChildren.indexOf(aNode);
		}
		
		public abstract String getName();
	}
	
	public static class PackageNode extends Node
	{
		private String itsPackageName;
		private String itsDisplayName;
		
		public PackageNode(String aPackageName)
		{
			itsPackageName = aPackageName;
			itsDisplayName = getLastName(aPackageName);
		}
		
		public String getName()
		{
			return itsDisplayName;
		}
	}
	
	public static class TypeNode extends Node
	{
		private ITypeInfo itsTypeInfo;
		private String itsDisplayName;
		
		public TypeNode(ITypeInfo aTypeInfo)
		{
			itsTypeInfo = aTypeInfo;
			itsDisplayName = getLastName(itsTypeInfo.getName());
		}
		
		public ITypeInfo getTypeInfo()
		{
			return itsTypeInfo;
		}
		
		public String getName()
		{
			return itsDisplayName;
		}
	}

	public static class MemberNode extends Node
	{
		private IMemberInfo itsMemberInfo;
		private String itsDisplayName;
		
		public MemberNode(IMemberInfo aMemberInfo)
		{
			itsMemberInfo = aMemberInfo;
			itsDisplayName = aMemberInfo.getName();
		}
		
		public IMemberInfo getMemberInfo()
		{
			return itsMemberInfo;
		}
		
		public String getName()
		{
			return itsDisplayName;
		}
	}
}
