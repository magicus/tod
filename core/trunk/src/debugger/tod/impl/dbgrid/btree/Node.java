package tod.impl.dbgrid.btree;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_BTREE_COUNT_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENTID_POINTER_SIZE;

import java.util.Arrays;

import tod.impl.dbgrid.dbnode.PagedFile.Page;
import tod.impl.dbgrid.dbnode.PagedFile.PageBitStruct;

/**
 * Node for a {@link BTree}.
 * @author gpothier
 */
public class Node
{
	private static final byte[] BLANK_POINTER = new byte[(EVENTID_POINTER_SIZE+7) / 8];
	
	/**
	 * The tree that owns this node.
	 */
	private BTree itsTree;
	
	private long itsPageId;
	
	/**
	 * Current number of used keys in this node.
	 */
	private int itsCount;
	private boolean itsLeaf;
	private boolean itsRoot;
	
	private byte[][] itsKeys;
	private long[] itsValues;
	private long[] itsChildrenIds;

	public Node(BTree aTree, long aPageId, boolean aLeaf)
	{
		itsTree = aTree;
		itsPageId = aPageId;
		itsLeaf = aLeaf;

		itsKeys = new byte[maxKeys()][];
		itsValues = new long[maxKeys()];
		itsChildrenIds = new long[maxChildren()];
		
		if (itsLeaf) itsChildrenIds[0] = -1;
	}

	private int maxKeys()
	{
		return itsTree.maxKeys();
	}
	
	private int minKeys()
	{
		return itsTree.minKeys();
	}
	
	private int maxChildren()
	{
		return itsTree.maxChildren();
	}
	
	private int minChildren()
	{
		return itsTree.minChildren();
	}
	
	/**
	 * Compares two event pointers.
	 */
	protected int compareKeys(byte[] aPointer1, byte[] aPointer2)
	{
		return itsTree.compareKeys(aPointer1, aPointer2);
	}

	/**
	 * Verifies that the node has the specified characteristics.
	 */
	public void assertNode(long aPageId, int aCount, byte[][] aKeys, long[] aValues, long[] aChildrenIds)
	{
		assert getPageId() == aPageId;
		assert itsCount == aCount;
		assert Arrays.equals(itsKeys, aKeys);
		assert Arrays.equals(itsValues, aValues);
		assert Arrays.equals(itsChildrenIds, aChildrenIds);
	}
	
	/**
	 * Loads a node from a page
	 */
	public static Node readFrom(BTree aTree, Page aPage)
	{
		Node theNode = new Node(aTree, aPage.getPageId(), false);
		PageBitStruct theBitStruct = aPage.asBitStruct();
		
		theNode.itsCount = theBitStruct.readInt(DB_BTREE_COUNT_BITS);
		theNode.itsLeaf = theBitStruct.readBoolean();
		
		for (int i=0;i<theNode.maxKeys();i++)
		{
			byte[] theKey = theBitStruct.readBytes(EVENTID_POINTER_SIZE);
			theNode.itsKeys[i] = BTree.isBlank(theKey) ? null : theKey;
			theNode.itsValues[i] = theBitStruct.readLong(DB_PAGE_POINTER_BITS);
		}

		for (int i=0;i<theNode.maxChildren();i++)
		{
			theNode.itsChildrenIds[i] = theBitStruct.readLong(DB_PAGE_POINTER_BITS);
		}
		
		theNode.check();
		return theNode;
	}
	
	public static Node create(BTree aTree, long aPageId, boolean aLeaf)
	{
		Node theNode = new Node(aTree, aPageId, aLeaf);
		theNode.itsCount = theNode.maxKeys();
		theNode.truncate(0);
		return theNode;
	}
	
