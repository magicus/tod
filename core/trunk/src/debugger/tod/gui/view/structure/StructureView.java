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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import tod.Util;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.gui.IGUIManager;
import tod.gui.MinerUI;
import tod.gui.seed.StructureSeed;
import tod.gui.view.LogView;
import zz.utils.tree.SimpleTree;
import zz.utils.tree.SimpleTreeNode;
import zz.utils.tree.SwingTreeModel;
import zz.utils.ui.StackLayout;
import zz.utils.ui.UniversalRenderer;

/**
 * Provides access to the structural database.
 * @author gpothier
 */
public class StructureView extends LogView
{
	private static final String PROPERTY_SPLITTER_POS = "structureView.splitterPos";
	private final StructureSeed itsSeed;
	private JTree itsTree;
	private JPanel itsInfoHolder;
	private JSplitPane itsSplitPane;

	
	public StructureView(IGUIManager aGUIManager, ILogBrowser aLog, StructureSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
	}
	
	@Override
	public void init()
	{
		itsTree = new JTree(createTreeModel());
		itsTree.setCellRenderer(new MyRenderer());
		itsTree.setShowsRootHandles(false);
		
		itsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		itsTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent aEvent)
			{
				LocationNode theNode = (LocationNode) aEvent.getPath().getLastPathComponent();
				showNode(theNode.getLocation());
			}
		});
		
		itsInfoHolder = new JPanel(new StackLayout());
		
		itsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		itsSplitPane.setResizeWeight(0.5);
		itsSplitPane.setLeftComponent(new JScrollPane(itsTree));
		itsSplitPane.setRightComponent(itsInfoHolder);
		
		setLayout(new StackLayout());
		add(itsSplitPane);
	}
	
	private TreeModel createTreeModel()
	{
		IStructureDatabase theDatabase = getLogBrowser().getStructureDatabase();
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
	
	/**
	 * Shows the information corresponding to the given node.
	 */
	private void showNode(ILocationInfo aLocation)
	{
		if (aLocation instanceof IBehaviorInfo)
		{
			IBehaviorInfo theBehavior = (IBehaviorInfo) aLocation;
			showPanel(new BehaviorPanel(theBehavior));
		}
	}
	
	/**
	 * Changes the currently displayed info panel
	 */
	private void showPanel(JComponent aComponent)
	{
		itsInfoHolder.removeAll();
		itsInfoHolder.add(aComponent);
		itsInfoHolder.revalidate();
		itsInfoHolder.repaint();
	}

	@Override
	public void addNotify()
	{
		int theSplitterPos = MinerUI.getIntProperty(
				getGUIManager(), 
				PROPERTY_SPLITTER_POS, 400);
		
		itsSplitPane.setDividerLocation(theSplitterPos);
		
		super.addNotify();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		
		getGUIManager().setProperty(
				PROPERTY_SPLITTER_POS, 
				""+itsSplitPane.getDividerLocation());
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
}
