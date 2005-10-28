/*
 * Created on Oct 13, 2004
 */
package reflex.lib.logging.test;

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
		if (itsValue == 0) cannotDivide();
		return aValue / itsValue;
	}
	
	private void cannotDivide()
	{
		throw new RuntimeException("We cannot divide: value is 0!!");
	}
}
