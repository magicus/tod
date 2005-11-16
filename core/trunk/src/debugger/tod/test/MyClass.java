/*
 * Created on Oct 13, 2004
 */
package tod.test;

/**
 * @author gpothier
 */
public class MyClass
{
	private int itsValue;

	public void set (int aInt1, int aInt2)
	{
		itsValue = aInt1 - aInt2;
	}
	
	public int div (int aValue)
	{
		return aValue / itsValue;
	}
}
