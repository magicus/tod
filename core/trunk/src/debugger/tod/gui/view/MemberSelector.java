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
