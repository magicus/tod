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

import java.io.FileInputStream;
import java.io.IOException;

import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;

import zz.utils.Utils;

public class JythonTest
{
	public static void main(String[] args) throws IOException
	{
		System.setProperty("python.home", "/home/gpothier/tmp/tod");
		String theScript = Utils.readInputStream(JythonTest.class.getResourceAsStream("getattr.py"));
		PythonInterpreter interp = new PythonInterpreter();
		
		interp.setLocals(new PyStringMap());
		interp.exec(theScript);
		
		interp.exec("import sys");
		interp.exec("print sys");
		interp.set("a", new PyInteger(42));
		interp.exec("print a");
		interp.exec("x = 2+2");
		PyObject x = interp.get("x");
		System.out.println("x: " + x);
		interp.setLocals(new PyStringMap());
		x = interp.get("x");
		System.out.println("x: " + x);
	}
	
	public static void foo()
	{
		System.out.println("JythonTest.foo()?");
	}
}
