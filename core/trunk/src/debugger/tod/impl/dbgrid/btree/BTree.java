package tod.impl.dbgrid.btree;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_BTREE_COUNT_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENTID_POINTER_SIZE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tod.impl.dbgrid.dbnode.PagedFile;
import tod.impl.dbgrid.dbnode.PagedFile.Page;
import zz.utils.Utils;

/**
 * Simple B-Tree implementation, used for storing children event page pointers.
 * Keys are external event pointers and values are page ids.
 * Does not support key deletion.
 * @author gpothier
 */
public class BTree
{
	private PagedFile itsFile;
	private NodeManager itsNodeManager = new NodeManager();
	private Node itsRoot;
	
	private final int itsMaxKeys;
	private final int itsMinKeys;
	private final int itsMaxChildren;
	private final int itsMinChildren;

	public BTree(PagedFile aFile)
	{
		itsFile = aFile;
		
		int ps = aFile.getPageSize()*8; // page size
		int hs = DB_BTREE_COUNT_BITS + 1; // header size (count + leaf flag)
		
		int ks = EVENTID_POINTER_SIZE; // keys size
		int vs = DB_PAGE_POINTER_BITS; // values size
		int cs = DB_PAGE_POINTER_BITS; // children size
		
		int t = (ps - hs + (ks + vs))/(2*(ks + vs + cs));
		
		itsMaxKeys = 2*t - 1;
		itsMinKeys = t - 1;
		itsMaxChildren = 2*t;
		itsMinChildren = t;
		
		setRoot(createNode(true));
	}
	
	/**
	 * Compares two event pointers.
	 */
	protected int compareKeys(byte[] aPointer1, byte[] aPointer2)
	{
		return Utils.compare(aPointer1, aPointer2);
	}
	
	/**
	 * Indicates if the given key is blank.
	 */
	public static boolean isBlank(byte[] aKey)
	{
		for (byte b : aKey) if (b != 0) return false;
		return true;
	}
	
	public int maxKeys()
	{
		return itsMaxKeys;
	}
	
	public int minKeys()
	{
		return itsMinKeys;
	}
	
	public int maxChildren()
	{
		return itsMaxChildren;
	}
	
	public int minChildren()
	{
		return itsMinChildren;
	}

	/**
	 * Returns the node that corresponds to the given page id.
	 */
	public Node getNode(long aPageId)
	{
		return itsNodeManager.getNode(aPageId);
	}
	
	/**
	 * Stores the specified node, or schedules it for later storage. 
	 */
	public void writeNode(Node aNode)
	{
		itsNodeManager.markNode(aNode);
	}
	
	/**
	 * Creates a new node.
	 */
	public Node createNode(boolean aLeaf)
	{
		return itsNodeManager.create(aLeaf);
	}


	private void setRoot(Node aRoot)
	{
		if (itsRoot != null) itsRoot.setRoot(false);
		itsRoot = aRoot;
		itsRoot.setRoot(true);
	}
	
	/**
	 * Returns the value associated with the specified key.
	 */
	public Long get(byte[] aKey)
	{
		Node theNode = itsRoot;
		while (theNode != null)
		{
			int theLocation = theNode.search(aKey);
			if (theLocation >= 0)
			{
				// We found the key
				return theNode.getValue(theLocation);
			}
			else
			{
				// The key is not in the node, we descend into the appropriate child
				int theChildIndex = -theLocation-1;
				theNode = theNode.readChild(theChildIndex);
			}
		}
		
		return null;
	}
	
