/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.view.LogView;

import zz.csg.api.IDisplay;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.display.GraphicPanel;
import zz.utils.ItemAction;
import zz.utils.properties.ArrayListProperty;
import zz.utils.properties.IListProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyUtils;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.GridStackLayout;
import zz.utils.ui.StackLayout;

/**
 * A component that displays a stack of event views.
 * @author gpothier
 */
public class SequenceViewsDock extends JPanel
{
	private IListProperty<IEventSequenceSeed> pSeeds = new ArrayListProperty<IEventSequenceSeed>(this)
	{
		@Override
		protected void elementAdded(int aIndex, IEventSequenceSeed aSeed)
		{
			// TODO: provide proper display
			SequencePanel thePanel = new SequencePanel(aSeed.createView(itsDisplay, getLogView()));
			itsViewsPanel.add(thePanel, aIndex);
			itsViewsPanel.revalidate();
			itsViewsPanel.repaint();
		}
		
		@Override
		protected void elementRemoved(int aIndex, IEventSequenceSeed aSeed)
		{
			itsViewsPanel.remove(aIndex);
			itsViewsPanel.revalidate();
			itsViewsPanel.repaint();
		}
	};
	
	private IRWProperty<Long> pStart = new SimpleRWProperty<Long>(this);
	
	private IRWProperty<Long> pEnd = new SimpleRWProperty<Long>(this);

	
	private JPanel itsViewsPanel;
	
	// TODO: Temp.
	private GraphicPanel itsDisplay;

	private final LogView itsLogView;
	
	public SequenceViewsDock(LogView aLogView)
	{
		itsLogView = aLogView;
		createUI();
	}
	
	private void createUI()
	{
		setLayout(new BorderLayout());
		itsViewsPanel = new JPanel (new GridStackLayout(1, 0, 0, true, false));
		add (new JScrollPane(itsViewsPanel), BorderLayout.CENTER);
		
		itsDisplay = new GraphicPanel();
		itsDisplay.setPreferredSize(new Dimension(1, 1));
		add (itsDisplay, BorderLayout.SOUTH);
	}


	public LogView getLogView()
	{
		return itsLogView;
	}

	/**
	 * First timestamp of the events displayed in the all the sequences of this dock.
	 */
	public IRWProperty<Long> pStart ()
	{
		return pStart;
	}
	
	/**
	 * Last timestamp of the events displayed in the all the sequences of this dock.
	 */
	public IRWProperty<Long> pEnd ()
	{
		return pEnd;
	}
	
	/**
	 * The seeds whose views are displayed in this dock
	 */
	public IListProperty<IEventSequenceSeed> pSeeds()
	{
		return pSeeds;
	}

	
	private class SequencePanel extends JPanel
	{
		private IEventSequenceView itsView;
		private IRectangularGraphicObject itsStripe;
		
		/**
		 * We keep references to connectors so that we can disconnect.
		 */
		private PropertyUtils.Connector<Long>[] itsConnectors = new PropertyUtils.Connector[2];

		public SequencePanel(IEventSequenceView aView)
		{
			itsView = aView;
			createUI();
		}
		
		@Override
		public void addNotify()
		{
			super.addNotify();
			itsConnectors[0] = PropertyUtils.connect(pStart(), itsView.pStart(), true, true);
			itsConnectors[1] = PropertyUtils.connect(pEnd(), itsView.pEnd(), true, true);
		}
		
		@Override
		public void removeNotify()
		{
			super.removeNotify();
			itsConnectors[0].disconnect();
			itsConnectors[1].disconnect();
		}
		
		private void createUI()
		{
			setLayout(new BorderLayout());
			setPreferredSize(new Dimension(10, 80));
			
			itsStripe = itsView.getEventStripe();
			
			final GraphicPanel theStripePanel = new GraphicPanel();
			theStripePanel.setTransform(new AffineTransform());
			theStripePanel.setRootNode(itsStripe);
			
			add (theStripePanel, BorderLayout.CENTER);
			
			add (createNorthPanel(), BorderLayout.NORTH);
			
			theStripePanel.addComponentListener(new ComponentAdapter()
					{
						@Override
						public void componentResized(ComponentEvent aE)
						{
							Rectangle2D.Double theBounds = new Rectangle2D.Double(
									0, 
									0, 
									theStripePanel.getWidth(), 
									theStripePanel.getHeight());
							
							itsStripe.pBounds().set(theBounds);
						}
					});
		}
		
		public JPanel createNorthPanel()
		{
			JPanel thePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			thePanel.add (new JLabel(itsView.getTitle()));
			
			JToolBar theToolBar = new JToolBar();
			for (ItemAction theAction : itsView.getActions())
			{
				theToolBar.add(theAction);
			}
			
			thePanel.add(theToolBar);
			
			return thePanel;
		}
	}
	
}
