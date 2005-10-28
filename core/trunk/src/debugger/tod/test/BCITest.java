/*
 * Created on Oct 24, 2005
 */
package tod.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import tod.agent.AgentReady;
import tod.core.IIdentifiableObject;

public class BCITest
{
	public static void main(String[] args)
	{
		try
		{
			JarFile theFile = new JarFile("/home/gpothier/eclipse/workbench-3.1/TOD/lib/junit.jar");
			for (Enumeration<JarEntry> e = theFile.entries(); e.hasMoreElements();)
			{
				System.out.println(e.nextElement().getName());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		int k = 0;
		long j = 90;
		BCITest theTest = new BCITest(50, 123);
		
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
		if (aObject instanceof IIdentifiableObject)
		{
			IIdentifiableObject theObject = (IIdentifiableObject) aObject;
			long theId = theObject.__log_uid();
			System.out.println("Identifiable object: "+theId+" "+theObject);
		}
		else
		{
			System.out.println("Normal object: "+aObject);
		}
	}
	
	private void foo (int i, long l, float f, double d, String s)
	{
		System.out.println(""+i+" "+l+" "+f+" "+d+" "+s);
	}
}
