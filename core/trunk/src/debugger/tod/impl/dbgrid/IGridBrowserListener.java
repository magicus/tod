/*
 * Created on Nov 3, 2006
 */
package tod.impl.dbgrid;

import tod.impl.dbgrid.monitoring.Monitor.MonitorData;

/**
 * Interface for listeners of a {@link GridLogBrowser}.
 * For now it is just a relay of {@link RIGridMasterListener}
 * that permits to keep {@link GridLogBrowser} as unique remote
 * listener that dispatches events to local listeners.
 * @author gpothier
 */
public interface IGridBrowserListener
{
	/**
	 * See {@link RIGridMasterListener#monitorData(int, MonitorData)}
	 */
	public void monitorData(int aNodeId, MonitorData aData);

}
