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
package tod.tools.parsers.workingset;

import java.io.StringReader;

import tod.tools.parsers.ParseException;
import tod.tools.parsers.WorkingSetParser;


/**
 * This class parses working sets.
 * 
 * 
 * Pseudo Grammar for working set parsing is:
 *  _classSet = _simpleSet | _coumpoundSet
 *  _simpleSet = _className   -> new SimpleClassSet(_className)
 *  _className =  id(.id)*(."*"("*")?)?
 *  _coumpoundSet =  [ (_op)+ ]   -> new CoupoundSet(_op)
 *  _op = "+" classSet (":" classSet)*    -> new  SetOperation("+", listOfClassSet)
 *        | -> new  SetOperation("+", listOfClassSet)
 * 
 * 
 *   If the first operation is an include, default is accept nothing
 *   If the first operation is an exclude, default is accept all.
 * 
 * 
 * @author gpothier
 */
public class WorkingSetFactory
{
    static
    {
        new WorkingSetParser(new StringReader(""));
    }

    public static AbstractClassSet parseWorkingSet(String aString) throws ParseException
    {
        WorkingSetParser.ReInit(new StringReader(aString));
        return WorkingSetParser.classSet();
    }

    /**
     * Creates an appropriate class set for the given class name.
     */
    public static AbstractClassSet createClassSet(String aClassName) throws ParseException
    {
        if (aClassName.endsWith(".**")) 
        	return new RecursivePackageSet(aClassName.substring(0, aClassName.length() - 3));
        else if (aClassName.endsWith(".*")) 
        	return new SinglePackageSet(aClassName.substring(0, aClassName.length() - 2));
        else if (aClassName.endsWith(".")) 
        	throw new ParseException("class/package name cannot end with '.': "+ aClassName);
        else return new SingleClassSet(aClassName);
    }
}
