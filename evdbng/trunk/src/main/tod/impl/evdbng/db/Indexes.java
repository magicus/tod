/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_ADVICE_SRC_ID_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_BEHAVIOR_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_BYTECODE_LOCS_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_DEPTH_RANGE;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_FIELD_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_THREADS_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_TYPE_COUNT;
import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_VAR_COUNT;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import tod.impl.evdbng.DebuggerGridConfig;
import tod.impl.evdbng.ObjectCodec;
import tod.impl.evdbng.SplittedConditionHandler;
import tod.impl.evdbng.db.IndexSet.IndexManager;
import tod.impl.evdbng.db.file.PagedFile;
import tod.impl.evdbng.db.file.RoleTree;
import tod.impl.evdbng.db.file.SimpleTree;
import zz.utils.bit.BitUtils;
import zz.utils.monitoring.AggregationType;
import zz.utils.monitoring.Monitor;
import zz.utils.monitoring.Probe;

/**
 * Groups all the indexes maintained by a database node.
 * @author gpothier
 */
public class Indexes 
{
	private IndexManager itsIndexManager = new IndexManager();
	
	private SimpleIndexSet itsThreadIndexSet;
	private SimpleIndexSet itsDepthIndexSet;
	private SimpleIndexSet itsLocationIndexSet;
	private SimpleIndexSet itsAdviceSourceIdIndexSet;
	private RoleIndexSet itsBehaviorIndexSet;
	private SimpleIndexSet itsFieldIndexSet;
	private SimpleIndexSet itsVariableIndexSet;
	
	/**
	 * (Split) index sets for array indexes 
	 */
	private SimpleIndexSet[] itsArrayIndexIndexSets;
	
	/**
	 * (Split) index sets for object ids.
	 */
	private RoleIndexSet[] itsObjectIndexeSets;
	
	private long itsMaxObjectId = 0;

	
	/**
	 * Protected constructor for subclasses. Does not initialize indexes.
	 */
	protected Indexes()
	{
	}
	
	public Indexes(PagedFile aFile)
	{
		Monitor.getInstance().register(this);
		
		itsThreadIndexSet = new SimpleIndexSet(itsIndexManager, "thread", aFile, STRUCTURE_THREADS_COUNT+1);
		itsDepthIndexSet = new SimpleIndexSet(itsIndexManager, "depth", aFile, STRUCTURE_DEPTH_RANGE+1);
		itsLocationIndexSet = new SimpleIndexSet(itsIndexManager, "bytecodeLoc.", aFile, STRUCTURE_BYTECODE_LOCS_COUNT+1);
		itsAdviceSourceIdIndexSet = new SimpleIndexSet(itsIndexManager, "advice src id", aFile, STRUCTURE_ADVICE_SRC_ID_COUNT+1);
		itsBehaviorIndexSet = new RoleIndexSet(itsIndexManager, "behavior", aFile, STRUCTURE_BEHAVIOR_COUNT+1);
		itsFieldIndexSet = new SimpleIndexSet(itsIndexManager, "field", aFile, STRUCTURE_FIELD_COUNT+1);
		itsVariableIndexSet = new SimpleIndexSet(itsIndexManager, "variable", aFile, STRUCTURE_VAR_COUNT+1);

		itsArrayIndexIndexSets = createSplitIndex(
				"index", 
				SimpleIndexSet.class, 
				DebuggerGridConfig.INDEX_ARRAY_INDEX_PARTS,
				aFile);
		
		itsObjectIndexeSets = createSplitIndex(
				"object",
				RoleIndexSet.class,
				DebuggerGridConfig.INDEX_OBJECT_PARTS,
				aFile);
	}
	
