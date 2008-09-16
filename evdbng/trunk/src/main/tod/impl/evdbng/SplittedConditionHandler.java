/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.evdbng;

import tod.impl.evdbng.db.IndexSet;
import tod.impl.evdbng.db.RoleIndexSet;
import tod.impl.evdbng.db.SimpleIndexSet;
import tod.impl.evdbng.queries.ArrayIndexCondition;
import tod.impl.evdbng.queries.Conjunction;
import tod.impl.evdbng.queries.ObjectCondition;
import tod.impl.evdbng.queries.SimpleCondition;
import zz.utils.bit.BitUtils;

/**
 * Abstract factory for creating splitted conditions. Splitted conditions are
 * conditions on fields that have a domain too large to fit in a single index
 * set (eg. object id, or array index). When such a field is indexes, the key is
 * split into a number of components, usually by partitioning the bits of the
 * key value, and each key part is indexed separately. For queries, the key
 * value is split in the same way and a conjunctive condition is created for
 * each key part.
 * 
 * @author gpothier
 */
public abstract class SplittedConditionHandler<T extends IndexSet<?>>
{
	/**
	 * Splits the given index number into various parts.
	 * For a given condition factory, this method should always return
	 * the same number of parts, for all index number values. 
	 */
	protected abstract int[] splitIndex(long aIndex);
	
	protected abstract SimpleCondition createPartialCondition(
			int aPart,
			int aPartialKey,
			byte aRole);

	/**
	 * Creates a compound condition matching all key parts.
	 * @param aRole Optional tuple role.
	 */
	public Conjunction createCondition(long aIndex, byte aRole)
	{
		Conjunction theResult = new Conjunction(true, true);
		
		int[] theParts = splitIndex(aIndex);
		
		for(int i=0;i<theParts.length;i++)
		{
			theResult.add(createPartialCondition(i, theParts[i], aRole));
		}
		
		return theResult;
	}
	
	protected abstract void add(T aIndexSet, int aIndex, long aKey, byte aRole);
	
	/**
	 * Adds the specified tuple to all the indexes that correspond
	 * to the partition of the key.
	 */
	public void add(T[] aIndexSets, int aIndex, long aKey, byte aRole)
	{
		int[] theParts = splitIndex(aIndex);
		for(int i=0;i<theParts.length;i++)
		{
			add(aIndexSets[i], theParts[i], aKey, aRole);
		}
	}

	/**
	 * Whether the given part/partial key are a partial match for
	 * the given value.
	 */
	public boolean match(int aPart, int aPartialKey, long aIndex)
	{
		int[] theParts = splitIndex(aIndex);
		return theParts[aPart] == aPartialKey;
	}
	/**
	 * A standard index number partitioning method.
	 * @param aIndex The index number to partition.
	 * @param aBits The number of bits to go into each partition. 
	 */
	protected static int[] splitIndex(long aIndex, int aBits)
	{
		int[] theParts = new int[2];
		int theMask = BitUtils.pow2i(aBits)-1;
		
		int theIndex = (int) aIndex;
		theParts[0] = theIndex & theMask;
		theIndex >>>= aBits;
		theParts[1] = theIndex & theMask;
		theIndex >>>= aBits;
		if (theIndex != 0) throw new RuntimeException("Index overflow: "+aIndex);
		
		return theParts;
	}
	
	public static Objects OBJECTS = new Objects();
	public static ArrayIndexes INDEXES = new ArrayIndexes();

	public static class Objects extends SplittedConditionHandler<RoleIndexSet>
	{
		@Override
		protected SimpleCondition createPartialCondition(int aPart, int aPartialKey, byte aRole)
		{
			return new ObjectCondition(aPart, aPartialKey, aRole);
		}

		@Override
		protected int[] splitIndex(long aIndex)
		{
			return splitIndex(aIndex, DebuggerGridConfigNG.INDEX_OBJECT_PARTS);
		}

		@Override
		protected void add(RoleIndexSet aIndexSet, int aIndex, long aKey, byte aRole)
		{
			aIndexSet.add(aIndex, aKey, aRole);
		}
	}
	
	public static class ArrayIndexes extends SplittedConditionHandler<SimpleIndexSet>
	{
		@Override
		protected SimpleCondition createPartialCondition(int aPart, int aPartialKey, byte aRole)
		{
			return new ArrayIndexCondition(aPart, aPartialKey);
		}

		@Override
		protected int[] splitIndex(long aIndex)
		{
			return splitIndex(aIndex, DebuggerGridConfigNG.INDEX_ARRAY_INDEX_PARTS);
		}

		@Override
		protected void add(SimpleIndexSet aIndexSet, int aIndex, long aKey, byte aRole)
		{
			aIndexSet.add(aIndex, aKey);
		}
	}
}