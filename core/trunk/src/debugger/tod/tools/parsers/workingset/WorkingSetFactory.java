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
package tod.tools.parsers.workingset;

import java.io.StringReader;

import tod.tools.parsers.ParseException;
import tod.tools.parsers.WorkingSetParser;


/**
 * This class parses working sets for {@link reflex.std.run.WorkingSetHandler}
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
