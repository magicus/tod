package tod.impl.evdbng.db.file;

import static tod.impl.evdbng.db.file.PagedFile.PAGE_SIZE;
import tod.impl.evdbng.db.file.Page.PageIOStream;


/**
 * Succinct representation of a tree, based on the SODA'10 paper
 * of Sadakane and Navarro (http://www.dcc.uchile.cl/~gnavarro/publ.html).
 * 
 * @author gpothier
 */
public class RangeMinMaxTree
{
	private final PagedFile itsFile;
	private static final int BITS_PER_PAGE = PAGE_SIZE*8;
	private static final int PACKETS_PER_PAGE = BITS_PER_PAGE/32;
	private static final int MAX_LEVELS = 6;
	
	/**
	 * Size in bytes of each tuple (for all levels except 0).
	 * 3 shorts for e, m and M, and one int for page pointer.
	 */
	private static final int TUPLE_BYTES = 10;
	
	private static final int TUPLES_PER_PAGE = PAGE_SIZE/TUPLE_BYTES;

	
	private static final int TUPLE_OFFSET_SUM = 0; 
	private static final int TUPLE_OFFSET_MIN = 2; 
	private static final int TUPLE_OFFSET_MAX = 4; 
	private static final int TUPLE_OFFSET_PTR = 6; 
	
	
	/**
	 * The integer MASKS[i] has the (32-i)th bit set and all other bits are 0.
	 */
	private static final int[] MASKS = new int[32];
	
	static
	{
		for(int i=31, j=1;i>=0;i--, j<<=1) MASKS[i] = j; 
	}
	
	/**
	 * The first page of each level of the tree. Index 0 corresponds to the leaves.
	 */
	private PageIOStream[] itsLevels = new PageIOStream[MAX_LEVELS];
	
	private int itsCurrentPacket;
	private int itsCurrentPacketMask = MASKS[0];
	
	private int itsCurrentSum = 0;
	
	private int[] itsCurrentMin = new int[MAX_LEVELS];
	private int[] itsCurrentMax = new int[MAX_LEVELS]; 
	
	public RangeMinMaxTree(PagedFile aFile)
	{
		itsFile = aFile;
		itsLevels[0] = itsFile.create().asIOStream();
		
		itsCurrentMin[0] = 1;
		itsCurrentMax[0] = -1;
		
		for(int i=1;i<MAX_LEVELS;i++) 
		{
			itsCurrentMin[i] = Short.MAX_VALUE;
			itsCurrentMax[i] = Short.MIN_VALUE;
		}
	}
	
	/**
	 * Writes an open parenthesis (start of a node)
	 */
	public void open()
	{
		itsCurrentPacket |= itsCurrentPacketMask;
		itsCurrentPacketMask >>>= 1;
		
		itsCurrentSum++;
		if (itsCurrentMax[0] < itsCurrentSum) itsCurrentMax[0] = itsCurrentSum;
		
		if (itsCurrentPacketMask == 0) writePacket();
	}
	
	/**
	 * Writes a close parenthesis (end of a node)
	 */
	public void close()
	{
		itsCurrentPacketMask >>>= 1;
		
		itsCurrentSum--;
		if (itsCurrentMin[0] > itsCurrentSum) itsCurrentMin[0] = itsCurrentSum;
		
		if (itsCurrentPacketMask == 0) writePacket();
	}
	
	private void writePacket()
	{
		PageIOStream stream = itsLevels[0];
		stream.writeInt(itsCurrentPacket);
		itsCurrentPacket = 0;
		itsCurrentPacketMask = MASKS[0];

		if (stream.remaining() == 0) commitLeaf();
	}
	
	private void commitLeaf()
	{
		commitLevel(0);
	}
	
