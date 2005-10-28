/*
 * Created on Nov 26, 2004
 */
package tod.core;

/**
 * An enumeration of the various behaviour types.
 * @author gpothier
 */
public enum BehaviourType
{
	METHOD("method"), 
	STATIC_METHOD("static method"), 
	CONSTRUCTOR("constructor"), 
	STATIC_BLOCK("static block");
	
	private String itsName;
	
	BehaviourType (String aName)
	{
		itsName = aName;
	}
	
	public String getName ()
	{
		return itsName;
	}
}
