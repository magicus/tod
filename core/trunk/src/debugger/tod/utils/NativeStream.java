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
package tod.utils;

import java.io.DataInputStream;
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

	public static void b2i(byte[] aSrc, int[] aDest)
	{
		b2i(aSrc, 0, aDest, 0, aSrc.length);
	}
	
	
	/**
	 * Copies the data from the source byte array to the dest int array
	 * @param aSrcOffset Offset of the first source array slot.
	 * @param aDestOffset Offset of the first dest array slot.
	 * @param aLength Number of bytes to copy.
	 */
	public static native void b2i(
			byte[] aSrc,
			int aSrcOffset,
			int[] aDest,
			int aDestOffset,
			int aLength);
		
	public static void i2b(int[] aSrc, byte[] aDest)
	{
		i2b(aSrc, 0, aDest, 0, aSrc.length*4);
	}
	
	/**
	 * Copies the data from the source int array to the dest byte array
	 * @param aSrcOffset Offset of the first source array slot.
	 * @param aDestOffset Offset of the first dest array slot.
	 * @param aLength Number of bytes to copy.
	 */
	public static native void i2b(
			int[] aSrc,
			int aSrcOffset,
			byte[] aDest,
			int aDestOffset,
			int aLength);
	
	/**
	 * Reads an int from the given byte array and returns it as an int.
	 * Byte ordering is safe (compatible with {@link DataInputStream}).
	 */
	public static native int ba2i(byte[] aSrc);
	
	/**
	 * Writes an int to the given byte array.
	 * WARNING: NO CHECK IS PERFORMED ON THE ARRAY (SIZE, NULL...)
	 * Byte ordering is safe (compatible with {@link DataInputStream}).
	 */
	public static native void i2ba(int aValue, byte[] aDest);

	
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
