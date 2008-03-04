/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.utils;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketImpl;

public class NativeSocket
{
	private NativeStream itsInputStream;
	private NativeStream itsOutputStream;
	
	private static Method METHOD_GET_IMPL;
	private static Method METHOD_GET_FD;
	private final Socket itsSocket;
	static
	{
		try
		{
			METHOD_GET_IMPL = Socket.class.getDeclaredMethod("getImpl");
			METHOD_GET_IMPL.setAccessible(true);
			
			METHOD_GET_FD = SocketImpl.class.getDeclaredMethod("getFileDescriptor");
			METHOD_GET_FD.setAccessible(true);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public NativeSocket(Socket aSocket)
	{
		itsSocket = aSocket;
		try
		{
			SocketImpl theImpl = (SocketImpl) METHOD_GET_IMPL.invoke(aSocket);
			FileDescriptor theDescriptor = (FileDescriptor) METHOD_GET_FD.invoke(theImpl);
			
			itsInputStream = new SocketInputStream(theDescriptor);
			itsOutputStream = new SocketOutputStream(theDescriptor);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public NativeStream getInputStream()
	{
		return itsInputStream;
	}

	public NativeStream getOutputStream()
	{
		return itsOutputStream;
	}
	
	public void close() throws IOException
	{
		itsSocket.close();
	}
	
	public boolean isConnected()
	{
		return itsSocket.isConnected();
	}
	
	private class SocketInputStream extends NativeStream
	{
		private int itsFD;
		
		public SocketInputStream(FileDescriptor aDescriptor)
		{
			itsFD = getFD(aDescriptor);
		}
		
		@Override
		public void write(int[] aBuffer, int aOffset, int aSize)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int read(int[] aBuffer, int aOffset, int aSize)
		{
			if (aOffset != 0) throw new UnsupportedOperationException();
			return recv(itsFD, aBuffer, aSize);
		}
	}
	
	private class SocketOutputStream extends NativeStream
	{
		private int itsFD;
		
		public SocketOutputStream(FileDescriptor aDescriptor)
		{
			itsFD = getFD(aDescriptor);
		}
		
		@Override
		public void write(int[] aBuffer, int aOffset, int aSize) throws IOException
		{
			if (aOffset != 0) throw new UnsupportedOperationException();
			int n = send(itsFD, aBuffer, aSize);
			if (aSize*4 != n) throw new IOException("Sent only "+n+"bytes");
		}
		
		@Override
		public int read(int[] aBuffer, int aOffset, int aSize)
		{
			throw new UnsupportedOperationException();
		}
	}
	
}
