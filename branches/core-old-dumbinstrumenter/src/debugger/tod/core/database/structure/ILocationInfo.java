/*
 * Created on Nov 22, 2005
 */
package tod.core.database.structure;

import tod.core.database.browser.ILocationsRepository;

public interface ILocationInfo
{

	public int getId();

	/**
	 * Returns the location trace that contains this location. 
	 */
	public ILocationsRepository getTrace();

	public String getName();

}