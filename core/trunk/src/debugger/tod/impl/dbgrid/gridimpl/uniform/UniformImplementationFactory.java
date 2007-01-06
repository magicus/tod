/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.impl.dbgrid.gridimpl.uniform;

import java.rmi.RemoteException;

import tod.core.database.browser.ILocationStore;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.dispatch.LeafEventDispatcher;
import tod.impl.dbgrid.gridimpl.IGridImplementationFactory;

public class UniformImplementationFactory implements IGridImplementationFactory
{
	public LeafEventDispatcher createLeafDispatcher(boolean aConnectToMaster, ILocationStore aLocationStore)
	{
		try
		{
			UniformEventDispatcher theDispatcher = new UniformEventDispatcher(aLocationStore);
			if (aConnectToMaster) theDispatcher.connectToMaster();
			return theDispatcher;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public DatabaseNode createNode(boolean aConnectToMaster)
	{
		try
		{
			UniformDatabaseNode theNode = new UniformDatabaseNode();
			if (aConnectToMaster) theNode.connectToMaster();
			return theNode;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}