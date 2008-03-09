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
package tod.agent;

import java.io.PrintStream;

/**
 * This enumeration permits to identify one of the standard outputs
 * (out or err).
 * @author gpothier
 */
public enum Output  
{
	OUT
	{
		public PrintStream get()
		{
			return System.out;
		}
		
		public void set(PrintStream aStream)
		{
			System.setOut(aStream);
		}
	}, 
	ERR
	{
		public PrintStream get()
		{
			return System.err;
		}
		
		public void set(PrintStream aStream)
		{
			System.setErr(aStream);
		}
	};

	
	public abstract PrintStream get();
	public abstract void set(PrintStream aStream);
	
	/**
	 * Cached values; call to values() is costly. 
	 */
	public static final Output[] VALUES = values();
}
