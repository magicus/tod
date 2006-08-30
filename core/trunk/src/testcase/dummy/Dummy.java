/*
 * Created on Aug 28, 2006
 */
package dummy;

public class Dummy
{
	public static void main(String[] args)
	{
		int j;
		for(int i=0;i<1000000;i++)
		{
			j = i;
			if (i % 10000 == 0) System.out.println(i);
		}
	}
}
