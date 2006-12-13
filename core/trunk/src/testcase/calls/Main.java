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
package calls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main
{
	private static int toto = 9;
	private int titi = 10;
	
	public static void staticA() throws Throwable
	{
		System.out.println("hoho");
		throw new Throwable();
	}
	
	public void compare()
	{
		List<String> theList = new ArrayList<String>();
		Object o = theList;
		theList.add("A");
		theList.add("B");
		Collections.sort(theList, new A());
		
		synchronized (this)
		{
			String s = o.toString();
		}
	}
	
	public static void main(String[] args)
	{
		for (int i=0;i<1000;i++)
		{
			try
			{
				staticA();
			}
			catch (Throwable e)
			{
			}
			Main theMain = new Main();
			theMain.compare();
		}
	}
	
	private static class A implements Comparator<String>
	{
		public int compare(String aO1, String aO2)
		{
			return aO1.compareToIgnoreCase(aO2);
		}
	}
}