	/**
	 * Commits (= finishes) a page at the indicated level.
	 * This outputs a tuple at the above level, and recursively commits the above page if needed. 
	 */
	private void commitLevel(int l)
	{
		assert isShort(itsCurrentSum);
		assert isShort(itsCurrentMin[l]);
		assert isShort(itsCurrentMax[l]);

		if (itsCurrentMin[l+1] > itsCurrentMin[l]) itsCurrentMin[l+1] = itsCurrentMin[l];
		if (itsCurrentMax[l+1] < itsCurrentMax[l]) itsCurrentMax[l+1] = itsCurrentMax[l];
		
		PageIOStream stream = itsLevels[l+1];
		if (stream == null) 
		{
			stream = itsFile.create().asIOStream();
			itsLevels[l+1] = stream;
		}
		
		stream.writeSSSI(
				(short) itsCurrentSum, 
				(short) itsCurrentMin[l], 
				(short) itsCurrentMax[l], 
				itsLevels[l].getPage().getPageId());
		if (stream.remaining() < TUPLE_BYTES) commitLevel(l+1);
		
		itsLevels[l] = itsFile.create().asIOStream();
		
		itsCurrentMin[l] = itsCurrentSum+1;
		itsCurrentMax[l] = itsCurrentSum-1;
	}
	
	private static boolean isShort(int aValue)
	{
		return aValue >= Short.MIN_VALUE && aValue <= Short.MAX_VALUE;
	}
	
	private static boolean isBetween(int aValue, int aMin, int aMax)
	{
		return aValue >= aMin && aValue <= aMax;
	}
	
	/**
	 * Returns the ith bit
	 */
	public boolean get(long i)
	{
		long pageNumber = i/BITS_PER_PAGE;
		int offset = (int) (i%BITS_PER_PAGE);
		
		Page page = getNthPage(pageNumber, 0);
		int packetNumber = offset/32;
		int packetOffset = offset%32;
		
		int packet = getPacket(page, packetNumber);
		return (packet & MASKS[packetOffset]) != 0;
	}
	
	/**
	 * Retrives a packet from a leaf node.
	 * This method properly checks if the requested packet is the current packet.
	 */
	private int getPacket(Page aPage, int aNumber)
	{
		if (aPage.getPageId() == itsLevels[0].getPage().getPageId())
		{
			// Check if we are querying the current packet
			if (itsLevels[0].getPos() == aNumber*4) return itsCurrentPacket;
		}
		return aPage.readInt(aNumber*4);
	}
	
	/**
	 * Retrieves the Nth page at a given level.
	 */
	private Page getNthPage(long n, int aLevel)
	{
		PageIOStream up = itsLevels[aLevel+1];
		if (up == null)
		{
			if (n != 0) throw new RuntimeException("Requested page #"+n+" from level "+aLevel);
			return itsLevels[aLevel].getPage();
		}
		else
		{
			long parentPage = n/TUPLES_PER_PAGE;
			int offset = (int) n%TUPLES_PER_PAGE;
			Page parent = getNthPage(parentPage, aLevel+1);
			
			if (parent.getPageId() == up.getPage().getPageId())
			{
				// We might be trying to access the current page
				if (up.getPos() == offset*TUPLE_BYTES) return itsLevels[aLevel].getPage();
			}
			
			int id = parent.readInt(offset*TUPLE_BYTES + TUPLE_OFFSET_PTR);
			return itsFile.get(id);
		}
	}
	
