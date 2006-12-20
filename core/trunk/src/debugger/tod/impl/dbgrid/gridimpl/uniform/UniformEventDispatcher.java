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
package tod.impl.dbgrid.gridimpl.uniform;

import java.net.Socket;

import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dispatcher.DBNodeProxy;
import tod.impl.dbgrid.dispatcher.EventDispatcher;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * This implementation of the dispatcher forwards each received event
 * to one node, in a round robin fashion.
 * @author gpothier
 */
public class UniformEventDispatcher extends EventDispatcher
{
	private int itsCurrentNode = 0;
	
	public UniformEventDispatcher(GridMaster aMaster)
	{
		super(aMaster);
	}
	
	@Override
	protected DBNodeProxy createProxy(Socket aSocket, int aId)
	{
		return new UniformDBNodeProxy(aSocket, aId, getMaster());
	}

	@Override
	protected UniformDBNodeProxy getNode(int aIndex)
	{
		return (UniformDBNodeProxy) super.getNode(aIndex);
	}
	
	@Override
	protected void dispatchEvent0(GridEvent aEvent)
	{
		UniformDBNodeProxy theProxy = getNode(itsCurrentNode);
		theProxy.pushEvent(aEvent);
		
		// The following code is 5 times faster than using a modulo.
		// (Pentium M 2ghz)
		itsCurrentNode++;
		if (itsCurrentNode >= getNodesCount()) itsCurrentNode = 0;
	}
	
}
