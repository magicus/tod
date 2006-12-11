/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package btree;
/*
 * Created on May 1, 2006
 */

public class Config
{
	/**
	 * Branching factor of the BTree.
	 */
	private static int t = 0;
	
	public static void setT(int aT)
	{
		t = aT;
	}
	
	public static int t()
	{
		return t;
	}
	
	public static int maxKeys()
	{
		return 2*t() - 1;
	}
	
	public static int minKeys()
	{
		return t() - 1;
	}
	
	public static int maxChildren()
	{
		return 2*t();
	}
	
	public static int minChildren()
	{
		return t();
	}
	
	public static int pageSize()
	{
		return 4 // keys count
			+ 1 // leaf flag
			+ 16 * maxKeys() // keys and values
			+ 4 * maxChildren(); // children pointers
	}
}
