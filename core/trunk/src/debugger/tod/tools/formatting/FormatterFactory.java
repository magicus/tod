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
package tod.tools.formatting;

import java.io.IOException;

import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;

import tod.core.database.browser.IObjectInspector;
import tod.gui.IGUIManager;

import zz.utils.Utils;

/**
 * This factory creates formatters for reconstituted objects.
 * A formatter is a snippet of Python code.
 * @author gpothier
 */
public class FormatterFactory
{
	public static void main(String[] args) 
	{
		String theCode = "return 'o.x: '+o.x+', o.x.y: '+o.x.y+', o.y: '+o.y";
		IPyObjectFormatter theFormatter = getInstance().createFormatter(theCode);
		System.out.println(theFormatter.format(new ReconstitutedObject(null, null)));
	}
	
	private static FormatterFactory INSTANCE = new FormatterFactory();

	public static FormatterFactory getInstance()
	{
		return INSTANCE;
	}
	
	private IPyFormatterFactory itsFactory;
	private PythonInterpreter itsInterpreter;

	private FormatterFactory()
	{
		String theTmpDir = System.getProperty("java.io.tmpdir");
		System.setProperty("python.home", theTmpDir);
		initFactory();
	}
	
	private void initFactory()
	{
		try
		{
			String theScript = Utils.readInputStream(FormatterFactory.class.getResourceAsStream("formatter.py"));
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
	
	Object createTODObject(ReconstitutedObject aObject)
	{
		return itsFactory.createTODObject(aObject);
	}
	
	Object wrap(IGUIManager aGUIManager, IObjectInspector aInspector)
	{
		return createTODObject(new ReconstitutedObject(aGUIManager, aInspector));
	}
	
	/**
	 * Creates a formatter using the provided Python code snippet. Within the snippet, the
	 * following variables are available:
	 * <li> o: a proxy to the reconstituted object. Use o.n to access the value of field n of o.
	 * Example:
	 * <code>  
	 * return "x: "+o.x+", y: "+o.y
	 * </code>
	 */
	public IPyObjectFormatter createFormatter(String aFunctionBody)
	{
//		initFactory(); // Temp. This is for debugging.
		
		String theImports = "from java.util import *\n";
		String theDef = theImports+"def func(o):\n"+Utils.indent(aFunctionBody, 1, "\t");
		itsInterpreter.exec(theDef);
		PyFunction theFunction = (PyFunction) itsInterpreter.get("func");
		
		return itsFactory.createFormatter(theFunction);
	}
	
}
