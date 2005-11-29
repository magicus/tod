/*
 * Created on Nov 10, 2004
 */
package tod.gui.view;

import java.awt.BorderLayout;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventTrace;
import tod.gui.EventListModel;
import tod.gui.IGUIManager;
import tod.gui.formatter.EventFormatter;
import tod.gui.seed.FilterSeed;
import tod.gui.view.event.EventView;
import tod.gui.view.event.EventViewFactory;
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
		itsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
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