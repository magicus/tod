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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ComponentSampleModel;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.Scrollable;

import tod.core.database.browser.BrowserUtils;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.Stepper;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.IGUIManager;
import tod.gui.Hyperlinks.ISeedFactory;
import tod.gui.controlflow.tree.CFlowTree;
import tod.gui.controlflow.watch.WatchPanel;
import tod.gui.eventlist.MuralScroller;
import tod.gui.eventlist.MuralScroller.UnitScroll;
import tod.gui.formatter.EventFormatter;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.Seed;
import tod.gui.view.LogView;
import zz.csg.api.GraphicUtils;
import zz.csg.display.GraphicPanel;
import zz.utils.SimpleAction;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;
import zz.utils.ui.UIUtils;

public class CFlowView extends LogView
{
	public static final boolean SHOW_PARENT_FRAMES = false;
	
	private CFlowSeed itsSeed;
	private CFlowTree itsCFlowTree;
	
	private EventFormatter itsFormatter;
	
	private Stepper itsStepper;
	
	private GraphicPanel itsTreePanel;
	private WatchPanel itsWatchPanel;
	
	private MuralScroller itsScroller;
	
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
	
	private IPropertyListener<IBehaviorCallEvent> itsParentListener = new PropertyListener<IBehaviorCallEvent>()
	{
		@Override
		public void propertyChanged(IProperty<IBehaviorCallEvent> aProperty, IBehaviorCallEvent aOldValue, IBehaviorCallEvent aNewValue)
		{
			setParent(aNewValue);
		}
	};
	


	private JSplitPane itsSplitPane;
	
	public CFlowView(IGUIManager aGUIManager, ILogBrowser aLogBrowser, CFlowSeed aSeed)
	{
		super (aGUIManager, aLogBrowser);
		itsSeed = aSeed;
		itsFormatter = new EventFormatter(aLogBrowser);

		itsStepper = new Stepper(getLogBrowser(), itsSeed.getThread());
	}

	public CFlowSeed getSeed()
	{
		return itsSeed;
	}

	public EventFormatter getFormatter()
	{
		return itsFormatter;
	}

	@Override
	public void init()
	{
		setLayout(new BorderLayout());
		
		// Create tree panel
		itsCFlowTree = new CFlowTree(this);
		
		itsTreePanel = new GraphicPanel()
		{
			@Override
			public boolean getScrollableTracksViewportHeight()
			{
				return true;
			}
			
			@Override
			public boolean getScrollableTracksViewportWidth()
			{
				return false;
			}
		};
		itsTreePanel.setTransform(new AffineTransform());
		itsTreePanel.setRootNode(itsCFlowTree);
		
		itsScroller = new MuralScroller();
		itsScroller.eUnitScroll().addListener(new IEventListener<UnitScroll>()
				{
					public void fired(IEvent< ? extends UnitScroll> aEvent, UnitScroll aData)
					{
						switch (aData)
						{
						case UP:
							itsCFlowTree.backward(1);
							break;
							
						case DOWN:
							itsCFlowTree.forward(1);
							break;
						}
					}
				});
		itsScroller.pTrackScroll().addHardListener(new PropertyListener<Long>()
				{
					@Override
					public void propertyChanged(IProperty<Long> aProperty, Long aOldValue, Long aNewValue)
					{
						itsCFlowTree.setTimestamp(aNewValue);
					}
				});
		
		JScrollPane theTreeScrollPane = new MyScrollPane(itsTreePanel);
		theTreeScrollPane.setWheelScrollingEnabled(false);
		
		theTreeScrollPane.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent aE)
			{
				itsCFlowTree.setSize(
						itsCFlowTree.pBounds().get().getWidth(), 
						itsTreePanel.getHeight());
				itsCFlowTree.update();
			}
		});
		

		
		JPanel theCFlowTreePanel = new JPanel(new BorderLayout());
		theCFlowTreePanel.add(theTreeScrollPane, BorderLayout.CENTER);
		theCFlowTreePanel.add(itsScroller, BorderLayout.EAST);
		
		JPanel theCFlowPanel = new JPanel(new BorderLayout());
