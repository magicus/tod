/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.awt.Image;
import java.util.Collection;

import zz.csg.api.IRectangularGraphicObject;
import zz.utils.ItemAction;
import zz.utils.properties.IRWProperty;

/**
 * A view of an horizontal event sequence.
 * Each indivudual components of the view must be requested
 * through corresponding methods. For instance, to obtain the main
 * graphic object (the one that displays the events), use
 * {@link #getEventStripe()};
 * for obtaining the available actions, use {@link #getActions()}.
 * @author gpothier
 */
public interface IEventSequenceView 
{
	/**
	 * Starting timestamp of the displayed time range.
	 */
	public IRWProperty<Long> pStart ();
	
	/**
	 * Ending timestamp of the displayed time range.
	 */
	public IRWProperty<Long> pEnd ();
	
	/**
	 * Returns the horizontal stripe that displays events.
	 */
	public IRectangularGraphicObject getEventStripe();
	
	/**
	 * Returns a collection of available actions for this sequence view.
	 */
	public Collection<ItemAction> getActions();
	
	/**
	 * Returns an icon representing this sequence view.
	 */
	public Image getIcon();
	
	/**
	 * Returns the title of this sequence view.
	 */
	public String getTitle();
}
