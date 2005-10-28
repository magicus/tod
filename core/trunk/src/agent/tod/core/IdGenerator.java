/*
 * Created on Oct 24, 2005
 */
package tod.core;


public class IdGenerator
{
	private static long itsCurrentLongId = 1l+Integer.MAX_VALUE;
	private static int itsCurrentIntId = 1;

	/**
	 * This method is used in introduced field's initializer.
	 * We make it static to limit overhead.
	 */
	public static synchronized long createLongId()
	{
//		System.out.println("ObjectIdentifier.createLongId()");
//		if (itsCurrentLongId == Long.MAX_VALUE) throw new RuntimeException("Long ids exhausted");
		return itsCurrentLongId++;
	}
	
	/**
	 * This method is used in introduced field's initializer.
	 * We make it static to limit overhead.
	 */
	public static synchronized int createIntId()
	{
//		System.out.println("ObjectIdentifier.createIntId()");
//		if (itsCurrentIntId == Integer.MAX_VALUE) throw new RuntimeException("Int ids exhausted");
		return itsCurrentIntId++;
	}
	

}
