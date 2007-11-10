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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

import tod.core.transport.LogReceiver;
import tod.impl.database.structure.standard.HostInfo;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dispatch.AbstractEventDispatcher;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.dispatch.DispatchNodeProxy;
import tod.impl.dbgrid.dispatch.RIDispatchNode;

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
	protected void waitReady0()
	{
		try
		{
			FakeDispatcher theDispatcher = new FakeDispatcher();
			setRootDispatcher(theDispatcher);
			getDispatchers().add(theDispatcher);
		
			itsDatabaseNode.connectToLocalMaster(getMaster(), "db-0");
			
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
	
	private class FakeDispatcher extends AbstractEventDispatcher
	{
		public FakeDispatcher() throws RemoteException
		{
			super(false);
		}

		@Override
		protected DispatchNodeProxy createProxy(RIDispatchNode aConnectable, InputStream aInputStream, OutputStream aOutputStream, String aId)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		protected void connectToDispatcher(Socket aSocket)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public LogReceiver createLogReceiver(HostInfo aHostInfo, GridMaster aMaster, InputStream aInStream, OutputStream aOutStream, boolean aStartImmediately)
		{
			return itsDatabaseNode.createLogReceiver(aHostInfo, aMaster, aInStream, aOutStream, aStartImmediately);
		}

		@Override
		public synchronized void clear()
		{
			itsDatabaseNode.clear();
		}

		@Override
		public synchronized int flush()
		{
			return itsDatabaseNode.flush();
		}
	}
}
