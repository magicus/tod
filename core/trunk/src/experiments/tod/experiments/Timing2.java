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
package tod.experiments;

import java.io.IOException;

public class Timing2
{
	public static void main(String[] args) throws IOException
	{
		System.out.println("Hello World");
		
		long t0 = System.currentTimeMillis();
		int j = 1;
		for(int i=0;i<100000000;i++)
		{
			j = j*i + i + foo(j);
		}
		long t1 = System.currentTimeMillis();
		
		System.out.println(t1-t0);
		System.out.println(j);
	}
	
	private static int foo(int i)
	{
		return i*2;
	}

}
