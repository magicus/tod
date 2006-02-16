/*
 * Created on Oct 13, 2004
 */
package tod.core;


/**
 * Provides a helper method that retrieves the id
 * of an object.
 * @author gpothier
 */
public class ObjectIdentity
{
	/**
	 * Retrieves the identifier of an object.
	 */
	public static native long get (Object aObject);
}
