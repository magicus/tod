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
package tod.impl.dbgrid.dispatch.tree;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.db.NodeRejectedException;
import tod.impl.dbgrid.dispatch.RIDispatchNode;
import tod.impl.dbgrid.dispatch.RIEventDispatcher;
import zz.utils.ListMap;

/**
 * A dispatch tree structure that is created from an XML definition.
 * @author gpothier
 */
public class FixedDispatchTreeStructure extends DispatchTreeStructure
{
	
	private DispatchNode itsRoot;
	
	/**
	 * Node map for assigning roles
	 */
	private ListMap<String, DispatchNode> itsNodesMap1 = new ListMap<String, DispatchNode>();
	
	/**
	 * Node map for establishing connections.
	 */
	private ListMap<String, DispatchNode> itsNodesMap2 = new ListMap<String, DispatchNode>();
	
	private List<DispatchNode> itsNodes = new ArrayList<DispatchNode>();
	
	public FixedDispatchTreeStructure(Reader aReader)
	{
		super(0, 0, 0);
		try
		{
			DocumentBuilder theBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document theDocument = theBuilder.parse(new InputSource(aReader));
			NodeInfo theRoot = parseNode(theDocument.getDocumentElement());
			itsRoot = buildNode(null, theRoot);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public NodeRole getRoleForNode0(String aHostName)
	{
		List<DispatchNode> theList = itsNodesMap1.getList(aHostName);
		if (theList == null || theList.size() == 0) return null;
		
		DispatchNode theNode = theList.remove(theList.size()-1);
		itsNodesMap2.add(aHostName, theNode);

		return theNode.type;
	}
	
	@Override
	public synchronized String registerNode(RIDispatchNode aNode, String aHostname) throws NodeRejectedException
	{
		List<DispatchNode> theList = itsNodesMap2.getList(aHostname);
		if (theList == null || theList.size() == 0) throw new NodeRejectedException();
		
		DispatchNode theNode = pullCompatibleNode(theList, aNode);
		theNode.node = aNode;
		itsNodes.add(theNode);
		
		return super.registerNode(aNode, aHostname);
	}
	
	/**
	 * Removes from the list and returns the first {@link DispatchNode} that is
	 * compatible with the given node.
	 */
	private DispatchNode pullCompatibleNode(List<DispatchNode> aList, RIDispatchNode aNode)
	{
		for (Iterator<DispatchNode> theIterator = aList.iterator(); theIterator.hasNext();)
		{
			DispatchNode theDispatchNode = theIterator.next();
			if (theDispatchNode.type.isCompatible(aNode))
			{
				theIterator.remove();
				return theDispatchNode;
			}
		}
		return null;
	}

	@Override
	public void waitReady(GridMaster aMaster)
	{
		try
		{
			waitNodes();
			createRootDispatcher(aMaster);
			itsRoot.node = getRootDispatcher();
			
			for (DispatchNode theNode : itsNodes)
			{
				if (theNode.parent == null) continue;
				
				RIEventDispatcher theDispatcher = (RIEventDispatcher) theNode.parent.node;
				theNode.node.connectToDispatcher(theDispatcher.getAdress());
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	
	private NodeInfo parseNode(Element aElement)
	{
		String theNodeName = aElement.getNodeName();
		if (! "node".equals(theNodeName)) throw new IllegalArgumentException(theNodeName);
		
		String theName = aElement.getAttribute("name");
		
		NodeInfo theNodeInfo = new NodeInfo(theName);

		NodeList theChildren = aElement.getChildNodes();
		for (int i=0;i<theChildren.getLength();i++)
		{
			Node theChild = theChildren.item(i);
			if (theChild.getNodeType() != Node.ELEMENT_NODE) continue;
			
			theNodeInfo.addChild(parseNode((Element) theChild));
		}
	
		return theNodeInfo;
	}
	
	private DispatchNode buildNode(DispatchNode aParent, NodeInfo aNodeInfo)
	{
		String theName = aNodeInfo.getName();
		
		DispatchNode theNode;
		
		if (aNodeInfo.isDatabase())
		{
			assert aParent != null;
			incExpectedDatabaseNodes();
			theNode = new DispatchNode(NodeRole.DATABASE, aParent, theName);
		}
		else
		{
			NodeRole theType;
			
			if (aNodeInfo.isLeafDispatcher())
			{
				theType = NodeRole.LEAF_DISPATCHER;
				if (aParent != null) incExpectedLeafDispatchers();
			}
			else
			{
				theType = NodeRole.INTERNAL_DISPATCHER;
				if (aParent != null) incExpectedInternalDispatchers();
			}
			
			theNode = new DispatchNode(theType, aParent, theName);
			
			for(NodeInfo theChild : aNodeInfo.getChildren())
			{
				buildNode(theNode, theChild);
			}
		}
		
		itsNodesMap1.add(theName, theNode);
		return theNode;
	}
	
	private static class NodeInfo
	{
		private String itsName;
		private List<NodeInfo> itsChildren = new ArrayList<NodeInfo>();

		public NodeInfo(String aName)
		{
			itsName = aName;
		}
		
		public String getName()
		{
			return itsName;
		}
		
		public void addChild(NodeInfo aChild)
		{
			itsChildren.add(aChild);
		}
		
		public boolean isLeafDispatcher()
		{
			return ! isDatabase() && itsChildren.get(0).isDatabase();
		}
		
		public boolean isDatabase()
		{
			return itsChildren.size() == 0;
		}
		
		public Iterable<NodeInfo> getChildren()
		{
			return itsChildren;
		}
	}
	
	private static class DispatchNode
	{
		public final NodeRole type;
		public final DispatchNode parent;
		public final String name;
		public RIDispatchNode node = null;
		
		public DispatchNode(NodeRole aType, DispatchNode aParent, String aName)
		{
			type = aType;
			parent = aParent;
			name = aName;
			
			switch (type)
			{
			case DATABASE:
				if (parent == null) throw new IllegalArgumentException("No parent for database node: "+name);
				if (parent.type != NodeRole.LEAF_DISPATCHER) throw new IllegalArgumentException("Parent of " + name + "is a " + parent.type);
				break;
				
			case LEAF_DISPATCHER:
			case INTERNAL_DISPATCHER:
				if (parent != null && parent.type == NodeRole.DATABASE) throw new IllegalArgumentException("Parent of " + name + " is a DATABASE");
				break;
			
			default:
				throw new RuntimeException("Not handled: "+type);
			}
		}
	}

}
