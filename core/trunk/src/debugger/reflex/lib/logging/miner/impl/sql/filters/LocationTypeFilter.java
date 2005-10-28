/*
 * Created on Nov 26, 2004
 */
package reflex.lib.logging.miner.impl.sql.filters;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.tables.Column;
import reflex.lib.logging.miner.impl.sql.tables.Events;

/**
 * A filter that accepts only events that have a specific location type.
 * It defines constant instance for the existing location types.
 * @author gpothier
 */
public class LocationTypeFilter extends ColumnFilter
{
	public static final LocationTypeFilter BEHAVIOUR = new LocationTypeFilter (Queries.LOCATION_TYPE_BEHAVIOUR);
	public static final LocationTypeFilter FIELD = new LocationTypeFilter (Queries.LOCATION_TYPE_FIELD);
	public static final LocationTypeFilter TYPE = new LocationTypeFilter (Queries.LOCATION_TYPE_TYPE);
	
	private LocationTypeFilter(byte aLocationType)
	{
		super(Events.LOCATION_TYPE, ""+aLocationType);
	}
}
