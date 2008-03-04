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

public class ConfigUtils
{
	/**
	 * Reads a boolean from system properties.
	 */
	public static boolean readBoolean (String aPropertyName, boolean aDefault)
	{
		String theString = System.getProperty(aPropertyName);
		TODUtils.log(1,"[TOD] "+aPropertyName+"="+theString);
		return theString != null ? Boolean.parseBoolean(theString) : aDefault;
	}
	
	/**
	 * Reads an int from system properties.
	 */
	public static int readInt (String aPropertyName, int aDefault)
	{
		String theString = System.getProperty(aPropertyName);
		TODUtils.log(1,"[TOD] "+aPropertyName+"="+theString);
		return theString != null ? Integer.parseInt(theString) : aDefault;
	}
	
	/**
	 * Reads a long from system properties.
	 */
	public static long readLong (String aPropertyName, long aDefault)
	{
		String theString = System.getProperty(aPropertyName);
		TODUtils.log(1,"[TOD] "+aPropertyName+"="+theString);
		return theString != null ? Long.parseLong(theString) : aDefault;
	}
	
	/**
	 * Reads a string from system properties.
	 */
	public static String readString (String aPropertyName, String aDefault)
	{
		String theString = System.getProperty(aPropertyName);
		TODUtils.log(1,"[TOD] "+aPropertyName+"="+theString);
		if (theString != null && theString.length() == 0) return null;
		return theString != null ? theString : aDefault;
	}
	
	/**
	 * Reads a size in bytes. Commonly used size suffixes can be used:
	 * k for kilo, m for mega, g for giga
	 */
	public static long readSize (String aPropertyName, String aDefault)
	{
		String theString = readString(aPropertyName, aDefault);
		TODUtils.log(1,"[TOD] "+aPropertyName+"="+theString);
		return readSize(theString);
	}
	
	public static long readSize(String aSize)
	{
		long theFactor = 1;
		if (aSize.endsWith("k")) theFactor = 1024;
		else if (aSize.endsWith("m")) theFactor = 1024*1024;
		else if (aSize.endsWith("g")) theFactor = 1024*1024*1024;
		if (theFactor != 1) aSize = aSize.substring(0, aSize.length()-1);
		
		return Long.parseLong(aSize)*theFactor;
	}
	

}
