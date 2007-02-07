/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.controlflow.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import tod.core.database.browser.BrowserUtils;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.EventUtils;
import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.IConstructorChainingEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IMethodCallEvent;
import tod.core.database.event.IParentEvent;
import tod.gui.JobProcessor;
import tod.gui.MinerUI;
import tod.gui.controlflow.CFlowView;
import tod.gui.eventlist.EventListCore;
import tod.gui.eventlist.MuralScroller;
import tod.gui.eventlist.MuralScroller.UnitScroll;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.properties.IProperty;
import zz.utils.properties.PropertyListener;
import zz.utils.ui.GridStackLayout;
import zz.utils.ui.StackLayout;

public class CFlowTree extends JPanel
implements MouseWheelListener
{
	private static final String PROPERTY_SPLITTER_POS = "cflowTree.splitterPos";
	
	private final CFlowView itsView;

	private EventListCore itsCore;
	private JPanel itsEventList;
	
	private MuralScroller itsScroller;
	
	private long itsFirstDisplayedTimestamp;
	private long itsLastDisplayedTimestamp;
	
	/**
	 * Maps currently displayed events to their graphic node.
	 */
	private Map<ILogEvent, AbstractEventNode> itsNodesMap = 
		new HashMap<ILogEvent, AbstractEventNode>();

	private JSplitPane itsSplitPane;
	
	public CFlowTree(CFlowView aView)
	{
		itsView = aView;
		
		createUI();
		setParent(null);
	}
	
	public JobProcessor getJobProcessor()
	{
		return itsView.getGUIManager().getJobProcessor();
//		return null;
	}

	public void forward(int aCount)
	{
		getJobProcessor().acquire();
		itsCore.forward(aCount);
		updateList();
		getJobProcessor().release();
	}
	
	public void backward(int aCount)
	{
		getJobProcessor().acquire();
		itsCore.backward(aCount);
		updateList();
		getJobProcessor().release();
	}
	
	public void setTimestamp(long aTimestamp)
	{
		getJobProcessor().acquire();
		itsCore.setTimestamp(aTimestamp);
		updateList();
		getJobProcessor().release();
	}

	public void setParent(IParentEvent aParentEvent)
	{
		if (aParentEvent == null) aParentEvent = itsView.getSeed().pRootEvent().get();
		itsCore = new EventListCore(aParentEvent.getChildrenBrowser(), 10);

		IEventBrowser theBrowser = aParentEvent.getChildrenBrowser();
		itsScroller.set(
				theBrowser, 
				BrowserUtils.getFirstTimestamp(theBrowser),
				BrowserUtils.getLastTimestamp(theBrowser));

		update();
	}

	private void updateList()
	{
		itsEventList.removeAll();
		LinkedList<ILogEvent> theEvents = itsCore.getDisplayedEvents();
		
		Map<ILogEvent, AbstractEventNode> theOldMap = itsNodesMap;
		itsNodesMap = new HashMap<ILogEvent, AbstractEventNode>();
		
		for (ILogEvent theEvent : theEvents)
		{
			AbstractEventNode theNode = theOldMap.get(theEvent);
			if (theNode == null) theNode = buildEventNode(theEvent);
			
			if (theNode != null) 
			{
				itsEventList.add(theNode);
				itsNodesMap.put(theEvent, theNode);
			}
		}
		
		itsEventList.revalidate();
		itsEventList.repaint();
	}
	
	private void createUI()
	{
		itsScroller = new MuralScroller();
		
		itsScroller.eUnitScroll().addListener(new IEventListener<UnitScroll>()
				{
					public void fired(IEvent< ? extends UnitScroll> aEvent, UnitScroll aData)
					{
						switch (aData)
						{
						case UP:
							backward(1);
							break;
							
						case DOWN:
							forward(1);
							break;
						}
					}
				});
		itsScroller.pTrackScroll().addHardListener(new PropertyListener<Long>()
				{
					@Override
					public void propertyChanged(IProperty<Long> aProperty, Long aOldValue, Long aNewValue)
					{
						setTimestamp(aNewValue);
					}
				});
		
		itsSplitPane = new JSplitPane();
		setLayout(new StackLayout());
		add(itsSplitPane);
		
		itsEventList = new ScrollablePanel(new GridStackLayout(1, 0, 0, true, false));
		itsEventList.setOpaque(false);
		itsEventList.addMouseWheelListener(this);

		
		JPanel theRightComponent = new JPanel(new BorderLayout());
		theRightComponent.add(new MyScrollPane(itsEventList), BorderLayout.CENTER);
		theRightComponent.add(itsScroller, BorderLayout.EAST);
		itsSplitPane.setRightComponent(theRightComponent);
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		int theSplitterPos = MinerUI.getIntProperty(
				itsView.getGUIManager(), 
				PROPERTY_SPLITTER_POS, 200);
		itsSplitPane.setDividerLocation(theSplitterPos);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsView.getGUIManager().setProperty(
				PROPERTY_SPLITTER_POS, 
				""+itsSplitPane.getDividerLocation());
	}
	
	private void update()
	{
		JComponent theStack = createStack();
		theStack.setOpaque(false);
		JScrollPane theScrollPane = new JScrollPane(theStack);
		theScrollPane.getViewport().setBackground(Color.WHITE);
		itsSplitPane.setLeftComponent(theScrollPane);
		
		itsNodesMap.clear();
		updateList();
				
		revalidate();
		repaint();
	}
	
	public boolean isVisible(ILogEvent aEvent)
	{
		long theTimestamp = aEvent.getTimestamp();
		return theTimestamp >= itsFirstDisplayedTimestamp 
				&& theTimestamp <= itsLastDisplayedTimestamp;
	}
	
	/**
	 * Scrolls so that the given event is visible.
	 * @return The bounds of the graphic object that represent
	 * the event.
	 */
	public void makeVisible(ILogEvent aEvent)
	{
		AbstractEventNode theNode = itsNodesMap.get(aEvent);
		if (theNode == null)
		{
			setTimestamp(aEvent.getTimestamp());
			backward(2);
			theNode = itsNodesMap.get(aEvent);
		}
		
		if (theNode != null) 
		{
			theNode.scrollRectToVisible(theNode.getBounds());
		}
	}
	
	private IBehaviorCallEvent getCurrentParent()
	{
		return itsView.getSeed().pParentEvent().get();
	}
	
	public void mouseWheelMoved(MouseWheelEvent aEvent)
	{
		System.out.println("CFlowTree.mouseWheelMoved()");
		int theRotation = aEvent.getWheelRotation();
		if (theRotation < 0) backward(1);
		else if (theRotation > 0) forward(1);

		aEvent.consume();
	}

	
	/**
	 * Builds the stack of ancestor events.
	 */
	private JComponent createStack()
	{
		List<IBehaviorCallEvent> theAncestors = new ArrayList<IBehaviorCallEvent>();
		IBehaviorCallEvent theCurrentParent = getCurrentParent();
		while (theCurrentParent != null)
		{
			theAncestors.add(theCurrentParent);
			theCurrentParent = theCurrentParent.getParent();
		}
		
		JPanel theContainer = new JPanel(new GridStackLayout(1, 0, 2, true, false));

		if (theAncestors.size() > 0) for(int i=0;i<theAncestors.size();i++)
		{
			IBehaviorCallEvent theAncestor = theAncestors.get(i);
			theContainer.add(buildStackNode(theAncestor));
		}
		
		return theContainer;
	}
	
	private AbstractEventNode buildEventNode(ILogEvent aEvent)
	{
//		JobProcessor theJobProcessor = getJobProcessor();
		JobProcessor theJobProcessor = null;
		
		if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
			return new FieldWriteNode(itsView, theJobProcessor, theEvent);
		}
		else if (aEvent instanceof IArrayWriteEvent)
		{
			IArrayWriteEvent theEvent = (IArrayWriteEvent) aEvent;
			return new ArrayWriteNode(itsView, theJobProcessor, theEvent);
		}
		else if (aEvent instanceof ILocalVariableWriteEvent)
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) aEvent;
			return new LocalVariableWriteNode(itsView, theJobProcessor, theEvent);
		}
		else if (aEvent instanceof IExceptionGeneratedEvent)
		{
			IExceptionGeneratedEvent theEvent = (IExceptionGeneratedEvent) aEvent;
			if (EventUtils.isIgnorableException(theEvent)) return null;
			else return new ExceptionGeneratedNode(itsView, theJobProcessor, theEvent);
		}
		else if (aEvent instanceof IMethodCallEvent)
		{
			IMethodCallEvent theEvent = (IMethodCallEvent) aEvent;
			return new MethodCallNode(itsView, theJobProcessor, theEvent);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			return new InstantiationNode(itsView, theJobProcessor, theEvent);
		}
		else if (aEvent instanceof IConstructorChainingEvent)
		{
			IConstructorChainingEvent theEvent = (IConstructorChainingEvent) aEvent;
			return new ConstructorChainingNode(itsView, theJobProcessor, theEvent);
		}
		else if (aEvent instanceof IBehaviorExitEvent)
		{
			return null;
		}

		return new UnknownEventNode(itsView, theJobProcessor, aEvent);
	}


	
	private StackNode buildStackNode(IBehaviorCallEvent aEvent)
	{
//		JobProcessor theJobProcessor = getJobProcessor();
		JobProcessor theJobProcessor = null;
		
		if (aEvent instanceof IMethodCallEvent)
		{
			return new StackNode(itsView, theJobProcessor, aEvent);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			return new StackNode(itsView, theJobProcessor, aEvent);
		}
		else if (aEvent instanceof IConstructorChainingEvent)
		{
			return new StackNode(itsView, theJobProcessor, aEvent);
		}
		else throw new RuntimeException("Not handled: "+aEvent);
	}
	
	private static class MyScrollPane extends JScrollPane
	{
		private MyScrollPane(Component aView)
		{
			super(aView, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			getViewport().setBackground(Color.WHITE);
			setWheelScrollingEnabled(false);
		}
	}
	

	
	private static class ScrollablePanel extends JPanel 
	implements Scrollable
	{
		private ScrollablePanel(LayoutManager aLayout)
		{
			super(aLayout);
			
		}

		public boolean getScrollableTracksViewportHeight()
		{
			return true;
		}
		
		public boolean getScrollableTracksViewportWidth()
		{
			return false;
		}

		public Dimension getPreferredScrollableViewportSize()
		{
			return getPreferredSize();
		}

		public int getScrollableBlockIncrement(Rectangle aVisibleRect, int aOrientation, int aDirection)
		{
			switch (aOrientation)
			{
			case SwingConstants.HORIZONTAL:
				return 80 * aVisibleRect.width / 100;
				
			case SwingConstants.VERTICAL:
				return 80 * aVisibleRect.height / 100;
				
			default:
				throw new RuntimeException();
			}
		}

		public int getScrollableUnitIncrement(Rectangle aVisibleRect, int aOrientation, int aDirection)
		{
			switch (aOrientation)
			{
			case SwingConstants.HORIZONTAL:
				return 10 * aVisibleRect.width / 100;
				
			case SwingConstants.VERTICAL:
				return 10 * aVisibleRect.height / 100;
				
			default:
				throw new RuntimeException();
			}
		}
		
	}
	
}
