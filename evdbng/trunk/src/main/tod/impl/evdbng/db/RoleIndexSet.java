/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import tod.impl.database.AbstractFilteredBidiIterator;
import tod.impl.database.IBidiIterator;
import tod.impl.evdbng.db.IndexSet.IndexManager;
import tod.impl.evdbng.db.file.BTree;
import tod.impl.evdbng.db.file.PagedFile;
import tod.impl.evdbng.db.file.RoleTree;
import tod.impl.evdbng.db.file.RoleTuple;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

/**
 * An index set where index tuples have associated roles
 * @author gpothier
 */
public class RoleIndexSet extends IndexSet<RoleTuple>
{
	/**
	 * Represents any of the behavior roles.
	 */
	public static final byte ROLE_BEHAVIOR_ANY = 0;
	
	/**
	 * Represents either {@link #ROLE_BEHAVIOR_CALLED} or {@link #ROLE_BEHAVIOR_EXECUTED}.
	 */
	public static final byte ROLE_BEHAVIOR_ANY_ENTER = 1;
	
	public static final byte ROLE_BEHAVIOR_CALLED = 2;
	public static final byte ROLE_BEHAVIOR_EXECUTED = 3;
	public static final byte ROLE_BEHAVIOR_EXIT = 4;
	public static final byte ROLE_BEHAVIOR_OPERATION = 5;
	
	
	/**
	 * Roles are negative unless when it deals with arguments. In this case, 
	 * role value is the argument position (starting a 1).
	 */
	public static final byte ROLE_OBJECT_TARGET = -1;
	public static final byte ROLE_OBJECT_VALUE = -2;
	public static final byte ROLE_OBJECT_RESULT = -3;
	public static final byte ROLE_OBJECT_EXCEPTION = -4;
	public static final byte ROLE_OBJECT_ANYARG = -5;
	public static final byte ROLE_OBJECT_ANY = -6;
	
	public RoleIndexSet(
			IndexManager aIndexManager, 
			String aName, 
			PagedFile aFile, 
			int aIndexCount)
	{
		super(aIndexManager, aName, aFile, aIndexCount);
	}
	
	@Override
	public BTree<RoleTuple> createIndex(int aIndex)
	{
		return new RoleTree(getName()+"-"+aIndex, getFile());
	}

	@Override
	public BTree<RoleTuple> loadIndex(int aIndex, PageIOStream aStream)
	{
		return new RoleTree(getName()+"-"+aIndex, getFile(), aStream);
	}
	
	@Override
	public RoleTree getIndex(int aIndex)
	{
		return (RoleTree) super.getIndex(aIndex);
	}

	public void add(int aIndex, long aKey, byte aRole)
	{
		getIndex(aIndex).add(aKey, aRole);
	}

	/**
	 * Creates an iterator that filters out the tuples from a source iterator that
	 * don't have one of the specified roles.
	 */
	public static IBidiIterator<RoleTuple> createFilteredIterator(
			IBidiIterator<RoleTuple> aIterator,
			final byte... aRole)
	{
		return new AbstractFilteredBidiIterator<RoleTuple, RoleTuple>(aIterator)
		{
			@Override
			protected Object transform(RoleTuple aIn)
			{
				int theRole = aIn.getRole();
				for (byte theAllowedRole : aRole)
				{
					if (theRole == theAllowedRole) return aIn;
				}
				return REJECT;
			}
		};
	}
	
	/**
	 * Creates an iterator that filters out duplicate tuples, which is useful when the 
	 * role is not checked: for instance if a behavior call event has the same called
	 * and executed method, it would appear twice in the behavior index with
	 * a different role.
	 */
	public static IBidiIterator<RoleTuple> createFilteredIterator(
			IBidiIterator<RoleTuple> aIterator)
	{
		return new DuplicateFilterIterator(aIterator);
	}
	
	private static class DuplicateFilterIterator extends AbstractFilteredBidiIterator<RoleTuple, RoleTuple>
	{
		private long itsLastKey;
		private int itsDirection = 0;
		
		public DuplicateFilterIterator(IBidiIterator<RoleTuple> aIterator)
		{
			super(aIterator);
			itsLastKey = -1;
		}
		
		@Override
		protected RoleTuple fetchNext()
		{
			if (itsDirection != 1) itsLastKey = -1;
			itsDirection = 1;
			return super.fetchNext();
		}
		
		@Override
		protected RoleTuple fetchPrevious()
		{
			if (itsDirection != -1) itsLastKey = -1;
			itsDirection = -1;
			return super.fetchPrevious();
		}
		
		@Override
		protected Object transform(RoleTuple aIn)
		{
			if (aIn.getKey() == itsLastKey) return REJECT;
			itsLastKey = aIn.getKey();
			
			return aIn;
		}
	}
	
}