	public boolean put(byte[] aKey, long aValue)
	{
		assert ! isBlank(aKey);
		
		Node theNode = itsRoot;
		
		// 1. Check if we must split the root
		if (theNode.getCount() == maxKeys())
		{
			Node theNewRoot = createNode(false);
			theNewRoot.setChildId(0, itsRoot.getPageId());
			theNewRoot.split(0, itsRoot);
			setRoot(theNewRoot);
			theNode = theNewRoot;
		}
		
		// 2. Search insertion point
		while (! theNode.isLeaf())
		{
			int theLocation = theNode.search(aKey);
			if (theLocation >= 0)
			{
				// The key already exists: we simply replace the value
				theNode.setValue(theLocation, aValue);
				writeNode(theNode);
				return false;
			}
			else
			{
				int theChildIndex = -theLocation-1;
				Node theChildNode = theNode.readChild(theChildIndex);
				if (theChildNode.getCount() == maxKeys())
				{
					// If the child node is full we must split it.
					Node theNewNode = theNode.split(theChildIndex, theChildNode);
					byte[] theUpKey = theNode.getKey(theChildIndex);
					if (aKey == theUpKey)
					{
						// Check if the key that went up is our key
						theNode.setValue(theChildIndex, aValue);
						writeNode(theNode);
						return false;
					}
					else if (compareKeys(aKey, theUpKey) > 0) theChildNode = theNewNode;
				}
				theNode = theChildNode;
			}
		}
		
		// 3. Insert the key into the (non full) leaf
		int theLocation = theNode.search(aKey);
		if (theLocation >= 0)
		{
			// The key already exists: we simply replace the value
			theNode.setValue(theLocation, aValue);
			writeNode(theNode);
			return false;
		}
		else
		{
			int theChildIndex = -theLocation-1;
			theNode.insert(theChildIndex, aKey, aValue);
			writeNode(theNode);
			return true;
		}
	}
		
	@Override
	public String toString()
	{
		LinkedList<Node> theQueue = new LinkedList<Node>();
		theQueue.addLast(itsRoot);
		
		StringBuilder theBuilder = new StringBuilder();
		while (! theQueue.isEmpty())
		{
			Node theNode = theQueue.removeFirst();
			if (theNode == null)
			{
				theBuilder.append("\n");
			}
			else
			{
				theBuilder.append(theNode);
				theBuilder.append(" | ");
				
				if (! theNode.isLeaf()) 
				{
					theQueue.addLast(null);
					for (int i=0;i<theNode.getCount()+1;i++)
					{
						theQueue.addLast(theNode.readChild(i));
					}
				}
			}
		}
		
		return theBuilder.toString();
	}
	
	/**
	 * Manages a cache of recently used nodes
	 * @author gpothier
	 */
	private class NodeManager
	{
		private int itsCacheSize = 256;
		
		private Map<Long, Node> itsCachedNodes = new HashMap<Long, Node>();
		private Set<Node> itsDirtyNodes = new HashSet<Node>();
		
		/**
		 * Most recently used nodes list
		 */
		private LinkedList<Node> itsNodesList = new LinkedList<Node>();
		
		public void markNode(Node aNode)
		{
//			System.out.println("mark node: "+aNode.getPageId());
			itsDirtyNodes.add(aNode);
			itsCachedNodes.put(aNode.getPageId(), aNode);
			useNode(aNode);
		}
		
		private void useNode(Node aNode)
		{
			itsNodesList.remove(aNode);
			itsNodesList.addLast(aNode);
			
			if (itsNodesList.size() > itsCacheSize)
			{
				Node theNode = itsNodesList.removeFirst();
				if (itsDirtyNodes.remove(theNode)) saveNode(theNode);
				itsCachedNodes.remove(theNode.getPageId());
			}			
		}
		
		private void saveNode(Node aNode)
		{
//			System.out.println("save node: "+aNode.getPageId());
			Page thePage = itsFile.getPageForOverwrite(aNode.getPageId());
			aNode.writeTo(thePage);
			itsFile.writePage(thePage);
		}
		
		public Node getNode(long aId)
		{
//			System.out.println("get node: "+aId);
			Node theNode = itsCachedNodes.get(aId);
			if (theNode == null)
			{
				theNode = readNode(aId);
				itsCachedNodes.put(aId, theNode);
			}
			useNode(theNode);
			return theNode;
		}
		
		private Node readNode(long aId)
		{
//			System.out.println("read node: "+aId);
			Page thePage = itsFile.getPage(aId);
			return Node.readFrom(BTree.this, thePage);
		}
		
		public Node create(boolean aLeaf)
		{
			Page thePage = itsFile.createPage();
			return Node.create(BTree.this, thePage.getPageId(), aLeaf);
		}

	}
}
