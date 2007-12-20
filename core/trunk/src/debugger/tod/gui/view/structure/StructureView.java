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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.ILocationInfo;
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
		itsInfoHolder = new JPanel();
		
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
			@Override
			protected SimpleTreeNode<ILocationInfo> createRoot()
			{
				return new PackageNode(this, new PackageInfo("root!"));
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
	
	private static class PackageNode extends SimpleTreeNode<ILocationInfo>
	{
		public PackageNode(SimpleTree<ILocationInfo> aTree, PackageInfo aValue)
		{
			super(aTree, false);
			pValue().set(aValue);
		}
		
		/**
		 * Retrieves the package node corresponding to the given name,
		 * creating it if needed.
		 */
		public PackageNode getPackageNode(String aName)
		{
			int theIndex = Collections.binarySearch(
					pChildren().get(),
					aName, 
					PackageComparator.PACKAGE);
			
			if (theIndex >= 0) 
			{
				// return existing node
				return (PackageNode) pChildren().get(theIndex);
			}
			else
			{
				// create new node
				PackageInfo thePackage = new PackageInfo(aName);
				PackageNode theNode = new PackageNode(getTree(), thePackage);
				pChildren().add(-theIndex-1, theNode);
				return theNode;
			}
		}
		
		/**
		 * Retrieves the class node corresponding to the given name.
		 */
		public ClassNode getClassNode(String aName)
		{
			int theIndex = Collections.binarySearch(
					pChildren().get(), 
					aName,
					PackageComparator.CLASS);
			
			if (theIndex < 0) throw new RuntimeException("Class node not found: "+aName); 
			return (ClassNode) pChildren().get(theIndex);
		}
		
		/**
		 * Adds a new class node
		 */
		public ClassNode addClassNode(IClassInfo aClassInfo)
		{
			int theIndex = Collections.binarySearch(
					pChildren().get(), 
					aClassInfo.getName(),
					PackageComparator.CLASS);
			
			if (theIndex >= 0) throw new RuntimeException("Class node already exists: "+aClassInfo); 

			ClassNode theNode = new ClassNode(getTree(), aClassInfo);
			pChildren().add(-theIndex-1, theNode);
			return theNode;
		}
		
	}
	
	private static class ClassNode extends SimpleTreeNode<ILocationInfo>
	{
		public ClassNode(SimpleTree<ILocationInfo> aTree, IClassInfo aClassInfo)
		{
			super(aTree, false);
			pValue().set(aClassInfo);
		}
	}

	
	private static class PackageInfo implements ILocationInfo
	{
		private String itsName;

		public PackageInfo(String aName)
		{
			itsName = aName;
		}

		public IStructureDatabase getDatabase()
		{
			return null;
		}

		public int getId()
		{
			return 0;
		}

		public String getName()
		{
			return itsName;
		}
	}
	
	/**
	 * Compares packages and classes.
	 * Packages are always before classes, otherwise lexicographic order is used.
	 * @author gpothier
	 */
	private static class PackageComparator implements Comparator
	{
		public static PackageComparator PACKAGE = new PackageComparator(true);
		public static PackageComparator CLASS = new PackageComparator(false);

		/**
		 * If true, compares against package names (package names always appear before
		 * class names).
		 */
		private boolean itsForPackage;
		
		private PackageComparator(boolean aForPackage)
		{
			itsForPackage = aForPackage;
		}
		
		public int compare(Object o1, Object o2)
		{
			SimpleTreeNode<ILocationInfo> node = (SimpleTreeNode<ILocationInfo>) o1;
			String name = (String) o2;
			
			ILocationInfo l = node.pValue().get();
			boolean p = l instanceof PackageInfo;
			
			if (p != itsForPackage) return p ? 1 : -1;
			else return l.getName().compareTo(name);
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
			return theLocation.getName();
		}
		
	}
}