	public void writeTo(Page aPage)
	{
		check();
		
		PageBitStruct theBitStruct = aPage.asBitStruct();
		
		theBitStruct.writeInt(itsCount, DB_BTREE_COUNT_BITS);
		theBitStruct.writeBoolean(itsLeaf);

		for (int i=0;i<maxKeys();i++)
		{
			byte[] theKey = itsKeys[i];
			
			theBitStruct.writeBytes(
					theKey != null ? theKey : BLANK_POINTER,
					EVENTID_POINTER_SIZE);
			
			theBitStruct.writeLong(itsValues[i], DB_PAGE_POINTER_BITS);
		}

		for (int i=0;i<maxChildren();i++)
		{
			theBitStruct.writeLong(itsChildrenIds[i], DB_PAGE_POINTER_BITS);
		}
	}
	
	/**
	 * Checks the integrity of this node
	 */
	public void check()
	{
		assert itsCount <= maxKeys();
		
		byte[] thelastKey = null;
		for (int i=0;i<itsCount;i++)
		{ 
			assert thelastKey == null || compareKeys(itsKeys[i], thelastKey) > 0;
			thelastKey = itsKeys[i];
		}
		
		for (int i=itsCount;i<maxKeys();i++)
		{
			assert itsKeys[i] == null;
			assert itsValues[i] == -1;
		}
		
		for (int i=0;i<maxChildren();i++)
		{
			assert i >= itsCount+1 || isLeaf() ? itsChildrenIds[i] == -1 : itsChildrenIds[i] >= 0;
		}
	}
	
	public long getPageId()
	{
		return itsPageId;
	}

	public long getChildId(int aIndex)
	{
		if (isLeaf()) return -1;
		else return itsChildrenIds[aIndex];
	}
	
	public Node readChild(int aIndex)
	{
		if (isLeaf()) return null;
		else return itsTree.getNode(getChildId(aIndex));
	}
	
	public void setChildId(int aIndex, long aId)
	{
		assert ! isLeaf();
		itsChildrenIds[aIndex] = aId;
	}
	
	public byte[] getKey(int aIndex)
	{
		return itsKeys[aIndex];
	}
	
	public void setKey(int aIndex, byte[] aKey)
	{
		itsKeys[aIndex] = aKey;
	}
	
	public long getValue(int aIndex)
	{
		return itsValues[aIndex];
	}
	
	public void setValue(int aIndex, long aValue)
	{
		itsValues[aIndex] = aValue;
	}
	
	public boolean isLeaf()
	{
		return itsLeaf;
	}
	
	public boolean isRoot()
	{
		return itsRoot;
	}

	public void setRoot(boolean aRoot)
	{
		itsRoot = aRoot;
	}

	public int getCount()
	{
		return itsCount;
	}
	
	public void truncate(int aNewCount)
	{
		assert aNewCount < itsCount;
		for (int i=aNewCount;i<itsCount;i++)
		{
			itsKeys[i] = null;
			itsValues[i] = -1;
			itsChildrenIds[i+1] = -1;
		}
		itsCount = aNewCount;
	}

	/**
	 * Searches for a key in this node. If the key is found its index
	 * is returned. Otherwise, the opposite minus one of the index of the subtree that should
	 * contain the key is returned.
	 */
	public int search(byte[] aKey)
	{
//		for (int i=0;i<itsCount;i++)
//		{
//			byte[] theKey = itsKeys[i];
//			int c = compareKeys(theKey, aKey);
//			if (c == 0) return i;
//			if (c > 0) return -i-1;
//		}
//		return -itsCount-1;
		
		return itsCount > 0 ? binarySearch(aKey, 0, itsCount-1) : -1;
	}
	
	private int binarySearch(byte[] aKey, int aFirst, int aLast)
	{
		assert aLast-aFirst > 0;
		
		byte[] theFirstKey = itsKeys[aFirst];
		byte[] theLastKey = itsKeys[aLast];
		
		int cf = compareKeys(aKey, theFirstKey);
		if (cf < 0) return -aFirst-1;
		if (cf == 0) return aFirst;
		
		int cl = compareKeys(aKey, theLastKey);
		if (cl == 0) return aLast;
		if (cl > 0) return -aLast-2;
		
		if (aLast-aFirst == 1) return -aLast-1;
		
		int theMiddle = (aFirst + aLast) / 2;
		byte[] theMiddleKey = itsKeys[theMiddle];
		
		int cm = compareKeys(aKey, theMiddleKey);
		if (cm == 0) return theMiddle;
		if (cm < 0) return binarySearch(aKey, aFirst, theMiddle);
		else return binarySearch(aKey, theMiddle, aLast);
	}
	
