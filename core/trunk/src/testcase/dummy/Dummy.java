/*
 * Created on Aug 28, 2006
 */
package dummy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;

public class Dummy
{
	public static void main(String[] args)
	{
		Object theObject = new Object();
		
		int j;
		
		Object[] theObjects = new Object[100];
		for(int i=0;i<theObjects.length;i++) theObjects[i] = new Object();
		
		Random theRandom = new Random(0);
		for(int i=0;i<10000000;i++)
		{
			j = i*2;
			foo(theObjects[theRandom.nextInt(theObjects.length)], j);
			if (i % 100000 == 0) System.out.println(i);
		}
	}
	
	public static int foo(Object o, long b)
	{
		long c = o.hashCode()+b;
		return (int)(c/2);
	}
}