//		theCFlowPanel.add(theTreeScrollPane, BorderLayout.CENTER);
		theCFlowPanel.add(theCFlowTreePanel, BorderLayout.CENTER);
		theCFlowPanel.add(createToolbar(), BorderLayout.NORTH);
		
		// Create watch panel
		itsWatchPanel = new WatchPanel(this);

		itsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		itsSplitPane.setResizeWeight(0.5);
		itsSplitPane.setLeftComponent(theCFlowPanel);
		itsSplitPane.setRightComponent(new JScrollPane(itsWatchPanel));
		
		add(itsSplitPane, BorderLayout.CENTER);
		
		setParent(getSeed().pParentEvent().get());
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
				ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
				if (theSelectedEvent != null)
				{
					itsStepper.setCurrentEvent(theSelectedEvent);
					itsStepper.stepOut();
					selectEvent(itsStepper.getCurrentEvent());
				}
				else 
				{
					IBehaviorCallEvent theParentEvent = itsSeed.pParentEvent().get();
					if (theParentEvent != null)
					{
						itsSeed.pParentEvent().set(theParentEvent.getParent());
					}
				}
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

		for (int i=0;i<theToolbar.getComponentCount();i++)
		{
			((JButton) theToolbar.getComponent(i)).setMargin(UIUtils.NULL_INSETS);
		}
	
		
		return theToolbar;
	}
	
	private void setParent(IBehaviorCallEvent aEvent)
	{
		IParentEvent theParentEvent = aEvent != null ?
				aEvent
				: getSeed().pRootEvent().get();


		itsCFlowTree.setParent(theParentEvent);
		IEventBrowser theBrowser = theParentEvent.getChildrenBrowser();
		itsScroller.set(
				theBrowser, 
				BrowserUtils.getFirstTimestamp(theBrowser),
				BrowserUtils.getLastTimestamp(theBrowser));
	}
	
	private void update()
	{
		ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
		
		if (theSelectedEvent != null)
		{
			itsWatchPanel.showStackFrame();
			showEvent(theSelectedEvent);
		}
		
		if (theSelectedEvent != null) getGUIManager().gotoEvent(theSelectedEvent);
	}
	
	private void showEvent (ILogEvent aEvent)
	{
		IBehaviorCallEvent theCurrentParent = getSeed().pParentEvent().get();
		if (! aEvent.getParentPointer().equals(theCurrentParent.getPointer()))
		{
			getSeed().pParentEvent().set(aEvent.getParent());
		}
		
		getSeed().pSelectedEvent().set(aEvent);
		
		Rectangle2D theBounds = itsCFlowTree.makeVisible(aEvent);
		if (theBounds != null)
		{
			Rectangle thePixelBounds = itsTreePanel.localToPixel(
					itsTreePanel.getRootNode().getContext(), 
					itsCFlowTree, 
					theBounds);
			
			thePixelBounds.x -= 50;
			itsTreePanel.scrollRectToVisible(thePixelBounds);
		}
	}
		
	@Override
	public void addNotify()
	{
		super.addNotify();
		itsSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
		itsSeed.pRootEvent().addHardListener(itsRootEventListener);
		itsSeed.pParentEvent().addHardListener(itsParentListener);
		
		itsSplitPane.setDividerLocation(400);
		
		update();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsSeed.pSelectedEvent().removeListener(itsSelectedEventListener);
		itsSeed.pRootEvent().removeListener(itsRootEventListener);
		itsSeed.pParentEvent().removeListener(itsParentListener);
	}
	
//	/**
//	 * Returns the path to the deepest {@link IParentEvent} whose span contains 
//	 * both specified timestamps.
//	 */
//	private List<IParentEvent> getPathForRange(double aT1, double aT2)
//	{
//		IParentEvent theRoot = (IParentEvent) itsBrowser.getRoot();
//		List<IParentEvent> thePath = new ArrayList<IParentEvent>();
//		computePathForRange(thePath, theRoot, aT1, aT2);
//		return thePath;
//	}
//
//	private void computePathForRange(
//			List<IParentEvent> aPath, 
//			IParentEvent aRoot, 
//			double aT1, 
//			double aT2)
//	{
//		aPath.add(aRoot);
//		
//		for (ILogEvent theChild : aRoot.getChildren())
//		{
//			if (theChild instanceof IParentEvent)
//			{
//				IParentEvent theContainer = (IParentEvent) theChild;
//				if (theContainer.getFirstTimestamp() <= aT1 && theContainer.getLastTimestamp() >= aT2)
//				{
//					computePathForRange(aPath, theContainer, aT1, aT2);
//					break;
//				}
//			}
//		}
//	}
//	
//	/**
//	 * Returns the path to the deepest {@link IParentEvent} whose span contains 
//	 * the specified timestamp.
//	 */
//	private List<IParentEvent> getPathForTimestamp(double aT)
//	{
//		return getPathForRange(aT, aT);
//	}
	
	public void selectEvent(ILogEvent aEvent)
	{
		itsSeed.pSelectedEvent().set(aEvent);
	}
	
	public boolean isEventSelected(ILogEvent aEvent)
	{
		ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
		return theSelectedEvent != null && theSelectedEvent.equals(aEvent);
	}
	
	private static class MyScrollPane extends JScrollPane
	{
		private MyScrollPane(Component aView)
		{
			super(aView, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			getViewport().setBackground(GraphicPanel.BACKGROUND_PAINT);
		}
	}
	
	private class CFlowSeedFactory implements ISeedFactory
	{

		public Seed behaviorSeed(IBehaviorInfo aBehavior)
		{
			return null;
		}

		public Seed cflowSeed(final ILogEvent aEvent)
		{
			return new Seed()
			{
				public void open()
				{
					showEvent(aEvent);
				}
			};
		}

		public Seed objectSeed(ObjectId aObjectId)
		{
			return null;
		}

		public Seed typeSeed(ITypeInfo aType)
		{
			return null;
		}
		
	}
}
