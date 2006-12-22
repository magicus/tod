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
package tod.gui.controlflow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import tod.core.database.browser.ICFlowBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.Stepper;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IThreadInfo;
import tod.gui.IGUIManager;
import tod.gui.seed.CFlowSeed;
import tod.gui.view.LogView;
import zz.csg.display.GraphicPanel;
import zz.utils.SimpleAction;
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
	
	private Stepper itsStepper;
	
	private GraphicPanel itsTreePanel;
	private GraphicPanel itsVariablesPanel;
	private GraphicPanel itsObjectsPanel;
	
	private Set<IParentEvent> itsExpandedEvents = new HashSet<IParentEvent>();
	
	private AbstractEventNode itsRootNode;
	
	private boolean itsUpdated = false;
	
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
	
	public CFlowView(IGUIManager aGUIManager, ILogBrowser aEventTrace, CFlowSeed aSeed)
	{
		super (aGUIManager, aEventTrace);
		itsSeed = aSeed;

		IThreadInfo theThread = itsSeed.getThread();
		itsBrowser = getLogBrowser().createCFlowBrowser(theThread);
		itsStepper = new Stepper(getLogBrowser(), theThread);
	}

	@Override
	public void init()
	{
		setLayout(new BorderLayout());
		
		// Create tree panel
		itsTreeBuilder = new CFlowTreeBuilder(this);
		itsRootNode = itsTreeBuilder.buildRootNode((IParentEvent) itsBrowser.getRoot());
		
		itsTreePanel = new GraphicPanel();
//		{
//			@Override
//			protected void paintComponent(Graphics aG)
//			{
//				super.paintComponent(aG);
//				if (! itsUpdated) 
//				{
//					SwingUtilities.invokeLater(new Runnable()
//					{
//						public void run()
//						{
//							CFlowView.this.update();
//						}
//					});
//					itsUpdated = true;
//				}
//			}
//		};
		itsTreePanel.setTransform(new AffineTransform());
		itsTreePanel.setRootNode(itsRootNode);
		
		JScrollPane theTreeScrollPane = new JScrollPane(itsTreePanel);
		theTreeScrollPane.setPreferredSize(new Dimension(400, 10));
		
		JPanel theCFlowPanel = new JPanel(new BorderLayout());
		theCFlowPanel.add(theTreeScrollPane, BorderLayout.CENTER);
		theCFlowPanel.add(createToolbar(), BorderLayout.NORTH);
		
		// Create variables panel
		itsVariablesBuilder = new CFlowVariablesBuilder(this);
		
		itsVariablesPanel = new GraphicPanel();
		itsVariablesPanel.setTransform(new AffineTransform());
		
		// Create objects panel
		itsObjectsBuilder = new CFlowObjectsBuilder(this);
		
		itsObjectsPanel = new GraphicPanel();
		itsObjectsPanel.setTransform(new AffineTransform());
		

		// Setup split panes
		JSplitPane theSplitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//		theSplitPane1.setResizeWeight(0.33);
		theSplitPane1.setLeftComponent(theCFlowPanel);
		
		JSplitPane theSplitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		theSplitPane2.setResizeWeight(0.5);
		theSplitPane2.setLeftComponent(new JScrollPane(itsVariablesPanel));
		theSplitPane2.setRightComponent(new JScrollPane(itsObjectsPanel));
		
		theSplitPane1.setRightComponent(theSplitPane2);
//		theSplitPane1.setRightComponent(new JScrollPane(itsVariablesPanel));
		
		add(theSplitPane1, BorderLayout.CENTER);
		
		update();
	}
	
	private JComponent createToolbar()
	{
		JPanel theToolbar = new JPanel();
		
		theToolbar.add(new JButton(new SimpleAction("|<", "Backward step over")
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
				itsStepper.backwardStepOver();
				selectEvent(itsStepper.getCurrentEvent());
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction("{}<", "Backward step into")
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
				itsStepper.backwardStepInto();
				selectEvent(itsStepper.getCurrentEvent());
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction("/\\", "Step out")
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
				itsStepper.stepOut();
				selectEvent(itsStepper.getCurrentEvent());
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction(">{}", "Forward step into")
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
				itsStepper.forwardStepInto();
				selectEvent(itsStepper.getCurrentEvent());
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction(">|", "Forward step over")
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
				itsStepper.forwardStepOver();
				selectEvent(itsStepper.getCurrentEvent());
			}
		}));
		
		return theToolbar;
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
		for (Iterator<ILogEvent> theIterator = theEventPath.iterator(); theIterator.hasNext();)
		{
			ILogEvent theEvent = theIterator.next();
			
			theNode = theNode.getNode(theEvent);
			if (theIterator.hasNext()) theNode.expand();
			theNode.invalidate();
		}
		
		itsUpdated = false;
		
		// the layout must be ready.
		itsRootNode.invalidate();
		itsRootNode.checkValid(); 
		itsTreePanel.setShownBounds(null); // TODO: hack to recompute the size.
		
		Rectangle2D theNodeBounds = theNode.getBounds(null);
		Rectangle theBounds = itsTreePanel.localToPixel(null, theNode, theNodeBounds);
		theBounds.width = 10;
		itsTreePanel.scrollRectToVisible(theBounds);
	}
		
	@Override
	public void addNotify()
	{
		super.addNotify();
		itsSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
		itsSeed.pRootEvent().addHardListener(itsRootEventListener);
		
		update();
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
