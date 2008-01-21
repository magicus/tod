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
package tod.gui.formatter;

import java.io.IOException;

import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;

import zz.utils.Utils;

public class CustomFormatter
{
	public static void main(String[] args) 
	{
		IPyObjectFormatter theFormatter = getInstance().createFormatter("return 'x: '+o.x+', y: '+o.y");
		System.out.println(theFormatter.format(new ReconstitutedObject(null, 0)));

	}
	
	private static CustomFormatter INSTANCE = new CustomFormatter();

	public static CustomFormatter getInstance()
	{
		return INSTANCE;
	}
	
	private IPyFormatterFactory itsFactory;
	private PythonInterpreter itsInterpreter;

	private CustomFormatter()
	{
		try
		{
			System.setProperty("python.home", "/home/gpothier/tmp/tod");
			String theScript = Utils.readInputStream(CustomFormatter.class.getResourceAsStream("formatter.py"));
			itsInterpreter = new PythonInterpreter();
			itsInterpreter.setLocals(new PyStringMap());
			itsInterpreter.exec(theScript);
			
			PyObject f = itsInterpreter.get("factory");
			itsFactory = (IPyFormatterFactory) f.__tojava__(IPyFormatterFactory.class);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public IPyObjectFormatter createFormatter(String aFunctionBody)
	{
		String theDef = "def func(o):\n\t"+aFunctionBody;
		itsInterpreter.exec(theDef);
		PyFunction theFunction = (PyFunction) itsInterpreter.get("func");
		
		return itsFactory.create(theFunction);
	}
}
