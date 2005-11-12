/*
 * Created on Nov 10, 2004
 */
package reflex.lib.logging.miner.gui.view;

import java.awt.BorderLayout;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import reflex.lib.logging.miner.gui.EventListModel;
import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.formatter.EventFormatter;
import reflex.lib.logging.miner.gui.seed.FilterSeed;
import reflex.lib.logging.miner.gui.view.event.EventView;
import reflex.lib.logging.miner.gui.view.event.EventViewFactory;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventFilter;
import zz.utils.ui.FormattedRenderer;

/**
 * A view component that displays a list of events 
 * based on a {@link tod.core.model.trace.IEventFilter}
 * @author gpothier
 */
public class FilterView extends LogView implements ListSelectionListener
{
	private FilterSeed itsSeed;
	
	private EventListModel itsModel;
	
	private JSplitPane itsSplitPane;

	private JList itsList;
	
	public FilterView(IGUIManager aGUIManager, IEventTrace aLog, FilterSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
		
		createUI ();
	}

	private void createUI()
	{
		itsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		IEventBrowser theBrowser = getEventTrace().createBrowser(itsSeed.getFilter());
		itsModel = new EventListModel (theBrowser);
		itsList = new JList (itsModel);
		itsList.setCellRenderer(new FormattedRenderer(EventFormatter.getInstance()));
		itsList.addListSelectionListener(this);
		
		setLayout(new BorderLayout());
		add (itsSplitPane, BorderLayout.CENTER);
		
		itsSplitPane.setLeftComponent(new JScrollPane (itsList));
	}
	
	
	public void valueChanged(ListSelectionEvent aE)
	{
		setSelectedEvent((ILogEvent) itsList.getSelectedValue());
	}
	
	private void setSelectedEvent (ILogEvent aEvent)
	{
		EventView theView = EventViewFactory.createView(
				getGUIManager(), 
				getEventTrace(),
				aEvent);
		
		itsSplitPane.setRightComponent(theView);
		revalidate();
		repaint();
	}
}
