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
import java.io.IOException;
import java.net.Socket;

import tod.core.transport.MessageType;
import zz.utils.Utils;

public class DispatcherProxy extends DispatchNodeProxy
{
	private byte[] itsBuffer = new byte[1024];
	
	public DispatcherProxy(RIDispatchNode aConnectable, Socket aSocket, String aNodeId)
	{
		super(aConnectable, aSocket, aNodeId);
	}
	
	/**
	 * Transfers a packet to the child node.
	 */
	public void forwardPacket(MessageType aType, DataInputStream aStream)
	{
		try
		{
			getOutStream().writeByte((byte) aType.ordinal());
			int theSize = aStream.readInt();
			getOutStream().writeInt(theSize);

			Utils.pipe(itsBuffer, aStream, getOutStream(), theSize);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void clear()
	{
		try
		{
			getOutStream().flush();
			getConnectable().flush();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public int flush()
	{
		try
		{
			getOutStream().flush();
			return getConnectable().flush();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
}
