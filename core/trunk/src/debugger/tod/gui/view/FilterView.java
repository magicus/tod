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

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.EventListModel;
import tod.gui.IGUIManager;
import tod.gui.formatter.EventFormatter;
import tod.gui.seed.FilterSeed;
import tod.gui.view.event.EventView;
import tod.gui.view.event.EventViewFactory;
import zz.utils.ui.FormattedRenderer;

/**
 * A view component that displays a list of events 
 * based on a {@link tod.core.database.browser.IEventFilter}
 * @author gpothier
 */
public class FilterView extends LogView implements ListSelectionListener
{
	private FilterSeed itsSeed;
	
	private EventListModel itsModel;
	
	private JSplitPane itsSplitPane;

	private JList itsList;
	
	public FilterView(IGUIManager aGUIManager, ILogBrowser aLog, FilterSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
		
		createUI ();
	}

	private void createUI()
	{
		itsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		IEventBrowser theBrowser = getTrace().createBrowser(itsSeed.getFilter());
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
				getTrace(),
				aEvent);
		
		itsSplitPane.setRightComponent(theView);
		revalidate();
		repaint();
	}
}
