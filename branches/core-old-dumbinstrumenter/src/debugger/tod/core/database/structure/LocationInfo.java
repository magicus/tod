/*
 * Created on Nov 3, 2004
 */
package tod.core.database.structure;

import tod.core.database.browser.ILocationsRepository;

/**
 * Base class for aggregation of location information.
 * @author gpothier
 */
public abstract class LocationInfo implements ILocationInfo
{
	private final ILocationsRepository itsTrace;
	private final int itsId;
	private String itsName;
	
	public LocationInfo(ILocationsRepository aTrace, int aId)
	{
		itsTrace = aTrace;
		itsId = aId;
	}

	public LocationInfo(ILocationsRepository aTrace, int aId, String aName)
	{
		itsTrace = aTrace;
		itsId = aId;
		itsName = aName;
	}
	
	public int getId()
	{
		return itsId;
	}
	
	public ILocationsRepository getTrace()
	{
		return itsTrace;
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
		assert itsName == null || itsName.equals(aName);
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
			ILocationInfo theInfo = (ILocationInfo) aObj;
			return theInfo.getClass().equals(getClass()) && theInfo.getId() == getId();
		}
		else return false;
	}
}
