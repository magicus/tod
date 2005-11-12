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
	
	/**
	 * Two location info are equal if they have the same class and
	 * the same id.
	 */
	@Override
	public final boolean equals(Object aObj)
	{
		if (aObj instanceof LocationInfo)
		{
			LocationInfo theInfo = (LocationInfo) aObj;
			return theInfo.getClass().equals(getClass()) && theInfo.getId() == getId();
		}
		else return false;
	}
}
