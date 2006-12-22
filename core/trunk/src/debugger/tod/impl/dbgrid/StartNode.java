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
package tod.impl.dbgrid;

import java.rmi.RemoteException;

import tod.core.config.TODConfig;
import tod.impl.dbgrid.dispatcher.InternalEventDispatcher;
import tod.impl.dbgrid.gridimpl.GridImpl;

/**
 * Main class for starting nodes of the dispatching tree.
 * Nodes are created as leaf dispatcher, internal dispatcher or
 * database node according to the task id.
 * @author gpothier
 */
public class StartNode
{
	public static void main(String[] args) throws RemoteException
	{
		String theMasterHost = args[0];
		int theExpectedNodes = Integer.parseInt(args[1]);
		int theTaskId = Integer.parseInt(System.getProperty("task-id"));
		
		System.out.println(String.format(
				"StartNode [%d]: expecting %d database nodes.",
				theTaskId,
				theExpectedNodes));
		
		DispatchTreeStructure theStructure = DispatchTreeStructure.compute(theExpectedNodes);
		System.out.println(theStructure);

		TODConfig theConfig = new TODConfig();
		
		theTaskId -= 2; //first node has task id == 2
		if (theTaskId < theStructure.databaseNodes)
		{
			System.out.println("Starting database node.");
			GridImpl.getFactory(theConfig).createNode(true);
			return;
		}
		
		theTaskId -= theStructure.databaseNodes;
		
		if (theTaskId < theStructure.leafNodes)
		{
			System.out.println("Starting leaf dispatcher.");
			GridImpl.getFactory(theConfig).createLeafDispatcher(true);
			return;
		}
		
		theTaskId -= theStructure.leafNodes;

		if (theTaskId < theStructure.internalNodes)
		{
			System.out.println("Starting internal dispatcher.");
			new InternalEventDispatcher(true);
			return;
		}

		throw new UnsupportedOperationException("Don't know what to do.");
	}
}
