/*
 * Created on Oct 27, 2005
 */
package tod.test;

public class B extends A
{
	public B(int i)
	{
		super (new A(null));
	}
	
	public B(long l)
	{
		super(new B(0));
		new A(this);
	}
	
	private static void foo()
	{
	}
	
}
