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
package tod.gui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import tod.core.database.browser.IObjectInspector;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.gui.FontConfig;
import zz.utils.SimpleListModel;
import zz.utils.Utils;
import zz.utils.properties.HashSetProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.ISetProperty;
import zz.utils.properties.SimpleRWProperty;

public class MemberSelector extends JPanel
{
	private ISetProperty<IMemberInfo> pSelectedMembers = new HashSetProperty<IMemberInfo>(this)
	{
		@Override
		protected void contentChanged()
		{
			repaint();
		}
	};
	
	private IRWProperty<IObjectInspector> pInspector = new SimpleRWProperty<IObjectInspector>(this)
	{
		@Override
		protected void changed(IObjectInspector aOldValue, IObjectInspector aNewValue)
		{
			updateListPanel();
		}
	};

	private JList itsMembersList;
	private SimpleListModel itsMembersListModel;
	
	public MemberSelector()
	{
		createUI();
	}

	/**
	 * The set of currently selected members.
	 */
	public ISetProperty<IMemberInfo> pSelectedMembers()
	{
		return pSelectedMembers;
	}

	/**
	 * The type whose members are being selected
	 */
	public IRWProperty<IObjectInspector> pInspector()
	{
		return pInspector;
	}
	
	private void createUI()
	{
		setLayout(new BorderLayout());
		
		itsMembersListModel = new SimpleListModel();
		itsMembersList = new JList(itsMembersListModel);
		itsMembersList.setCellRenderer(new MyRenderer());
		itsMembersList.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent aE)
					{
						int theIndex = itsMembersList.locationToIndex(aE.getPoint());
						IMemberInfo theMember = (IMemberInfo) itsMembersListModel.getElementAt(theIndex);
						if (! pSelectedMembers().remove(theMember)) pSelectedMembers().add (theMember);
					}
				});
		add (new JScrollPane (itsMembersList), BorderLayout.CENTER);
	}

	private void updateListPanel()
	{
		IObjectInspector theInspector = pInspector().get();
		
		List<IMemberInfo> theList = new ArrayList<IMemberInfo>();
		if (theInspector != null) Utils.fillCollection(theList, theInspector.getMembers());
		
		Collections.sort(theList, MemberComparator.getInstance());
		
		itsMembersListModel.setList(theList);
	}
	
	/**
	 * Select all the fields of the current type, and deselects everything else.
	 */
	public void selectFields()
	{
		pSelectedMembers().clear();
		
		IObjectInspector theInspector = pInspector().get();
		if (theInspector != null)
		{
			for (IMemberInfo theMember : theInspector.getMembers())
			{
				if (theMember instanceof IFieldInfo)
				{
					IFieldInfo theField = (IFieldInfo) theMember;
					pSelectedMembers().add(theField);
				}
			}
		}
	}

	
	private class MyRenderer extends JCheckBox implements ListCellRenderer
	{
		private Font itsBehaviorFont = FontConfig.STD_FONT.getAWTFont();
		private Font itsFieldFont = itsBehaviorFont.deriveFont(Font.ITALIC);
		
		public Component getListCellRendererComponent(JList aList, Object aValue, int aIndex, boolean aIsSelected, boolean aCellHasFocus)
		{
			IMemberInfo theMember = (IMemberInfo) aValue;
			setText(theMember.getName());
			setSelected(pSelectedMembers().contains(theMember));
			
			if (theMember instanceof IFieldInfo) setFont(itsFieldFont);
			else setFont(itsBehaviorFont);
			
			return this;
		}
	}
	
	private static class MemberComparator implements Comparator<IMemberInfo>
	{
		private static MemberComparator INSTANCE = new MemberComparator();

		public static MemberComparator getInstance()
		{
			return INSTANCE;
		}

		private MemberComparator()
		{
		}

		public int compare(IMemberInfo aO1, IMemberInfo aO2)
		{
			boolean theField1 = aO1 instanceof IFieldInfo;
			boolean theField2 = aO2 instanceof IFieldInfo;
			
			if (theField1 != theField2) return theField1 ? -1 : 1;
			else return aO1.getName().compareToIgnoreCase(aO2.getName());
		}
		
	}
}
