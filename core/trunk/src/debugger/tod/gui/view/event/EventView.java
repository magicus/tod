/*
 * Created on Nov 11, 2004
 */
package tod.gui.view.event;

import javax.swing.JLabel;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;
import tod.gui.IGUIManager;
import tod.gui.formatter.EventFormatter;
import tod.gui.kit.LinkLabel;
import tod.gui.kit.SeedLinkLabel;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.FilterSeed;
import tod.gui.view.LogView;
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
	public EventView(IGUIManager aManager, ILogBrowser aLog)
	{
		super (aManager, aLog);
	}
	
	public void init()
	{
		setLayout(new GridStackLayout(1, 0, 5, false, false));
		add (createTitleLabel(EventFormatter.getInstance().getHtmlText(getEvent())));
		
		ILogEvent theEvent = getEvent();
		IThreadInfo theThreadInfo = theEvent.getThread();
		
		// Thread & timestamp
		add (createTitledLink(
				"Thread: ", 
				"\""+theThreadInfo.getName()+"\" ["+theThreadInfo.getId()+"]", 
				new FilterSeed (getGUIManager(), getTrace(), getTrace().createThreadFilter(theThreadInfo))));

		add (createTitledPanel(
				"Timestamp: ", 
				new JLabel (""+theEvent.getTimestamp())));

		
		// CFLow
		LinkLabel theCFlowLabel = new SeedLinkLabel(
				getGUIManager(), 
				"View control flow", 
				new CFlowSeed(getGUIManager(), getTrace(), theEvent));
		add (theCFlowLabel);
		
	}

	/**
	 * Returns the event represented by this view.
	 */
	protected abstract ILogEvent getEvent ();
	
}
