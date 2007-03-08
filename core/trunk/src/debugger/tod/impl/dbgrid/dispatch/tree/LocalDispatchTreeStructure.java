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
package tod.impl.dbgrid.dispatch.tree;

import java.io.IOException;

import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.dispatch.EventDispatcher;
import tod.utils.pipe.PipedInputStream2;
import tod.utils.pipe.PipedOutputStream2;

/**
 * A dispatch tree structure for local, single-node situations.
 * @author gpothier
 */
public class LocalDispatchTreeStructure extends DispatchTreeStructure
{
	private final DatabaseNode itsDatabaseNode;

	public LocalDispatchTreeStructure(DatabaseNode aDatabaseNode)
	{
		super(1, 0);
		itsDatabaseNode = aDatabaseNode;
	}
	
	@Override
	public void waitReady(GridMaster aMaster)
	{
		try
		{
			EventDispatcher theDispatcher = new EventDispatcher();
			setRootDispatcher(theDispatcher);
			getDispatchers().add(theDispatcher);
		
			PipedInputStream2 theDispatcherIn = new PipedInputStream2();
			PipedInputStream2 theNodeIn = new PipedInputStream2();
			PipedOutputStream2 theDispatcherOut = new PipedOutputStream2(theNodeIn);
			PipedOutputStream2 theNodeOut = new PipedOutputStream2(theDispatcherIn);
			
			theDispatcher.acceptChild(
					"db-0", 
					itsDatabaseNode, 
					theDispatcherIn, 
					theDispatcherOut);
			
			itsDatabaseNode.connectToLocalDispatcher(theNodeIn, theNodeOut);
			
			itsDatabaseNode.connectToLocalMaster(aMaster, "db-0");
			theDispatcher.connectToLocalMaster(aMaster, "leaf-0");
			
			getDatabaseNodes().add(itsDatabaseNode);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public NodeRole getRoleForNode0(String aHostName)
	{
		return null;
	}

	@Override
	public int total()
	{
		return 0;
	}
	
	
}
