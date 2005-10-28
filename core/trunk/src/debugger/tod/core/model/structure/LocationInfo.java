/*
 * Created on Nov 3, 2004
 */
package tod.core.model.structure;

/**
 * Base class for aggregation of location information.
 * @author gpothier
 */
public abstract class LocationInfo
{
	private int itsId;
	private String itsName;
	
	public LocationInfo(int aId)
	{
		itsId = aId;
	}

	public LocationInfo(int aId, String aName)
	{
		itsId = aId;
		itsName = aName;
	}
	
	public int getId()
	{
		return itsId;
	}
	
	public String getName()
	{
		return itsName;
	}

	/**
	 * This is used for defered type registration.
	 */ 
	public void setName(String aName)
	{
		itsName = aName;
	}
}
