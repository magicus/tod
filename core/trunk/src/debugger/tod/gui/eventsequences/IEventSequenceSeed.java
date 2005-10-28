/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import reflex.lib.logging.miner.gui.view.LogView;
import zz.csg.api.IDisplay;
import zz.utils.properties.IRWProperty;

/**
 * A seed that permits to create event sequence views.
 * @author gpothier
 */
public interface IEventSequenceSeed
{
	/**
	 * Creates a new view corresponding to this seed.
	 * @param aLogView TODO
	 */
	public IEventSequenceView createView(IDisplay aDisplay, LogView aLogView);
}
