package tod.bench.overhead;


import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;

public class Dummy
{
	public static final String RESULT_PREFIX = "EXEC: ";
	
	public static void main(String[] args)
	{
		System.out.println("Dummy");
		Object theObject = new Object();
		
		int j = 0;
		
		long t0 = System.currentTimeMillis();
		
		Object[] theObjects = new Object[100];
		for(int i=0;i<theObjects.length;i++) theObjects[i] = new Object();
		
		Random theRandom = new Random(0);
		for(int i=0;i<10000000;i++)
		{
			j += (i*2)%10;
			foo(theObjects[theRandom.nextInt(theObjects.length)], j);
			if (i % 1000000 == 0) System.out.println(i);
		}
		
		long t1 = System.currentTimeMillis();
		System.out.println("j: "+j);
		System.out.println(RESULT_PREFIX+(t1-t0));
	}
	
	public static int foo(Object o, long b)
	{
		long c = o.hashCode()+b;
		return (int)(c/2);
	}
}
