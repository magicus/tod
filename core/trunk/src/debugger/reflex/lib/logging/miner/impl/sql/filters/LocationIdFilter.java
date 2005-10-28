/*
 * Created on Nov 26, 2004
 */
package reflex.lib.logging.miner.impl.sql.filters;

import reflex.lib.logging.miner.impl.sql.tables.Column;
import reflex.lib.logging.miner.impl.sql.tables.Events;

/**
 * A filter based on the location id column
 * @author gpothier
 */
public class LocationIdFilter extends ColumnFilter
{
	public LocationIdFilter(int aLocationId)
	{
		super(Events.LOCATION_ID, ""+aLocationId);
	}
}
