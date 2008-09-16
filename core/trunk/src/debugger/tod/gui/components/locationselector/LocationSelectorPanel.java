/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.components.locationselector;

import java.awt.Dimension;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import tod.Util;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.core.database.structure.tree.LocationNode;
import tod.core.database.structure.tree.StructureTreeBuilders;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.tree.SimpleTreeNode;
import zz.utils.tree.SwingTreeModel;
import zz.utils.ui.StackLayout;
import zz.utils.ui.UniversalRenderer;

/**
 * This panel permits to select a location in the structure database.
 * @author gpothier
 */
public class LocationSelectorPanel extends JPanel
{
	private final IRWProperty<ILocationInfo> pSelectedLocation = new SimpleRWProperty<ILocationInfo>();
	private final IStructureDatabase itsStructureDatabase;
	private final boolean itsShowMembers;
	
	public LocationSelectorPanel(IStructureDatabase aStructureDatabase, boolean aShowMembers)
	{
		itsStructureDatabase = aStructureDatabase;
		itsShowMembers = aShowMembers;
		createUI();
	}

	private void createUI()
	{
		JTabbedPane theTabbedPane = new JTabbedPane();
		
		theTabbedPane.addTab("Packages", new TreeSelector());
		theTabbedPane.addTab("Behaviors", new BehaviorIdSelector());
		theTabbedPane.addTab("Probes", new ProbeIdSelector());
		
		setLayout(new StackLayout());
		add(theTabbedPane);
	}
	
	public IStructureDatabase getStructureDatabase()
	{
		return itsStructureDatabase;
	}
	
	/**
	 * The property that contains the currently selected location node.
	 */
	public IRWProperty<ILocationInfo> pSelectedLocation()
	{
		return pSelectedLocation;
	}
	
	/**
	 * Called when the user selects a location.
	 */
	public void show(ILocationInfo aLocation)
	{
		pSelectedLocation.set(aLocation);
	}
	
	/**
	 * Presents all the classes/behaviors in a tree.
	 * @author gpothier
	 */
	private class TreeSelector extends JPanel
	{
		public TreeSelector()
		{
			createUI();
		}

		private void createUI()
		{
			JTree theTree = new JTree(createTreeModel());
			theTree.setCellRenderer(new MyRenderer());
			theTree.setShowsRootHandles(false);
			
			theTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			theTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
			{
				public void valueChanged(TreeSelectionEvent aEvent)
				{
					LocationNode theNode = (LocationNode) aEvent.getPath().getLastPathComponent();
					LocationSelectorPanel.this.show(theNode.getLocation());
				}
			});
			
			setLayout(new StackLayout());
			add(new JScrollPane(theTree));
		}
		
		private TreeModel createTreeModel()
		{
			return new SwingTreeModel(StructureTreeBuilders.createClassTree(
					getStructureDatabase(), 
					itsShowMembers, 
					itsShowMembers));
		}
	}
	
	/**
	 * Renderer for the classes tree.
	 * @author gpothier
	 */
	private static class MyRenderer extends UniversalRenderer<SimpleTreeNode<ILocationInfo>>
	{
		@Override
		protected String getName(SimpleTreeNode<ILocationInfo> aNode)
		{
			ILocationInfo theLocation = aNode.pValue().get();
			
			if (theLocation instanceof IClassInfo)
			{
				IClassInfo theClass = (IClassInfo) theLocation;
				return Util.getSimpleName(theClass.getName());
			}
			else if (theLocation instanceof IMemberInfo)
			{
				IMemberInfo theMember = (IMemberInfo) theLocation;
				return Util.getFullName(theMember);
			}
			else
			{
				return theLocation.getName();
			}
		}
	}

	/**
	 * Peer of {@link BigListModel}
	 * @author gpothier
	 */
	private static class BigJList extends JList
	{
		public BigJList()
		{
		}

		public BigJList(BigListModel aDataModel)
		{
			super(aDataModel);
		}

		@Override
		public BigListModel getModel()
		{
			return (BigListModel) super.getModel();
		}
		
		@Override
		public Dimension getPreferredSize()
		{
			getModel().setHideAway(true);
			Dimension thePreferredSize = super.getPreferredSize();
			getModel().setHideAway(false);
			return thePreferredSize;
		}
	}
	
	/**
	 * A list model for huge list for which we do not want the contents
	 * to be retrieved just for calculating the preferred size.
	 * @author gpothier
	 */
	private static abstract class BigListModel extends AbstractListModel
	{
		/**
		 * This flag permits to simulate we are empty
		 * during the call to getPreferredSize, otherwise the full
		 * model is scanned.
		 */
		private boolean itsHideAway = false;
		
		public void setHideAway(boolean aHideAway)
		{
			itsHideAway = aHideAway;
		}
		
		public final Object getElementAt(int aIndex)
		{
			if (itsHideAway) return "A";
			else return getElementAt0(aIndex);
		}
		
		protected abstract Object getElementAt0(int aIndex);
	}
	

	/**
	 * Presents behaviors by id.
	 * @author gpothier
	 */
	private class BehaviorIdSelector extends JPanel
	{
		public BehaviorIdSelector()
		{
			createUI();
		}

		private void createUI()
		{
			final BehaviorListModel theListModel = new BehaviorListModel(getStructureDatabase());
			
			JList theList = new BigJList(theListModel);
			setLayout(new StackLayout());
			add(new JScrollPane(theList));
		}
		
	}
	
	private static class BehaviorListModel extends BigListModel
	{
		private IStructureDatabase itsStructureDatabase;
		private int itsSize;
		
		public BehaviorListModel(IStructureDatabase aStructureDatabase)
		{
			itsStructureDatabase = aStructureDatabase;
			itsSize = itsStructureDatabase.getStats().nBehaviors;
		}

		@Override
		protected Object getElementAt0(int aIndex)
		{
			IBehaviorInfo theBehavior = itsStructureDatabase.getBehavior(aIndex, false);
			return theBehavior != null ?
					""+aIndex+" "+theBehavior.getType().getName()+"."+theBehavior.getName()
					: ""+aIndex;
		}

		public int getSize()
		{
			return itsSize;
		}
	}
	
	/**
	 * Presents probes by id.
	 * @author gpothier
	 */
	private class ProbeIdSelector extends JPanel
	{
		public ProbeIdSelector()
		{
			createUI();
		}

		private void createUI()
		{
			final ProbeListModel theListModel = new ProbeListModel(getStructureDatabase());
			
			JList theList = new BigJList(theListModel);
			setLayout(new StackLayout());
			add(new JScrollPane(theList));
		}
		
	}
	
	private static class ProbeListModel extends BigListModel
	{
		private IStructureDatabase itsStructureDatabase;
		private int itsSize;
		
		public ProbeListModel(IStructureDatabase aStructureDatabase)
		{
			itsStructureDatabase = aStructureDatabase;
			itsSize = itsStructureDatabase.getStats().nProbes;
		}

		@Override
		protected Object getElementAt0(int aIndex)
		{
			ProbeInfo theProbeInfo = itsStructureDatabase.getProbeInfo(aIndex);
			return theProbeInfo != null ?
					""+aIndex+" "+theProbeInfo
					: ""+aIndex;
		}

		public int getSize()
		{
			return itsSize;
		}
	}
	
	
}
