/*
 * Created on Nov 22, 2005
 */
package tod.core.model.structure;

import tod.core.model.trace.ILocationTrace;

public interface ILocationInfo
{

	public int getId();

	/**
	 * Returns the location trace that contains this location. 
	 */
	public ILocationTrace getTrace();

	public String getName();

}