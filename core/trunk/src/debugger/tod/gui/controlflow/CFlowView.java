/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.BorderLayout;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.seed.CFlowSeed;
import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.event.IParentEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.trace.ICFlowBrowser;
import tod.core.model.trace.IEventTrace;
import zz.csg.api.IRectangularGraphicObject;
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
	
	private GraphicPanel itsTreePanel;
	private GraphicPanel itsVariablesPanel;
	
	private Set<IParentEvent> itsExpandedEvents = new HashSet<IParentEvent>();
	
	private IPropertyListener<ILogEvent> itsSelectedEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			itsTreePanel.repaint();
			if (aNewValue != null) getGUIManager().gotoEvent(aNewValue);
			updateVariables();
		}
	};

	private IPropertyListener<ILogEvent> itsRootEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			updateVariables();
		}
	};
	
	public CFlowView(IGUIManager aGUIManager, IEventTrace aEventTrace, CFlowSeed aSeed)
	{
		super (aGUIManager, aEventTrace);
		itsSeed = aSeed;

		ThreadInfo theThread = itsSeed.getThread();
		itsBrowser = getEventTrace().createCFlowBrowser(theThread);
	}

	@Override
	public void init()
	{
		setLayout(new BorderLayout());

		// Create tree panel
		itsTreeBuilder = new CFlowTreeBuilder(this);
		
		itsTreePanel = new GraphicPanel();
		itsTreePanel.setTransform(new AffineTransform());
		itsTreePanel.setRootNode(itsTreeBuilder.buildRootNode((IParentEvent) itsBrowser.getRoot()));
		
		add(new JScrollPane(itsTreePanel), BorderLayout.CENTER);
		
		// Create variables panel
		itsVariablesBuilder = new CFlowVariablesBuilder(this);
		
		itsVariablesPanel = new GraphicPanel();
		itsVariablesPanel.setTransform(new AffineTransform());
		
		add(new JScrollPane(itsVariablesPanel), BorderLayout.EAST);
	}
	
	private void updateVariables()
	{
		ILogEvent theRootEvent = itsSeed.pRootEvent().get();
		ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
		
		IRectangularGraphicObject theGraphicObject = 
			itsVariablesBuilder.build(theRootEvent, theSelectedEvent);
		
		itsVariablesPanel.setRootNode(theGraphicObject);
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
