/*
 * Created on Nov 25, 2004
 */
package reflex.lib.logging.miner.impl.sql;

import reflex.lib.logging.core.api.collector.*;
import reflex.lib.logging.miner.impl.common.ObjectInspector;
import reflex.lib.logging.miner.impl.local.CFlowBrowser;
import reflex.lib.logging.miner.impl.sql.filters.*;
import reflex.lib.logging.miner.impl.sql.tables.Events;
import tod.core.LocationRegistrer;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ICFlowBrowser;
import tod.core.model.trace.ICompoundFilter;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventFilter;
import tod.core.model.trace.IObjectInspector;

import java.sql.SQLException;

/**
 * @author gpothier
 */
public class SQLBrowsableLog implements IEventTrace
{
	private final SQLCollector itsCollector;
	private final Queries itsQueries;

	public SQLBrowsableLog(Queries aQueries) throws SQLException
	{
		itsQueries = aQueries;
		itsCollector = new SQLCollector(aQueries);
	}

	public void clear()
	{
		try
		{
			itsQueries.dbInit.init();
		}
		catch (SQLException e)
		{
			throw new RuntimeException("Exception while clearing log", e);
		}
	}
	
	public long getFirstTimestamp()
	{
		throw new UnsupportedOperationException();
	}

	public long getLastTimestamp()
	{
		throw new UnsupportedOperationException();
	}

	public SQLCollector getCollector()
	{
		return itsCollector;
	}
	
	public LocationRegistrer getLocationRegistrer()
	{
		return itsCollector;
	}

	public IEventBrowser createBrowser(IEventFilter aFilter)
	{
		try
		{
			return new SQLEventBrowser(itsCollector, (SQLFilter) aFilter);
		}
		catch (SQLException e)
		{
			throw new RuntimeException("Could not create browser", e);
		}
	}

	public ICompoundFilter createUnionFilter(IEventFilter... aFilters)
	{
		return new UnionFilter(aFilters);
	}

	public ICompoundFilter createIntersectionFilter(IEventFilter... aFilters)
	{
		return new IntersectionFilter(aFilters);
	}

	public IEventFilter createBehaviorFilter(BehaviorInfo aBehaviourInfo)
	{
		return createIntersectionFilter(createBehaviorFilter(), new LocationIdFilter(aBehaviourInfo.getId()));
	}

	public IEventFilter createBehaviorFilter()
	{
		return LocationTypeFilter.BEHAVIOUR;
	}

	public IEventFilter createInstantiationFilter(ObjectId aObjectId)
	{
		return createIntersectionFilter(
				createTargetFilter(aObjectId),
				createInstantiationsFilter());
	}

	public IEventFilter createInstantiationsFilter(TypeInfo aTypeInfo)
	{
		return createIntersectionFilter(
				createTypeFilter(aTypeInfo),
				createInstantiationsFilter());
	}

	public IEventFilter createInstantiationsFilter()
	{
		return EventTypeFilter.INSTANTIATION;
	}

	public IEventFilter createFieldFilter(FieldInfo aFieldInfo)
	{
		return createIntersectionFilter(
				new ColumnFilter(Events.LOCATION_TYPE, ""+Queries.LOCATION_TYPE_FIELD),
				new ColumnFilter(Events.LOCATION_ID, ""+aFieldInfo.getId()));
	}

	public IEventFilter createTypeFilter(TypeInfo aTypeInfo)
	{
		return createIntersectionFilter(
				new ColumnFilter(Events.LOCATION_TYPE, ""+Queries.LOCATION_TYPE_TYPE),
				new ColumnFilter(Events.LOCATION_ID, ""+aTypeInfo.getId()));
	}

	public IEventFilter createFieldWriteFilter()
	{
		return EventTypeFilter.FIELD_WRITE;
	}

	public IEventFilter createTargetFilter(ObjectId aId)
	{
		if (aId instanceof ObjectId.ObjectUID)
		{
			ObjectId.ObjectUID theObjectUID = (ObjectId.ObjectUID) aId;
			return createIntersectionFilter(
					new ColumnFilter(Events.TARGET_TYPE, ""+Queries.OBJECT_TYPE_UID),
					new ColumnFilter(Events.TARGET_ID, ""+theObjectUID.getId()));
		}
		else if (aId instanceof ObjectId.ObjectHash)
		{
			ObjectId.ObjectHash theObjectHash = (ObjectId.ObjectHash) aId;
			return createIntersectionFilter(
					new ColumnFilter(Events.TARGET_TYPE, ""+Queries.OBJECT_TYPE_HASH),
					new ColumnFilter(Events.TARGET_ID, ""+theObjectHash.getHascode()));
			
		}
		else throw new RuntimeException();
	}

	public IEventFilter createArgumentFilter(ObjectId aId)
	{
		return null;
	}

	public IEventFilter createThreadFilter(ThreadInfo aThreadInfo)
	{
		return new ThreadFilter(aThreadInfo);
	}

	public IEventFilter createCallFilter(BehaviorInfo aBehaviourInfo)
	{
		return createIntersectionFilter(EventTypeFilter.BEHAVIOUR_ENTER, createBehaviorFilter(aBehaviourInfo));
	}

	public ICFlowBrowser createCFlowBrowser(ThreadInfo aThread)
	{
		return new CFlowBrowser(this, aThread);
	}

	public IObjectInspector createObjectInspector(ObjectId aObjectId)
	{
		return new ObjectInspector(this, aObjectId);
	}

	public IEventFilter createLocationFilter(TypeInfo aTypeInfo, int aLineNumber)
	{
		throw new UnsupportedOperationException();
	}

	public IEventFilter createBehaviorCallFilter()
	{
		throw new UnsupportedOperationException();
	}

	public IEventFilter createBehaviorCallFilter(BehaviorInfo aBehaviorInfo)
	{
		throw new UnsupportedOperationException();
	}

}
