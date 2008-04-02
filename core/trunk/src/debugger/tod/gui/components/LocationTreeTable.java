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
package tod.gui.components;

import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import tod.core.database.structure.ILocationInfo;
import zz.utils.tree.ITree;
import zz.utils.tree.SimpleTreeNode;
import zz.utils.treetable.JTreeTable;
import zz.utils.treetable.ZTreeTableModel;
import zz.utils.ui.StackLayout;
import zz.utils.ui.UniversalRenderer;

/**
 * A tree/table for location nodes.
 * The first column of the table is the tree. More columns can be defined
 * by subclasses.
 * @author gpothier
 */
public abstract class LocationTreeTable extends JPanel
{
	private final JTreeTable itsTreeTable;
	private ITree<SimpleTreeNode<ILocationInfo>, ILocationInfo> itsTree;

	public LocationTreeTable(ITree<SimpleTreeNode<ILocationInfo>, ILocationInfo> aTree)
	{
		super(new StackLayout());
		
		itsTreeTable = new JTreeTable();
		
		itsTreeTable.getTree().setRootVisible(false);
		itsTreeTable.getTree().setShowsRootHandles(true);
		itsTreeTable.setTableHeader(null);
		
		itsTreeTable.getTree().setCellRenderer(new MyTreeRenderer());

		add(itsTreeTable);
		setTree(aTree);
	}
	
	public void setTree(ITree<SimpleTreeNode<ILocationInfo>, ILocationInfo> aTree)
	{
		itsTree = aTree;
		itsTreeTable.setTreeTableModel(new MyModel(itsTree));
	}
	
	public void setDefaultRenderer(Class aClass, TableCellRenderer aRenderer)
	{
		itsTreeTable.setDefaultRenderer(aClass, aRenderer);
	}
	
	public void setDefaultEditor(Class aClass, TableCellEditor aRenderer)
	{
		itsTreeTable.setDefaultEditor(aClass, aRenderer);
	}
	
	public TableColumn getColumn(int aColumn)
	{
		return itsTreeTable.getColumnModel().getColumn(aColumn+1);
	}
	
	public void setRowHeight(int aHeight)
	{
		itsTreeTable.setRowHeight(aHeight);
	}
	
	public void setColumnWidth(int aColumn, int aWidth)
	{
		getColumn(aColumn).setMinWidth(aWidth);
		getColumn(aColumn).setMaxWidth(aWidth);
	}
	
	/**
	 * Number of additional columns.
	 */
	protected int getColumnCount()
	{
		return 0;
	}
	
	/**
	 * Returns the class of an additional column
	 * (first additional column has index 0).
	 */
	protected Class getColumnClass(int aColumn)
	{
		return Object.class;
	}
	
	/**
	 * Returns the name of an additional column
	 * (first additional column has index 0).
	 */
	protected String getColumnName(int aColumn)
	{
		return ""+aColumn;
	}
	
	/**
	 * Whether a cell of an additional column is editable
	 * @param aLocation The location info of the cell's line.
	 * @param aColumn The index of the additional column
	 * (first additional column has index 0).
	 */
	protected boolean isCellEditable(ILocationInfo aLocation, int aColumn)
	{
		return false;
	}

	protected Object getValueAt(ILocationInfo aLocation, int aColumn)
	{
		return null;
	}
	
	protected void setValueAt(Object aValue, ILocationInfo aLocation, int aColumn)
	{
	}


	
	/**
	 * Aspect tree model.
	 * @author gpothier
	 */
	private class MyModel extends ZTreeTableModel<SimpleTreeNode<ILocationInfo>, ILocationInfo>
	{

		public MyModel(ITree<SimpleTreeNode<ILocationInfo>, ILocationInfo> aTree)
		{
			super(aTree);
		}

		public int getColumnCount()
		{
			return LocationTreeTable.this.getColumnCount()+1;
		}

		public Class getColumnClass(int aColumn)
		{
			if (aColumn == 0) return null; // handled by jtreetable
			else return LocationTreeTable.this.getColumnClass(aColumn-1);
		}

		public String getColumnName(int aColumn)
		{
			if (aColumn == 0) return "Location";
			else return LocationTreeTable.this.getColumnName(aColumn-1);
		}
		
		@Override
		public boolean isCellEditable(Object aNode, int aColumn)
		{
			if (aColumn == 0) return super.isCellEditable(aNode, aColumn);
			else 
			{
				SimpleTreeNode<ILocationInfo> theNode = (SimpleTreeNode<ILocationInfo>) aNode;
				return LocationTreeTable.this.isCellEditable(theNode.pValue().get(), aColumn-1);
			}
		}
		
		@Override
		public Object getValueAt(Object aNode, int aColumn)
		{
			if (aColumn == 0) return super.getValueAt(aNode, aColumn);
			else
			{
				SimpleTreeNode<ILocationInfo> theNode = (SimpleTreeNode<ILocationInfo>) aNode;
				return LocationTreeTable.this.getValueAt(theNode.pValue().get(), aColumn-1);
			}
		}
		
		@Override
		public void setValueAt(Object aValue, Object aNode, int aColumn)
		{
			if (aColumn != 0)
			{
				SimpleTreeNode<ILocationInfo> theNode = (SimpleTreeNode<ILocationInfo>) aNode;
				LocationTreeTable.this.setValueAt(aValue, theNode.pValue().get(), aColumn-1);
			}
		}
	}
	
	/**
	 * Renderer for the locations tree.
	 * @author gpothier
	 */
	private static class MyTreeRenderer extends UniversalRenderer<SimpleTreeNode<ILocationInfo>>
	{
		@Override
		protected String getName(SimpleTreeNode<ILocationInfo> aNode)
		{
			ILocationInfo theLocation = aNode.pValue().get();
			return theLocation.getName();
		}
	}

}
