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
package tod.gui.view.structure;

import java.util.StringTokenizer;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListModel;
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
import zz.utils.tree.SimpleTree;
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
	private final StructureView itsStructureView;
	private final IStructureDatabase itsStructureDatabase;
	
	public LocationSelectorPanel(IStructureDatabase aStructureDatabase, StructureView aStructureView)
	{
		itsStructureDatabase = aStructureDatabase;
		itsStructureView = aStructureView;
		createUI();
	}

	private void createUI()
	{
		JTabbedPane theTabbedPane = new JTabbedPane();
		
		theTabbedPane.addTab("Packages", new TreeSelector());
		theTabbedPane.addTab("Behaviors", new BehaviorIdSelector());
		
		setLayout(new StackLayout());
		add(theTabbedPane);
	}
	
	public IStructureDatabase getStructureDatabase()
	{
		return itsStructureDatabase;
	}
	
	public void show(ILocationInfo aLocation)
	{
		itsStructureView.showNode(aLocation);
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
			IStructureDatabase theDatabase = getStructureDatabase();
			IClassInfo[] theClasses = theDatabase.getClasses();
			
			SimpleTree<ILocationInfo> theTree = new SimpleTree<ILocationInfo>()
			{
				
				protected SimpleTreeNode<ILocationInfo> createRoot()
				{
					return new PackageNode(this, new PackageInfo("Classes"));
				}
			};
			PackageNode theRoot = (PackageNode) theTree.getRoot();
			
			for (IClassInfo theClass : theClasses)
			{
				String theName = theClass.getName();
				StringTokenizer theTokenizer = new StringTokenizer(theName, ".");
				
				PackageNode theCurrentNode = theRoot;
				while (theTokenizer.hasMoreTokens())
				{
					String theToken = theTokenizer.nextToken();
					if (theTokenizer.hasMoreTokens())
					{
						// Token is still part of package name
						theCurrentNode = theCurrentNode.getPackageNode(theToken);
					}
					else
					{
						// We reached the class name
						theCurrentNode.addClassNode(theClass);
					}
					
				}
			}
			
			return new SwingTreeModel(theTree);
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
			JList theList = new JList();
			theList.setModel(new BehaviorListModel(getStructureDatabase()));
		
			setLayout(new StackLayout());
			add(new JScrollPane(theList));
		}
		
	}
	
	private static class BehaviorListModel extends AbstractListModel
	{
		private IStructureDatabase itsStructureDatabase;
		private IBehaviorInfo[] itsBehaviors;
		
		public BehaviorListModel(IStructureDatabase aStructureDatabase)
		{
			itsStructureDatabase = aStructureDatabase;
			itsBehaviors = itsStructureDatabase.getBehaviors();
		}

		public Object getElementAt(int aIndex)
		{
			IBehaviorInfo theBehavior = itsBehaviors[aIndex];
			return ""+theBehavior.getId()+" "+theBehavior.getType().getName()+"."+theBehavior.getName();
		}

		public int getSize()
		{
			return itsBehaviors.length;
		}
		
	}
	
	
}
