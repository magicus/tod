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
package tod.test;

import java.util.ArrayList;

import javax.swing.JPanel;

/**
 * @author gpothier
 */
public class LoggingTest 
{
	private int itsLastCompute;
	
    public static void main(String[] args) throws Throwable
    {
    	new JPanel();
    	ArrayList<Object> theList = new ArrayList<Object>();
    	new LoggingTest().compute();
    }
    
	private void compute ()
	{
		itsLastCompute = 0;
		for (int i=0;i<10;i++)
		{
			System.out.println("Trying with i="+i);
			MyClass theClass = new MyClass();
			theClass.set(i, 5);
			itsLastCompute = theClass.div(10);
		}
	}
}
