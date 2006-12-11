/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
