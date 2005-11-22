/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;

import tod.core.model.event.ILogEvent;
import tod.core.model.event.IParentEvent;
import tod.core.model.structure.IThreadInfo;
import tod.core.model.trace.ICFlowBrowser;
import tod.core.model.trace.IEventTrace;
import tod.gui.IGUIManager;
import tod.gui.seed.CFlowSeed;
import tod.gui.view.LogView;
import zz.csg.display.GraphicPanel;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;

public class CFlowView extends LogView
{
	private CFlowSeed itsSeed;
	private ICFlowBrowser itsBrowser;
	private CFlowTreeBuilder itsTreeBuilder;
	private CFlowVariablesBuilder itsVariablesBuilder;
	private CFlowObjectsBuilder itsObjectsBuilder;
	
	private GraphicPanel itsTreePanel;
	private GraphicPanel itsVariablesPanel;
	private GraphicPanel itsObjectsPanel;
	
	private Set<IParentEvent> itsExpandedEvents = new HashSet<IParentEvent>();
	
	private AbstractEventNode itsRootNode;
	
	private IPropertyListener<ILogEvent> itsSelectedEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			itsTreePanel.repaint();
			update();
		}
	};

	private IPropertyListener<ILogEvent> itsRootEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			update();
		}
	};
	
	public CFlowView(IGUIManager aGUIManager, IEventTrace aEventTrace, CFlowSeed aSeed)
	{
		super (aGUIManager, aEventTrace);
		itsSeed = aSeed;

		IThreadInfo theThread = itsSeed.getThread();
		itsBrowser = getEventTrace().createCFlowBrowser(theThread);
	}

	@Override
	public void init()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		// Create tree panel
		itsTreeBuilder = new CFlowTreeBuilder(this);
		itsRootNode = itsTreeBuilder.buildRootNode((IParentEvent) itsBrowser.getRoot());
		
		itsTreePanel = new GraphicPanel();
		itsTreePanel.setTransform(new AffineTransform());
		itsTreePanel.setRootNode(itsRootNode);
		
		JScrollPane theTreeScrollPane = new JScrollPane(itsTreePanel);
		theTreeScrollPane.setPreferredSize(new Dimension(400, 10));
		add(theTreeScrollPane);
		
		// Create variables panel
		itsVariablesBuilder = new CFlowVariablesBuilder(this);
		
		itsVariablesPanel = new GraphicPanel();
		itsVariablesPanel.setTransform(new AffineTransform());
		
		add(new JScrollPane(itsVariablesPanel));
		
		// Create objects panel
		itsObjectsBuilder = new CFlowObjectsBuilder(this);
		
		itsObjectsPanel = new GraphicPanel();
		itsObjectsPanel.setTransform(new AffineTransform());
		
		add(new JScrollPane(itsObjectsPanel));
		
		update();
	}
	
	private void update()
	{
		ILogEvent theRootEvent = itsSeed.pRootEvent().get();
		ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
		
		if (theSelectedEvent != null)
		{
			itsVariablesPanel.setRootNode(itsVariablesBuilder.build(theRootEvent, theSelectedEvent));
			itsObjectsPanel.setRootNode(itsObjectsBuilder.build(theRootEvent, theSelectedEvent));
			
			showEvent(theSelectedEvent);
		}
		
		if (theSelectedEvent != null) getGUIManager().gotoEvent(theSelectedEvent);
	}
	
	private void showEvent (ILogEvent aEvent)
	{
		ILogEvent theRootEvent = itsSeed.pRootEvent().get();
		
		LinkedList<ILogEvent> theEventPath = new LinkedList<ILogEvent>();
		
		while (aEvent != null)
		{
			theEventPath.addFirst(aEvent);
			if (aEvent == theRootEvent) break;
			aEvent = aEvent.getParent();
		}
		
		AbstractEventNode theNode = itsRootNode;
		for (ILogEvent theEvent : theEventPath)
		{
			theNode = theNode.getNode(theEvent);
			theNode.expand();
		}
		
//		itsRootNode.invalidate();
//		itsRootNode.checkValid(); // the layout must be ready.
//		Rectangle2D theNodeBounds = theNode.getBounds(null);
//		Rectangle theBounds = itsTreePanel.localToPixel(null, theNode, theNodeBounds);
//		itsTreePanel.scrollRectToVisible(theBounds);
	}
		
	@Override
	public void addNotify()
	{
		super.addNotify();
		itsSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
		itsSeed.pRootEvent().addHardListener(itsRootEventListener);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsSeed.pSelectedEvent().removeListener(itsSelectedEventListener);
		itsSeed.pRootEvent().removeListener(itsRootEventListener);
	}

	public CFlowTreeBuilder getBuilder()
	{
		return itsTreeBuilder;
	}

	/**
	 * Returns the path to the deepest {@link IParentEvent} whose span contains 
	 * both specified timestamps.
	 */
	private List<IParentEvent> getPathForRange(double aT1, double aT2)
	{
		IParentEvent theRoot = (IParentEvent) itsBrowser.getRoot();
		List<IParentEvent> thePath = new ArrayList<IParentEvent>();
		computePathForRange(thePath, theRoot, aT1, aT2);
		return thePath;
	}

	private void computePathForRange(
			List<IParentEvent> aPath, 
			IParentEvent aRoot, 
			double aT1, 
			double aT2)
	{
		aPath.add(aRoot);
		
		for (ILogEvent theChild : aRoot.getChildren())
		{
			if (theChild instanceof IParentEvent)
			{
				IParentEvent theContainer = (IParentEvent) theChild;
				if (theContainer.getFirstTimestamp() <= aT1 && theContainer.getLastTimestamp() >= aT2)
				{
					computePathForRange(aPath, theContainer, aT1, aT2);
					break;
				}
			}
		}
	}
	
	/**
	 * Returns the path to the deepest {@link IParentEvent} whose span contains 
	 * the specified timestamp.
	 */
	private List<IParentEvent> getPathForTimestamp(double aT)
	{
		return getPathForRange(aT, aT);
	}
	
	public void selectEvent(ILogEvent aEvent)
	{
		itsSeed.pSelectedEvent().set(aEvent);
	}
	
	public boolean isEventSelected(ILogEvent aEvent)
	{
		return itsSeed.pSelectedEvent().get() == aEvent;
	}
}
