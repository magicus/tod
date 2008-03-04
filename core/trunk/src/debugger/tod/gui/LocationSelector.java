/*
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
package tod.gui;

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
import javax.swing.tree.TreePath;

import tod.core.database.structure.ILocationsRepository;
import tod.core.database.structure.LocationTreeBuilder;
import tod.core.database.structure.LocationTreeBuilder.Node;

/**
 * This panel permits to select a source code location.
 * Locations are subclasses of 
 * {@link tod.core.database.structure.ILocationInfo}
 * @author gpothier
 */
public class LocationSelector extends JPanel implements TreeSelectionListener
{
	private ILocationsRepository itsLocationTrace;
	
	private MyTreeModel itsModel;
	private JTree itsTree;
	
	private List/*<ILocationSelectionListener>*/ itsListeners =
		new ArrayList/*<ILocationSelectionListener>*/();

	private LocationTreeBuilder itsLocationTreeBuilder;
	
	public LocationSelector(ILocationsRepository aLocationTrace)
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
