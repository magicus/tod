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
package tod.impl.dbgrid.dispatcher;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Proxy for nodes in the dispatching tree (dispatchers or database nodes)
 * @author gpothier
 */
public abstract class DispatchTreeNodeProxy
{
	private final DataOutputStream itsOutStream;
	private final DataInputStream itsInStream;

	private final int itsNodeId;
	
	public DispatchTreeNodeProxy(Socket aSocket, int aNodeId)
	{
		itsNodeId = aNodeId;
		
		try
		{
			itsOutStream = new DataOutputStream(new BufferedOutputStream(aSocket.getOutputStream()));
			itsInStream = new DataInputStream(aSocket.getInputStream());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

	}

	public int getNodeId()
	{
		return itsNodeId;
	}

	protected DataInputStream getInStream()
	{
		return itsInStream;
	}

	protected DataOutputStream getOutStream()
	{
		return itsOutStream;
	}


	/**
	 * Flushes possibly buffered messages, both in this proxy 
	 * and in the node.
	 */
	public abstract void flush();

	/**
	 * Requests the node to clear its database.
	 *
	 */
	public abstract void clear();
	
}
