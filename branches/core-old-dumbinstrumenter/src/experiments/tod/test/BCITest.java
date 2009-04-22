/*
 * Created on Oct 24, 2005
 */
package tod.test;

import java.util.ArrayList;

import javax.swing.JPanel;

import tod.core.ObjectIdentity;


public class BCITest
{
	public static void main(String[] args)
	{
		new JPanel();
		BCITest theTest = new BCITest(50, 123);
		int k = theTest.ex(0);
		long j = 90;
		
		
		theTest.foo(1, 2, 0.5f, 452.1, "ro");
		theTest.foo(10, 20, 10.5f, 4520.1, "RO");
		print ("toto");
		print ("titi");
		print (new ArrayList());
		print (theTest);
		print (theTest);
		theTest.foo(1, 2, 0.5f, 452.1, "ru");
		theTest.tcf(0);
		theTest.tcf(1);
		
		throw new RuntimeException();
	}
	
	private int itsInt;
	private long itsLong;
	
	public BCITest(int aI, long aL)
	{
		itsInt = aI;
		itsLong = aL;
	}

	public void tcf(int i)
	{
		try
		{
			if (i == 0) return;
			else System.out.println(i);
		}
		finally
		{
			System.out.println("Finally"+itsInt);
		}
		System.out.println("finish"+itsLong);
	}
	
	public static void print (Object aObject)
	{
		System.out.println("Object: "+ObjectIdentity.get(aObject));
	}
	
	private void foo (int i, long l, float f, double d, String s)
	{
		System.out.println(""+i+" "+l+" "+f+" "+d+" "+s);
	}
	
	private int ex(int n)
	{
		return 100/n;
	}
}
