/*
 * Created on Oct 27, 2005
 */
package tod.test;

import tod.core.BehaviorCallType;

public class A
{
	public A (A a)
	{
		System.out.println(BehaviorCallType.METHOD_CALL);
		int i = 987349827;
	}
}
