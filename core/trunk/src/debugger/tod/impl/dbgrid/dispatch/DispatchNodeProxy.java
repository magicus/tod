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
package tod.impl.dbgrid.dispatch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Proxy for nodes in the dispatching tree (dispatchers or database nodes)
 * @author gpothier
 */
public abstract class DispatchNodeProxy
{
	
	private RIDispatchNode itsConnectable;
	
	private final String itsNodeId;
	
	public DispatchNodeProxy(
			RIDispatchNode aConnectable,
			String aNodeId)
	{
		itsConnectable = aConnectable;
		itsNodeId = aNodeId;
	}

	public String getNodeId()
	{
		return itsNodeId;
	}

	public RIDispatchNode getConnectable()
	{
		return itsConnectable;
	}

	protected abstract DataInputStream getInStream();

	protected abstract DataOutputStream getOutStream();


	/**
	 * Flushes possibly buffered messages, both in this proxy 
	 * and in the node.
	 */
	public abstract int flush();

	/**
	 * Requests the node to clear its database.
	 *
	 */
	public abstract void clear();
	
}
