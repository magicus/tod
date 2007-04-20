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
package tod.impl.dbgrid.db;

import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_ARRAY_INDEX_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BEHAVIOR_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BYTECODE_LOCS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_DEPTH_RANGE;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_FIELD_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_OBJECT_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_THREADS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_TYPE_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_VAR_COUNT;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.SplittedConditionHandler;
import tod.impl.dbgrid.db.IndexSet.IndexManager;
import tod.impl.dbgrid.db.RoleIndexSet.RoleTuple;
import tod.impl.dbgrid.db.StdIndexSet.StdTuple;
import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.messages.ObjectCodec;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;

/**
 * Groups all the indexes maintained by a database node.
 * @author gpothier
 */
public class Indexes
{
	private IndexManager itsIndexManager = new IndexManager();	
	
	private StdIndexSet itsTypeIndex;
	private StdIndexSet itsThreadIndex;
	private StdIndexSet itsDepthIndex;
	private StdIndexSet itsLocationIndex;
	private RoleIndexSet itsBehaviorIndex;
	private StdIndexSet itsFieldIndex;
	private StdIndexSet itsVariableIndex;
	
	/**
	 * Index for array indexes 
	 */
	private StdIndexSet[] itsArrayIndexIndexes;
	private ObjectIndexSet[] itsObjectIndexes;
	
	private long itsMaxObjectId = 0;

	
	/**
	 * Protected constructor for subclasses. Does not initialize indexes.
	 */
	protected Indexes()
	{
	}
	
	public Indexes(HardPagedFile aFile)
	{
		Monitor.getInstance().register(this);
		
		itsTypeIndex = new StdIndexSet("type", itsIndexManager, aFile, STRUCTURE_TYPE_COUNT+1);
		itsThreadIndex = new StdIndexSet("thread", itsIndexManager, aFile, STRUCTURE_THREADS_COUNT+1);
		itsDepthIndex = new StdIndexSet("depth", itsIndexManager, aFile, STRUCTURE_DEPTH_RANGE+1);
		itsLocationIndex = new StdIndexSet("bytecodeLoc.", itsIndexManager, aFile, STRUCTURE_BYTECODE_LOCS_COUNT+1);
		itsBehaviorIndex = new RoleIndexSet("behavior", itsIndexManager, aFile, STRUCTURE_BEHAVIOR_COUNT+1);
		itsFieldIndex = new StdIndexSet("field", itsIndexManager, aFile, STRUCTURE_FIELD_COUNT+1);
		itsVariableIndex = new StdIndexSet("variable", itsIndexManager, aFile, STRUCTURE_VAR_COUNT+1);

		
		itsArrayIndexIndexes = new StdIndexSet[DebuggerGridConfig.INDEX_ARRAY_INDEX_PARTS.length];
		for (int i=0;i<itsArrayIndexIndexes.length;i++)
		{
			itsArrayIndexIndexes[i] = new StdIndexSet("index-"+i, itsIndexManager, aFile, STRUCTURE_ARRAY_INDEX_COUNT+1);
		}
		
		itsObjectIndexes = new ObjectIndexSet[DebuggerGridConfig.INDEX_OBJECT_PARTS.length];
		for (int i=0;i<itsObjectIndexes.length;i++)
		{
			itsObjectIndexes[i] = new ObjectIndexSet("object-"+i, itsIndexManager, aFile, STRUCTURE_OBJECT_COUNT+1);
		}
	}
	
	/**
	 * Unregister all the indexes from the monitor.
	 */
	public void unregister()
	{
		Monitor.getInstance().unregister(this);

		itsTypeIndex.unregister();
		itsThreadIndex.unregister();
		itsDepthIndex.unregister();
		itsLocationIndex.unregister();
		itsBehaviorIndex.unregister();
		itsFieldIndex.unregister();
		itsVariableIndex.unregister();

		for (int i=0;i<itsArrayIndexIndexes.length;i++)
		{
			itsArrayIndexIndexes[i].unregister();
		}
		
		for (int i=0;i<itsObjectIndexes.length;i++)
		{
			itsObjectIndexes[i].unregister();
		}
	}
	
	public void indexType(int aIndex, StdTuple aTuple)
	{
		itsTypeIndex.addTuple(aIndex, aTuple);
	}
	
	public HierarchicalIndex<StdTuple> getTypeIndex(int aIndex)
	{
		return itsTypeIndex.getIndex(aIndex);
	}
	
	public void indexThread(int aIndex, StdTuple aTuple)
	{
		itsThreadIndex.addTuple(aIndex, aTuple);
	}
	
	public HierarchicalIndex<StdTuple> getThreadIndex(int aIndex)
	{
		return itsThreadIndex.getIndex(aIndex);
	}
	
	public void indexDepth(int aIndex, StdTuple aTuple)
	{
		itsDepthIndex.addTuple(aIndex, aTuple);
	}
	
	public HierarchicalIndex<StdTuple> getDepthIndex(int aIndex)
	{
		return itsDepthIndex.getIndex(aIndex);
	}
	
	public void indexLocation(int aIndex, StdTuple aTuple)
	{
		itsLocationIndex.addTuple(aIndex, aTuple);
	}
	
	public HierarchicalIndex<StdTuple> getLocationIndex(int aIndex)
	{
		return itsLocationIndex.getIndex(aIndex);
	}
	
	public void indexBehavior(int aIndex, RoleTuple aTuple)
	{
		itsBehaviorIndex.addTuple(aIndex, aTuple);
	}
	
	public HierarchicalIndex<RoleTuple> getBehaviorIndex(int aIndex)
	{
		return itsBehaviorIndex.getIndex(aIndex);
	}
	
	public void indexField(int aIndex, StdTuple aTuple)
	{
		itsFieldIndex.addTuple(aIndex, aTuple);
	}
	
	public HierarchicalIndex<StdTuple> getFieldIndex(int aIndex)
	{
		return itsFieldIndex.getIndex(aIndex);
	}
	
	public void indexVariable(int aIndex, StdTuple aTuple)
	{
		itsVariableIndex.addTuple(aIndex, aTuple);
	}
	
	public HierarchicalIndex<StdTuple> getVariableIndex(int aIndex)
	{
		return itsVariableIndex.getIndex(aIndex);
	}
	
	public void indexArrayIndex(int aIndex, StdTuple aTuple)
	{
		SplittedConditionHandler.INDEXES.index(aIndex, aTuple, itsArrayIndexIndexes);
	}
	
	public HierarchicalIndex<StdTuple> getArrayIndexIndex(int aPart, int aPartialKey)
	{
		return itsArrayIndexIndexes[aPart].getIndex(aPartialKey);
	}
	
	public void indexObject(Object aObject, RoleTuple aTuple)
	{
		long theId = ObjectCodec.getObjectId(aObject, false);
		indexObject(theId, aTuple);
	}

	public void indexObject(long aIndex, RoleTuple aTuple)
	{
		itsMaxObjectId = Math.max(itsMaxObjectId, aIndex);
		SplittedConditionHandler.OBJECTS.index(aIndex, aTuple, itsObjectIndexes);
	}
	
	public HierarchicalIndex<RoleTuple> getObjectIndex(int aPart, int aPartialKey)
	{
		return itsObjectIndexes[aPart].getIndex(aPartialKey);
	}
	
	@Probe(key = "max object id", aggr = AggregationType.MAX)
	public long getMaxObjectId()
	{
		return itsMaxObjectId;
	}
}