	/**
	 * Splits the child of this node at the specified index. The child node must be full
	 * and this node must not be full.
	 * @param aChildIndex The index of the node to split
	 * @param aChild The node to split (provided here because it is already loaded
	 * when this method is called).
	 * @return Returns the newly created node.
	 */
	public Node split(int aChildIndex, Node aChild)
	{
		assert itsCount < maxKeys(); // must not be full
		assert aChild.itsCount == maxKeys(); // child must be full
		
		Node theNewNode = itsTree.createNode(aChild.isLeaf());
		theNewNode.itsCount = minKeys();
		
		// Copy the tail of the splited node to the new node
		System.arraycopy(
				aChild.itsKeys, maxKeys()-minKeys(), 
				theNewNode.itsKeys, 0, 
				minKeys());
		
		System.arraycopy(
				aChild.itsValues, maxKeys()-minKeys(), 
				theNewNode.itsValues, 0, 
				minKeys());
		
		if (! aChild.isLeaf())
		{
			System.arraycopy(
					aChild.itsChildrenIds, maxChildren()-minChildren(), 
					theNewNode.itsChildrenIds, 0, 
					minChildren());
		}
		
		// Shift children, keys and values of this node by one.
		itsCount++;
		
		if (itsCount-aChildIndex > 1)
		{
			System.arraycopy(
					itsChildrenIds, aChildIndex+1, 
					itsChildrenIds, aChildIndex+2, 
					itsCount-aChildIndex-1);
			
			System.arraycopy(
					itsKeys, aChildIndex, 
					itsKeys, aChildIndex+1,
					itsCount-aChildIndex-1);
			
			System.arraycopy(
					itsValues, aChildIndex, 
					itsValues, aChildIndex+1,
					itsCount-aChildIndex-1);
		}

		// Place new child and middle key
		itsChildrenIds[aChildIndex+1] = theNewNode.getPageId();
		itsKeys[aChildIndex] = aChild.getKey(maxKeys()-minKeys()-1);
		itsValues[aChildIndex] = aChild.getValue(maxKeys()-minKeys()-1);
		aChild.truncate(minKeys());
		
		// Write out all modified nodes
		itsTree.writeNode(this);
		itsTree.writeNode(aChild);
		itsTree.writeNode(theNewNode);
		
		return theNewNode;
	}
	
	
	public void insert (int aIndex, byte[] aKey, long aValue)
	{
		assert isLeaf();
		assert itsCount < maxKeys();
		
		itsCount++;
		System.arraycopy(itsKeys, aIndex, itsKeys, aIndex+1, itsCount-aIndex-1);
		System.arraycopy(itsValues, aIndex, itsValues, aIndex+1, itsCount-aIndex-1);
		itsKeys[aIndex] = aKey;
		itsValues[aIndex] = aValue;
	}
	
	
	@Override
	public String toString()
	{
		return (isLeaf() ? "Leaf" : "Node") 
			+" (pid: "+getPageId()
			+", n: "+itsCount+", k: "
			+toString(itsKeys)+", v: "
			+toString(itsValues)+", c: "
			+toString(itsChildrenIds)+")";
	}
	
	private static String toString (byte[][] aArray)
	{
		StringBuilder theBuilder = new StringBuilder();
		for (byte[] theValue : aArray)
		{
			theBuilder.append(theValue+", ");
		}
		return theBuilder.toString();
	}

	private static String toString (long[] aArray)
	{
		StringBuilder theBuilder = new StringBuilder();
		for (long theValue : aArray)
		{
			theBuilder.append(theValue+", ");
		}
		return theBuilder.toString();
	}
}
