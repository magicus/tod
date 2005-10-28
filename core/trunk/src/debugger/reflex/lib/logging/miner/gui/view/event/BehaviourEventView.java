/*
 * Created on Nov 11, 2004
 */
package reflex.lib.logging.miner.gui.view.event;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.kit.LinkLabel;
import reflex.lib.logging.miner.gui.seed.SeedFactory;
import tod.core.model.event.IEvent_Behaviour;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IEventTrace;
import zz.utils.ui.GridStackLayout;

/**
 * View for behaviour events.
 * @author gpothier
 */
public abstract class BehaviourEventView extends EventView 
{
	public BehaviourEventView(
			IGUIManager aManager, 
			IEventTrace aLog)
	{
		super(aManager, aLog);
	}

	
	protected ILogEvent getEvent()
	{
		return getBehaviourEvent();
	}
	
	/**
	 * Returns the behaviour event of this view.
	 */
	protected abstract IEvent_Behaviour getBehaviourEvent ();
}