	// For unit tests
	long _test_fwdsearch_π(long i, int d)
	{
		return fwdsearch_π(i, d);
	}
	

	
	// G[i] = sum(0, i)
	// The kth leftmost leaf of the tree stores the sub-vector[lk, rk]
	// e[k] = sum(0, rk)
	// m[k] = e[k-1] + rmq(lk, rk)
	// M[k] = e[k-1] + RMQ(lk, rk)
	private long fwdsearch_π(long i, int d)
	{
		// Index of the leaf containing i
		long k = i/BITS_PER_PAGE;
		int offset = (int) (i%BITS_PER_PAGE);
		
		Page leaf = getNthPage(k, 0);
		int sum;
		
		// Search within the page (note: we perform the search regardless of the value of d
		// because we also need to compute the sum).
		int result = fwdsearch_π(leaf, offset, d);
		if (result >= 0) return (k*BITS_PER_PAGE)+result;
		else sum = (result << 1) >> 1;
		
		if (itsLevels[1] == null) return -1;
		
		// Not in the same page, we must walk the tree
		
		long parentPageNumber = k/TUPLES_PER_PAGE;
		int parentTupleNumber = (int) (k%TUPLES_PER_PAGE);
		Page page = getNthPage(parentPageNumber, 1);
		
		assert leaf.getPageId() == page.readInt(parentTupleNumber*TUPLE_BYTES + TUPLE_OFFSET_PTR);
		int e_k = page.readShort(parentTupleNumber*TUPLE_BYTES + TUPLE_OFFSET_SUM); 
		
		// Compute the global target value we seek: d' = G[i-1]+d = e[k] - sum(π, i, rk) + d
		int d_ = e_k-sum+d;
		
		// Walk up the tree
		
		int lastSum = e_k;
		
		int level = 1;
		long kInc = 1;
		k += kInc;
		up:
		while(true)
		{
			// Check the tuples of the current page
			for(int j=parentTupleNumber+1;j<TUPLES_PER_PAGE;j++)
			{
				int min = page.readShort(j*TUPLE_BYTES + TUPLE_OFFSET_MIN);
				int max = page.readShort(j*TUPLE_BYTES + TUPLE_OFFSET_MAX);
				if (isBetween(d_, min, max)) 
				{
					level--;
					kInc /= TUPLES_PER_PAGE;
					int childId = page.readInt(j*TUPLE_BYTES + TUPLE_OFFSET_PTR);
					page = itsFile.get(childId);
					break up;
				}
				lastSum = page.readShort(j*TUPLE_BYTES + TUPLE_OFFSET_SUM);
				k += kInc;
			}
			
			level++;
			kInc *= TUPLES_PER_PAGE;
			if (itsLevels[level] == null) return -1;
				
			long newParentNumber = parentPageNumber/TUPLES_PER_PAGE;
			int newTupleNumber = (int) (parentPageNumber%TUPLES_PER_PAGE);
			parentPageNumber = newParentNumber;
			parentTupleNumber = newTupleNumber;
			page = getNthPage(parentPageNumber, level);
		}
		
		// Walk down the tree
		while(level > 0)
		{
			for(int j=0;j<TUPLES_PER_PAGE;j++)
			{
				int min = page.readShort(j*TUPLE_BYTES + TUPLE_OFFSET_MIN);
				int max = page.readShort(j*TUPLE_BYTES + TUPLE_OFFSET_MAX);
				if (isBetween(d_, min, max)) 
				{
					level--;
					kInc /= TUPLES_PER_PAGE;
					int childId = page.readInt(j*TUPLE_BYTES + TUPLE_OFFSET_PTR);
					page = itsFile.get(childId);
					break;
				}
				lastSum = page.readShort(j*TUPLE_BYTES + TUPLE_OFFSET_SUM);
				k += kInc;
			}
		}
		
		// Check leaf page
		result = fwdsearch_π(leaf, 0, d_-lastSum);
		if (result >= 0) return (k*BITS_PER_PAGE)+result;
		else throw new RuntimeException("Internal error");
	}
	
	/**
	 * See {@link #position_π(int, int)}
	 */
	private static final byte[] TABLE_POSITION_π = CREATE_TABLE_POSITION_π();
	
	private static int TABLE_POSITION_π_index(int aData, int aStart, int aValue)
	{
		int v = aValue+8;
		return (aStart*18 + v)*256 + aData;
	}
	
	private static int TABLE_POSITION_π_index0(int aData, int aValue)
	{
		int v = aValue+8;
		return v*256 + aData;
	}
	
