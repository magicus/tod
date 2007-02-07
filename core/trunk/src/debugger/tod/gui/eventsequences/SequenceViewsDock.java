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
package tod.gui.eventsequences;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import tod.gui.view.LogView;
import zz.utils.ItemAction;
import zz.utils.properties.ArrayListProperty;
import zz.utils.properties.IListProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyUtils;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.GridStackLayout;

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
			SequencePanel thePanel = new SequencePanel(aSeed.createView(getLogView()));
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
		private JComponent itsStripe;
		
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
			setLayout(new BorderLayout(5, 0));
			setPreferredSize(new Dimension(10, 80));
			
			itsStripe = itsView.getEventStripe();
			
			add (itsStripe, BorderLayout.CENTER);
			add (createNorthPanel(), BorderLayout.NORTH);
		}
		
		public JPanel createNorthPanel()
		{
			JPanel thePanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
			thePanel.add (new JLabel(itsView.getTitle()));
			
			JToolBar theToolBar = new JToolBar();
			theToolBar.setFloatable(false);
			for (ItemAction theAction : itsView.getActions())
			{
				theToolBar.add(theAction);
			}
			
			thePanel.add(theToolBar);
			
			return thePanel;
		}
	}
	
}
