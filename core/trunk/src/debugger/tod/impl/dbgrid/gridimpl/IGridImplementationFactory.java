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
package tod.impl.dbgrid.gridimpl;

import tod.core.database.browser.ILocationStore;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.dispatch.LeafEventDispatcher;

/**
 * Factory used to obtain the implementations of grid elements
 * such as event dispatcher, database nodes, query aggregator.
 * @author gpothier
 */
public interface IGridImplementationFactory
{
	/**
	 * Creates a new leaf dispatcher for this grid implementation.
	 */
	public LeafEventDispatcher createLeafDispatcher(
			boolean aConnectToMaster, 
			ILocationStore aLocationStore);
	
	public DatabaseNode createNode(boolean aConnectToMaster);
}
