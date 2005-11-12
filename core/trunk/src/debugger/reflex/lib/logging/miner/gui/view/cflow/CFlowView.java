/*
 * Created on Nov 15, 2004
 */
package reflex.lib.logging.miner.gui.view.cflow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.formatter.EventFormatter;
import reflex.lib.logging.miner.gui.formatter.LocationFormatter;
import reflex.lib.logging.miner.gui.seed.CFlowSeed;
import reflex.lib.logging.miner.gui.view.LogView;
import reflex.lib.logging.miner.impl.common.event.BehaviorEnter;
import reflex.lib.logging.miner.impl.common.event.Event;
import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ICFlowBrowser;
import zz.utils.SimpleListModel;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;
import zz.utils.tree.SwingTreeModel;
import zz.utils.ui.FormattedRenderer;
import zz.utils.ui.StackLayout;

/**
 * This view permits to navigate in the control flow.
 * @author gpothier
 */
public class CFlowView extends LogView 
{
	private CFlowSeed itsSeed;
	private ICFlowBrowser itsCFlowBrowser;
	
	private JTree itsEventsTree;
	private SwingTreeModel<ILogEvent, ILogEvent> itsEventsTreeModel;
	
	private JList itsCallStackList;
	private SimpleListModel itsCallStackListModel;
	
	private IPropertyListener<ILogEvent> itsSelectedEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			selectEvent(aNewValue);
		}
	};
	
	public CFlowView(IGUIManager aGUIManager, IEventTrace aLog, CFlowSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
		itsCFlowBrowser = getEventTrace().createCFlowBrowser(itsSeed.getThread());
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		itsSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsSeed.pSelectedEvent().removeListener(itsSelectedEventListener);
	}
	
	@Override
	public void init()
	{
		setLayout(new BorderLayout());
		
		// Create events tree
		itsEventsTreeModel = new SwingTreeModel<ILogEvent, ILogEvent>(itsCFlowBrowser);
		itsEventsTree = new JTree(itsEventsTreeModel);
		itsEventsTree.setCellRenderer(new FormattedRenderer(EventFormatter.getInstance()));
		itsEventsTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
				{
					public void valueChanged(TreeSelectionEvent aE)
					{
						ILogEvent theEvent = (ILogEvent) aE.getPath().getLastPathComponent();
						itsSeed.pSelectedEvent().set(theEvent);
						getGUIManager().gotoEvent(theEvent);
					}
				});
		add (new JScrollPane(itsEventsTree), BorderLayout.CENTER);
		
		// Create call stack list
		itsCallStackListModel = new SimpleListModel();
		itsCallStackList = new JList(itsCallStackListModel);
		itsCallStackList.setCellRenderer(new FormattedRenderer(EventFormatter.getInstance()));
		add (itsCallStackList, BorderLayout.WEST);
		
		selectEvent(itsSeed.pSelectedEvent().get());
	}
	
	private void selectEvent (ILogEvent aEvent)
	{
		// Select in tree
		TreePath theTreePath = itsEventsTreeModel.getTreePath(aEvent);
		itsEventsTree.setSelectionPath(theTreePath);
		itsEventsTree.scrollPathToVisible(theTreePath);
		
		// Update call stack
		List<IBehaviorEnterEvent> theCallStack = new ArrayList<IBehaviorEnterEvent>();
		IBehaviorEnterEvent theCurrentEvent = aEvent.getParent();
		while (theCurrentEvent != null)
		{
			theCallStack.add(theCurrentEvent);
			theCurrentEvent = theCurrentEvent.getParent();
		}
		itsCallStackListModel.setList(theCallStack);
	}
}
