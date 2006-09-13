/*
 * Created on Aug 28, 2006
 */
package dummy;

public class Dummy
{
	public static void main(String[] args)
	{
		int j;
		for(int i=0;i<100000;i++)
		{
			j = i*2;
			foo(i, j);
			if (i % 100000 == 0) System.out.println(i);
		}
	}
	
	public static int foo(int a, long b)
	{
		long c = a+b;
		return (int)(c/2);
	}
}
