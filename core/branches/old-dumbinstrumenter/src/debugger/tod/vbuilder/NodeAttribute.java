/*
 * Created on Jul 25, 2005
 */
package tod.vbuilder;

/**
 * Describes an attribute that can be set to an {@link tod.vbuilder.IObjectNode}.
 * It can also be used as a helper for setting or retrieving the attribute's value.
 * @author gpothier
 */
public class NodeAttribute<T>
{
	private String itsName;
	
	public NodeAttribute(String aName)
	{
		itsName = aName;
	}

	public String getName()
	{
		return itsName;
	}
}
