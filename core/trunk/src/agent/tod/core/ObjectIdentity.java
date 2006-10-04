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
	 * Returns a positive value if the object was already tagged.
	 * If this call causes the object to be tagged, the opposite of 
	 * the actual tag value is returned.
	 */
	public static native long get (Object aObject);
}
