/*
 * Created on Oct 24, 2005
 */
package tod.core;


public class IdGenerator
{
	private static long itsCurrentId = 1;

	/**
	 * This method is used in introduced field's initializer.
	 * We make it static to limit overhead.
	 */
	public static synchronized long createId()
	{
		return itsCurrentId++;
	}
}
