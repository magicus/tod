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
package dummy;

import tod.agent.TOD;

public class Dummy2
{
	public static void main(String[] args)
	{
		for(int i=0;i<10;i++)
		{
			System.out.println(i);
			dummy1();
			System.out.println("Clearing DB...");
			TOD.clearDatabase();
		}
	}
	
	public static void dummy1()
	{
		for(int i=0;i<1000;i++) 
		{
			foo(i);
		}
	}
	
	public static void foo(int i)
	{
		int j = i*2;
	}
	
	public static class Moo
	{
		private String s;

		public Moo(String aS)
		{
			s = aS;
		}
		
	}
}