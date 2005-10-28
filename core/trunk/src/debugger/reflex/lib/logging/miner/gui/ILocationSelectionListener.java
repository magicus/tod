/*
 * Created on Nov 10, 2004
 */
package reflex.lib.logging.miner.gui;

import java.util.List;

import tod.core.model.structure.LocationInfo;

/**
 * @author gpothier
 */
public interface ILocationSelectionListener
{
	public void selectionChanged (List/*<LocationInfo>*/ aSelectedLocations);
}
