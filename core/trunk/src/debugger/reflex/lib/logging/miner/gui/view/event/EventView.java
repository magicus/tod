/*
 * Created on Nov 11, 2004
 */
package reflex.lib.logging.miner.gui.view.event;

import javax.swing.JLabel;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.formatter.EventFormatter;
import reflex.lib.logging.miner.gui.kit.LinkLabel;
import reflex.lib.logging.miner.gui.kit.SeedLinkLabel;
import reflex.lib.logging.miner.gui.seed.CFlowSeed;
import reflex.lib.logging.miner.gui.seed.FilterSeed;
import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.trace.IEventTrace;
import zz.utils.ui.GridStackLayout;

/**
 * Base class for event viewers. It sets a framework for the UI:
 * subclasses should override the {@link #init()} method
 * and create their UI here after calling super.
 * They should add the components to the panel with no
 * layout constraints; they will be stacked vertically.
 * @author gpothier
 */
public abstract class EventView extends LogView
{
	public EventView(IGUIManager aManager, IEventTrace aLog)
	{
		super (aManager, aLog);
	}
	
	public void init()
	{
		setLayout(new GridStackLayout(1, 0, 5, false, false));
		add (createTitleLabel(EventFormatter.getInstance().getHtmlText(getEvent())));
		
		ILogEvent theEvent = getEvent();
		ThreadInfo theThreadInfo = theEvent.getThread();
		
		// Thread & timestamp
		add (createTitledLink(
				"Thread: ", 
				"\""+theThreadInfo.getName()+"\" ["+theThreadInfo.getId()+"]", 
				new FilterSeed (getGUIManager(), getEventTrace(), getEventTrace().createThreadFilter(theThreadInfo))));

		add (createTitledPanel(
				"Timestamp: ", 
				new JLabel (""+theEvent.getTimestamp())));

		
		// CFLow
		LinkLabel theCFlowLabel = new SeedLinkLabel(
				getGUIManager(), 
				"View control flow", 
				new CFlowSeed(getGUIManager(), getEventTrace(), theEvent));
		add (theCFlowLabel);
		
	}

	/**
	 * Returns the event represented by this view.
	 */
	protected abstract ILogEvent getEvent ();
	
}
