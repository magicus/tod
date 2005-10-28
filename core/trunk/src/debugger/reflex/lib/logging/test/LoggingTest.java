/*
 * Created on Sep 29, 2004
 */
package reflex.lib.logging.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author gpothier
 */
public class LoggingTest extends TestCase
{
	private int itsLastCompute;
	
    public static void main(String[] args) throws Throwable
    {
    	new HashMap();
    	new HashSet();
    	new LoggingTest().compute();
    }
    
	private void compute ()
	{
		itsLastCompute = 0;
		for (int i=0;i<10;i++)
		{
			MyClass theClass = new MyClass();
			theClass.set(i, 5);
			itsLastCompute = theClass.div(10);
		}
	}
}
