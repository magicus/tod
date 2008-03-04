/*
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

import java.io.EOFException;
import java.io.FileDescriptor;
import java.io.IOException;

public class NativeStream
{
	private long itsFID;
	
	protected NativeStream(long aFid)
	{
		itsFID = aFid;
	}
	
	protected NativeStream()
	{
	}
	
	protected static native int fileno(long aFID);
	protected static native long fdopen(FileDescriptor aFileDescriptor, String aMode);
	protected static native void setFD(FileDescriptor aFileDescriptor, int aFD);
	protected static native int getFD(FileDescriptor aFileDescriptor);
	
	protected static native int fwrite(long aFID, int[] aBuffer, int aOffset, int aSize);
	protected static native int fread(long aFID, int[] aBuffer, int aOffset, int aSize);
	protected static native int fflush(long aFID);
	protected static native int fseek(long aFID, long aOffset, int aOrigin);
	protected static native int feof(long aFID);
	
	protected static native long fopen(String aFileName, String aMode);
	protected static native int fclose(long aFID);
	
	protected static native int recv(int aFD, int[] aBuffer, int aSize);
	protected static native int send(int aFD, int[] aBuffer, int aSize);

	static
	{
		System.loadLibrary("native-stream");
	}
	
	protected void setFID(long aFID)
	{
		itsFID = aFID;
	}
	
	public void writeInt(int aValue) throws IOException
	{
		write(new int[] {aValue});
	}
	
	public void write(int[] aBuffer) throws IOException
	{
		write(aBuffer, 0, aBuffer.length);
	}
	
	public void write(int[] aBuffer, int aOffset, int aSize) throws IOException
	{
		fwrite(itsFID, aBuffer, aOffset, aSize);
	}
	
	public int readInt() throws IOException
	{
		int[] theBuffer = new int[1];
		read(theBuffer);
		return theBuffer[0];
	}
	
	public int read(int[] aBuffer) throws IOException
	{
		return read(aBuffer, 0, aBuffer.length);
	}
	
	public int read(int[] aBuffer, int aOffset, int aSize) throws IOException
	{
		return fread(itsFID, aBuffer, aOffset, aSize);
	}
	
	public void readFully(int[] aBuffer) throws IOException
	{
		int theCount = 0;
		while (theCount < aBuffer.length)
		{
			int theResult = read(aBuffer, theCount, aBuffer.length-theCount); 
			if (theResult == 0 && feof(itsFID) != 0) throw new EOFException();
			
			theCount += theResult;
		}
	}
	
	public void flush() throws IOException
	{
		int theResult = fflush(itsFID);
		if (theResult != 0) throw new IOException("Could not flush stream");
	}
	
	public void close() throws IOException
	{
		int theResult = fclose(itsFID);
		if (theResult != 0) throw new IOException("Could not close stream");
	}
	
}
