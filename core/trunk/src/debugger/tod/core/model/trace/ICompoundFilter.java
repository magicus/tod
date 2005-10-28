/*
 * Created on Nov 9, 2004
 */
package tod.core.model.trace;

import java.util.List;

/**
 * A filter that contains a number of subfilters.
 * This is useful for union and intersection of filters.
 * @author gpothier
 */
public interface ICompoundFilter extends IEventFilter
{
	/**
	 * Returns the list of all sub-filters.
	 * @return The lits that backs this compound filter.
	 */
	public List<IEventFilter> getFilters();
	
	/**
	 * Adds a filter to this compound filter.
	 * @param aFilter The filter to add
	 * @throws IllegalStateException Thrown if it is not possible anymore
	 * to change this filter. Implementation might not allow modifications
	 * to filters that have already runned.
	 */
	public void add (IEventFilter aFilter) throws IllegalStateException;
	
	/**
	 * Removes a filter from this compound filter.
	 * @param aFilter The filter to remove
	 * @throws IllegalStateException Thrown if it is not possible anymore
	 * to change this filter. Implementation might not allow modifications
	 * to filters that have already runned.
	 */
	public void remove (IEventFilter aFilter) throws IllegalStateException;
}