	/**
	 * Creates all the sub indexes for a split index.
	 * @param aName Name base of the indexes
	 * @param aIndexClass Class of each index
	 */
	private <T extends IndexSet> T[] createSplitIndex(
			String aName, 
			Class<T> aIndexClass, 
			int aBits,
			PagedFile aFile)
	{
		try
		{
			Constructor<T> theConstructor = aIndexClass.getConstructor(
					IndexManager.class,
					String.class,
					PagedFile.class, 
					int.class);
			
			T[] theResult = (T[]) Array.newInstance(aIndexClass, 2);
			theResult[0] = theConstructor.newInstance(itsIndexManager, aName+"-0", aFile, BitUtils.pow2i(aBits)+1);
			theResult[1] = theConstructor.newInstance(itsIndexManager, aName+"-1", aFile, BitUtils.pow2i(aBits)+1);
			
			return theResult;
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e.getCause());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Recursively disposes this object.
	 * Unregister all the indexes from the monitor.
	 */
	public void dispose()
	{
		Monitor.getInstance().unregister(this);

		itsThreadIndexSet.dispose();
		itsDepthIndexSet.dispose();
		itsLocationIndexSet.dispose();
		itsAdviceSourceIdIndexSet.dispose();
		itsBehaviorIndexSet.dispose();
		itsFieldIndexSet.dispose();
		itsVariableIndexSet.dispose();

		for (int i=0;i<itsArrayIndexIndexSets.length;i++)
		{
			itsArrayIndexIndexSets[i].dispose();
		}
		
		for (int i=0;i<itsObjectIndexeSets.length;i++)
		{
			itsObjectIndexeSets[i].dispose();
		}
		
		itsIndexManager.dispose();
	}
	
	public void indexThread(int aIndex, long aEventId)
	{
		itsThreadIndexSet.add(aIndex, aEventId);
	}
	
	public SimpleTree getThreadIndex(int aIndex)
	{
		return itsThreadIndexSet.getIndex(aIndex);
	}
	
	public void indexDepth(int aIndex, long aEventId)
	{
		itsDepthIndexSet.add(aIndex, aEventId);
	}
	
	public SimpleTree getDepthIndex(int aIndex)
	{
		return itsDepthIndexSet.getIndex(aIndex);
	}
	
	public void indexLocation(int aIndex, long aEventId)
	{
		itsLocationIndexSet.add(aIndex, aEventId);
	}
	
	public SimpleTree getLocationIndex(int aIndex)
	{
		return itsLocationIndexSet.getIndex(aIndex);
	}
	
	public void indexAdviceSourceId(int aIndex, long aEventId)
	{
		itsAdviceSourceIdIndexSet.add(aIndex, aEventId);
	}
	
	public SimpleTree getAdviceSourceIdIndex(int aIndex)
	{
		return itsAdviceSourceIdIndexSet.getIndex(aIndex);
	}
	
	public void indexBehavior(int aIndex, long aEventId, byte aRole)
	{
		itsBehaviorIndexSet.add(aIndex, aEventId, aRole);
	}
	
	public RoleTree getBehaviorIndex(int aIndex)
	{
		return itsBehaviorIndexSet.getIndex(aIndex);
	}
	
	public void indexField(int aIndex, long aEventId)
	{
		itsFieldIndexSet.add(aIndex, aEventId);
	}
	
	public SimpleTree getFieldIndex(int aIndex)
	{
		return itsFieldIndexSet.getIndex(aIndex);
	}
	
	public void indexVariable(int aIndex, long aEventId)
	{
		itsVariableIndexSet.add(aIndex, aEventId);
	}
	
	public SimpleTree getVariableIndex(int aIndex)
	{
		return itsVariableIndexSet.getIndex(aIndex);
	}
	
	public void indexArrayIndex(int aIndex, long aEventId)
	{
		SplittedConditionHandler.INDEXES.add(itsArrayIndexIndexSets, aIndex, aEventId, (byte) 0);
	}
	
	public SimpleTree getArrayIndexIndex(int aPart, int aPartialKey)
	{
		return itsArrayIndexIndexSets[aPart].getIndex(aPartialKey);
	}
	
	public void indexObject(Object aObject, long aEventId, byte aRole)
	{
		long theId = ObjectCodec.getObjectId(aObject, false);
		indexObject(theId, aEventId, aRole);
	}

	public void indexObject(long aIndex, long aEventId, byte aRole)
	{
		if (aIndex > Integer.MAX_VALUE) throw new RuntimeException("Object index overflow: "+aIndex);
		int theId = (int) aIndex;
		itsMaxObjectId = Math.max(itsMaxObjectId, aIndex);
		SplittedConditionHandler.OBJECTS.add(itsObjectIndexeSets, theId, aEventId, aRole);
	}
	
	public RoleTree getObjectIndex(int aPart, int aPartialKey)
	{
		return itsObjectIndexeSets[aPart].getIndex(aPartialKey);
	}
	
	@Probe(key = "max object id", aggr = AggregationType.MAX)
	public long getMaxObjectId()
	{
		return itsMaxObjectId;
	}
}