	private static byte[] CREATE_TABLE_POSITION_π()
	{
		// 256 possible data values
		// 8 possible starting positions
		// 17 possible target values + 1 for total byte sum
		byte[] table = new byte[256*8*18];
		
		for(int v=-8;v<=9;v++) for (int s=0;s<8;s++) for (int d=0;d<256;d++)
		{
			table[TABLE_POSITION_π_index(d, s, v)] = precalculate_position_π(d, s, v);
		}
		
		return table;
	}
	
	private static byte precalculate_position_π(int aData, int aStart, int aValue)
	{
		assert isBetween(aStart, 0, 7);
		
		int sum = 0;
		int mask = 1 << (7-aStart);
		for(int i=aStart;i<8;i++)
		{
			boolean bit = (aData & mask) != 0;
			sum += bit ? 1 : -1;
			if (sum == aValue) return (byte) (i-aStart);
			mask >>>= 1;
		}
		return (byte) (0x80 | sum);
	}
	
	/**
	 * Indicates in which position of the given data byte the sum of
	 * function π, starting at the specified position, reaches the specified value. 
	 * @param aData The data byte: 0..255
	 * @param aStart The starting position of the sum: 0..7 (0 is the MSB)
	 * @param aValue The searched value: anything, but only -8..8 can provide a position
	 * @return Positive or 0 means the position (relative to aStart), 
	 * otherwise returns 0x80 | sum until the end of the byte
	 */
	private byte position_π(int aData, int aStart, int aValue)
	{
		return TABLE_POSITION_π[TABLE_POSITION_π_index(aData, aStart, aValue)];
	}
	
	private byte position_π(int aData, int aValue)
	{
		return TABLE_POSITION_π[TABLE_POSITION_π_index0(aData, aValue)];
	}
	
	private static int getByte(int aData, int aByteNumber)
	{
		switch(aByteNumber)
		{
		case 0: return 0xff & (aData >>> 24);
		case 1: return 0xff & (aData >>> 16);
		case 2: return 0xff & (aData >>> 8);
		case 3: return 0xff & aData;
		default: throw new RuntimeException();
		}		
	}
	
	// For unit tests
	int _test_fwdsearch_π(Page aPage, int i, int d)
	{
		itsLevels[0].setPos(-1);
		return fwdsearch_π(aPage, i, d);
	}
	
	/**
	 * fwdsearch within a page.
	 * @return If positive or 0, the position of the answer (within the page).
	 * Otherwise, 0x80000000 | sum from i until the end of the page
	 */
	private int fwdsearch_π(Page aPage, int i, int d)
	{
		int originalTarget = d;
		
		int packetNumber = i/32;
		int packetOffset = i%32;
		
		int packet = getPacket(aPage, packetNumber);
		
		int byteNumber = packetOffset/8;
		int byteOffset = packetOffset%8;

		if (byteOffset > 0)
		{
			// Check if the answer might be in the remaining portion of the first byte
			byte pos = position_π(
					getByte(packet, byteNumber), 
					byteOffset, 
					isBetween(d, -8, 8) ? d : 9);
			
			if (pos >= 0) return i+pos;
			else
			{
				pos = (byte) ((byte) (pos << 1) >> 1);
				d -= pos;
				i += 8-byteOffset;
				byteNumber++;
			}
		}
		
		while(true)
		{
			if (byteNumber == 4)
			{
				byteNumber = 0;
				packetNumber++;
				if (packetNumber >= PACKETS_PER_PAGE) return 0x80000000 | (originalTarget-d);
				packet = getPacket(aPage, packetNumber);
			}
			
			byte pos = position_π(getByte(packet, byteNumber), isBetween(d, -8, 8) ? d : 9);
			if (pos >= 0) return i+pos;
			else
			{
				pos = (byte) ((byte) (pos << 1) >> 1);
				d -= pos;
				i += 8;
				byteNumber++;
			}
		}
	}

	
	private void fwdsearch_ψ()
	{
		
	}
	
	private void fwdsearch_Φ()
	{
		
	}
	
	
}
