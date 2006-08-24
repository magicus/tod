/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid.aggregator;

import tod.core.database.browser.ILogBrowser;
import tod.impl.dbgrid.RIGridMaster;

/**
 * Implementation of {@link ILogBrowser} for the grid backend.
 * @author gpothier
 */
public class GridLogBrowser implements ILogBrowser
{
	private RIGridMaster itsMaster;

	public GridLogBrowser(RIGridMaster aMaster)
	{
		itsMaster = aMaster;
	}
	
}
