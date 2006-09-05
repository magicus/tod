/*
 * Created on Apr 2, 2005
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
        if (aClassName.endsWith(".**")) return new RecursivePackageSet(aClassName.substring(0, aClassName.length() - 3));
        else if (aClassName.endsWith(".*")) return new SinglePackageSet(
                                                                        aClassName.substring(0, aClassName.length() - 2));
        else if (aClassName.endsWith(".")) throw new ParseException("class/package name cannot end with '.': "
                                                                    + aClassName);
        else return new SingleClassSet(aClassName);
    }
}
