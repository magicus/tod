/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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

public class ConfigUtils
{
	/**
	 * Reads a boolean from system properties.
	 */
	public static boolean readBoolean (String aPropertyName, boolean aDefault)
	{
		String theString = System.getProperty(aPropertyName);
		return theString != null ? Boolean.parseBoolean(theString) : aDefault;
	}
	
	/**
	 * Reads an int from system properties.
	 */
	public static int readInt (String aPropertyName, int aDefault)
	{
		String theString = System.getProperty(aPropertyName);
		return theString != null ? Integer.parseInt(theString) : aDefault;
	}
	
	/**
	 * Reads a string from system properties.
	 */
	public static String readString (String aPropertyName, String aDefault)
	{
		String theString = System.getProperty(aPropertyName);
		System.out.println(aPropertyName+"="+theString);
		return theString != null ? theString : aDefault;
	}
	
	/**
	 * Reads a size in bytes. Commonly used size suffixes can be used:
	 * k for kilo, m for mega, g for giga
	 */
	public static long readSize (String aPropertyName, String aDefault)
	{
		String theValue = readString(aPropertyName, aDefault);
		long theFactor = 1;
		if (theValue.endsWith("k")) theFactor = 1024;
		else if (theValue.endsWith("m")) theFactor = 1024*1024;
		else if (theValue.endsWith("g")) theFactor = 1024*1024*1024;
		if (theFactor != 1) theValue = theValue.substring(0, theValue.length()-1);
		
		return Long.parseLong(theValue)*theFactor;
	}
	

}
