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

import java.io.DataInputStream;

public class ArrayCast
{
	static
	{
		System.loadLibrary("array-cast");
	}

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

	

}
