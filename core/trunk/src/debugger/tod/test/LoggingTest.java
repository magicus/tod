/*
 * Created on Sep 29, 2004
 */
package tod.test;

/**
 * @author gpothier
 */
public class LoggingTest 
{
	private int itsLastCompute;
	
    public static void main(String[] args) throws Throwable
    {
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
