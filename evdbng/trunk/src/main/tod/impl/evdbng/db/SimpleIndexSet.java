/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import tod.impl.evdbng.db.IndexSet.IndexManager;
import tod.impl.evdbng.db.file.BTree;
import tod.impl.evdbng.db.file.PagedFile;
import tod.impl.evdbng.db.file.SimpleTree;
import tod.impl.evdbng.db.file.SimpleTuple;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

public class SimpleIndexSet extends IndexSet<SimpleTuple> 
{
	public SimpleIndexSet(
			IndexManager aIndexManager, 
			String aName, 
			PagedFile aFile, 
			int aIndexCount)
	{
		super(aIndexManager, aName, aFile, aIndexCount);
	}

	@Override
	public BTree<SimpleTuple> createIndex(int aIndex)
	{
		return new SimpleTree(getName()+"-"+aIndex, getFile());
	}

	@Override
	public BTree<SimpleTuple> loadIndex(int aIndex, PageIOStream aStream)
	{
		return new SimpleTree(getName()+"-"+aIndex, getFile(), aStream);
	}
	
	@Override
	public SimpleTree getIndex(int aIndex)
	{
		return (SimpleTree) super.getIndex(aIndex);
	}
	
	public void add(int aIndex, long aKey)
	{
		getIndex(aIndex).addAsync(aKey);
	}
}
