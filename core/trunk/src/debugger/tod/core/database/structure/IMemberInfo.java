/*
 * Created on Nov 22, 2005
 */
package tod.core.database.structure;

public interface IMemberInfo extends ILocationInfo
{
	/**
	 * The type that declares this member.
	 */
	public IClassInfo getType();

}