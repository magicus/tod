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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import tod.core.transport.MessageType;
import zz.utils.Utils;

public class DispatcherProxy extends DispatchTreeNodeProxy
{
	private byte[] itsBuffer = new byte[1024];
	
	public DispatcherProxy(Socket aSocket, int aNodeId)
	{
		super(aSocket, aNodeId);
	}

	public void pushByte(byte aByte)
	{
		try
		{
			getOutStream().writeByte(aByte);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void pushInt(int aInt)
	{
		try
		{
			getOutStream().writeInt(aInt);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void pushShort(short aShort)
	{
		try
		{
			getOutStream().writeShort(aShort);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Transfers a certain amount of data from the given input stream
	 * to the target dispatcher.
	 */
	public void pipe(InputStream aStream, int aCount)
	{
		try
		{
			Utils.pipe(itsBuffer, aStream, getOutStream(), aCount);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void clear()
	{
	}

	@Override
	public void flush()
	{
	}

}
