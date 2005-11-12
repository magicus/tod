/*
 * Created on Nov 3, 2004
 */
package reflex.lib.logging.miner.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import tod.core.LocationRegistrer;
import tod.core.model.structure.LocationInfo;
import tod.core.model.structure.LocationTreeBuilder;
import tod.core.model.structure.LocationTreeBuilder.Node;
import tod.core.model.trace.ILocationTrace;

/**
 * This panel permits to select a source code location.
 * Locations are subclasses of 
 * {@link tod.core.model.structure.LocationInfo}
 * @author gpothier
 */
public class LocationSelector extends JPanel implements TreeSelectionListener
{
	private ILocationTrace itsLocationTrace;
	
	private MyTreeModel itsModel;
	private JTree itsTree;
	
	private List/*<ILocationSelectionListener>*/ itsListeners =
		new ArrayList/*<ILocationSelectionListener>*/();

	private LocationTreeBuilder itsLocationTreeBuilder;
	
	public LocationSelector(ILocationTrace aLocationTrace)
	{
		itsLocationTrace = aLocationTrace;
		itsLocationTreeBuilder = LocationTreeBuilder.getInstance(itsLocationTrace);
		createUI();
	}
	
	private void createUI ()
	{
		itsModel = new MyTreeModel();
		itsTree = new JTree(itsModel);
		itsTree.setCellRenderer(new MyRenderer());
		
		itsTree.getSelectionModel().addTreeSelectionListener(this);
		
		setPreferredSize(new Dimension(300, 300));
		setLayout(new BorderLayout());
		add (new JScrollPane (itsTree), BorderLayout.CENTER);
		
		JButton theRefreshButton = new JButton("Refresh");
		theRefreshButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				refresh();
			}
		});
		add (theRefreshButton, BorderLayout.NORTH);
		
		refresh();
	}
	
	protected void refresh()
	{
		itsLocationTreeBuilder.rebuild();
		Node theNode = itsLocationTreeBuilder.getRootNode();
		itsModel.setRoot(theNode);
	}
	
	public void valueChanged(TreeSelectionEvent aE)
	{
		List/*<LocationInfo>*/ theSelectedLocations = 
			new ArrayList/*<LocationInfo>*/();
		
		TreePath[] thePaths = itsTree.getSelectionPaths();
//		for (TreePath thePath : thePaths)
//		{
		for (int i=0;i<thePaths.length;i++)
		{
			TreePath thePath = thePaths[i];
			
			LocationTreeBuilder.Node theNode = 
				(Node) thePath.getLastPathComponent();
			
			if (theNode instanceof LocationTreeBuilder.TypeNode)
			{
				LocationTreeBuilder.TypeNode theTypeNode = (LocationTreeBuilder.TypeNode) theNode;
				theSelectedLocations.add(theTypeNode.getTypeInfo());
			}
			else if (theNode instanceof LocationTreeBuilder.MemberNode)
			{
				LocationTreeBuilder.MemberNode theMemberNode = (LocationTreeBuilder.MemberNode) theNode;
				theSelectedLocations.add(theMemberNode.getMemberInfo());
			}
		}
		
		fireSelectionChanged(theSelectedLocations);
	}
	
	public void addSelectionListener (ILocationSelectionListener aListener)
	{
		itsListeners.add(aListener);
	}

	public void removeSelectionListener (ILocationSelectionListener aListener)
	{
		itsListeners.remove(aListener);
	}
	
	protected void fireSelectionChanged(List/*<LocationInfo>*/ aSelectedLocations)
	{
//		for (ILocationSelectionListener theListener : itsListeners)
//			theListener.selectionChanged(aSelectedLocations);
		for (Iterator theIterator = itsListeners.iterator(); theIterator.hasNext();)
		{
			ILocationSelectionListener theListener = (ILocationSelectionListener) theIterator.next();
			theListener.selectionChanged(aSelectedLocations);
		}
	}

	private static class MyTreeModel extends DefaultTreeModel
	{
		private Node itsRoot;

		public MyTreeModel()
		{
			super(null);
		}
		
		public Object getRoot()
		{
			return itsRoot;
		}
		
		public void setRoot(Node aRoot)
		{
			itsRoot = aRoot;
			reload();
		}
		
		public void reload()
		{
			fireTreeStructureChanged(this, new Object[] {itsRoot}, null, null);
		}
		
		public Object getChild(Object aParent, int aIndex)
		{
			LocationTreeBuilder.Node theParent = (Node) aParent;
			return theParent.getChild(aIndex);
		}
		
		public int getChildCount(Object aParent)
		{
			LocationTreeBuilder.Node theParent = (Node) aParent;
			return theParent.getSize();
		}
		
		public int getIndexOfChild(Object aParent, Object aChild)
		{
			LocationTreeBuilder.Node theParent = (Node) aParent;
			LocationTreeBuilder.Node theChild = (Node) aChild;
			
			return theParent.indexOf(theChild);
		}
		
		public boolean isLeaf(Object aNode)
		{
			return aNode instanceof LocationTreeBuilder.MemberNode;
		}
		
		
	}
	
	private static class MyRenderer extends DefaultTreeCellRenderer
	{
		public Component getTreeCellRendererComponent(JTree aTree, Object aValue, boolean aSel, boolean aExpanded,
				boolean aLeaf, int aRow, boolean aHasFocus)
		{
			LocationTreeBuilder.Node theNode = (Node) aValue;
			String theValue = theNode.getName();
			return super.getTreeCellRendererComponent(aTree, theValue, aSel, aExpanded, aLeaf, aRow, aHasFocus);
		}
	}
}
