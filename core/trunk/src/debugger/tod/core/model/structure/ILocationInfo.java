/*
 * Created on Nov 22, 2005
 */
package tod.core.model.structure;

import tod.core.model.browser.ILocationLog;

public interface ILocationInfo
{

	public int getId();

	/**
	 * Returns the location trace that contains this location. 
	 */
	public ILocationLog getTrace();

	public String getName();

}