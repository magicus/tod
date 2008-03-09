/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.locationselector;

import java.awt.Dimension;
import java.util.StringTokenizer;

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
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;
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
						theCurrentNode.addClassNode(theClass, itsShowMembers);
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
			final BehaviorListModel theListModel = new BehaviorListModel(getStructureDatabase());
			
			JList theList = new JList()
			{
				@Override
				public Dimension getPreferredSize()
				{
					theListModel.setHideAway(true);
					Dimension thePreferredSize = super.getPreferredSize();
					theListModel.setHideAway(false);
					return thePreferredSize;
				}
			};
			
			theList.setModel(theListModel);
		
			setLayout(new StackLayout());
			add(new JScrollPane(theList));
		}
		
	}
	
	private static class BehaviorListModel extends AbstractListModel
	{
		private IStructureDatabase itsStructureDatabase;
		private int itsSize;
		
		/**
		 * This flag permits to simulate we are empty
		 * during the call to getPreferredSize, otherwise the full
		 * model is scanned.
		 */
		private boolean itsHideAway = false;
		
		public BehaviorListModel(IStructureDatabase aStructureDatabase)
		{
			itsStructureDatabase = aStructureDatabase;
			itsSize = itsStructureDatabase.getStats().nBehaviors;
		}

		public void setHideAway(boolean aHideAway)
		{
			itsHideAway = aHideAway;
		}

		public Object getElementAt(int aIndex)
		{
			if (itsHideAway) return "A";
			
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
	
	
}