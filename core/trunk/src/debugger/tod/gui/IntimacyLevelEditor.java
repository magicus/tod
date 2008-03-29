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
package tod.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.tree.AdviceNode;
import tod.core.database.structure.tree.AspectNode;
import tod.core.database.structure.tree.LocationNode;
import tod.core.database.structure.tree.StructureTreeBuilders;
import tod.gui.eventlist.IntimacyLevel;
import tod.gui.settings.IntimacySettings;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.tree.ITree;
import zz.utils.tree.SimpleTreeNode;
import zz.utils.treetable.JTreeTable;
import zz.utils.treetable.ZTreeTableModel;
import zz.utils.ui.StackLayout;
import zz.utils.ui.UIUtils;
import zz.utils.ui.UniversalRenderer;
import zz.utils.ui.popup.ButtonPopupComponent;

/**
 * A popup button that permits to edit the intimacy settings 
 * (see {@link IntimacySettings}).
 * @author gpothier
 */
public class IntimacyLevelEditor extends ButtonPopupComponent
{
	private static final int ROLE_ICON_SIZE = 13;
	private final IGUIManager itsGUIManager;
	private final IStructureDatabase itsStructureDatabase;
	
	
	public IntimacyLevelEditor(IGUIManager aGUIManager, IStructureDatabase aStructureDatabase)
	{
		super(null, "", Resources.ICON_INTIMACY.asIcon(20));
		getButton().setMargin(UIUtils.NULL_INSETS);
		itsGUIManager = aGUIManager;
		itsStructureDatabase = aStructureDatabase;
		setPopupComponent(createPopup());
	}

	private JComponent createPopup()
	{
		return new MyPopup();
	}
	
	
	private class MyPopup extends JPanel implements IEventListener<Void>
	{
		private JTreeTable itsTreeTable;

		public MyPopup()
		{
			createUI();
		}

		private void createUI()
		{
			MyModel theModel = new MyModel(StructureTreeBuilders.createAspectTree(itsStructureDatabase, true));
			itsTreeTable = new JTreeTable(theModel);
			itsTreeTable.getTree().setRootVisible(false);
			itsTreeTable.getTree().setShowsRootHandles(true);
			itsTreeTable.setTableHeader(null);
			itsTreeTable.setRowHeight(ROLE_ICON_SIZE+7);
			itsTreeTable.getColumnModel().getColumn(1).setPreferredWidth((ROLE_ICON_SIZE+6)*(1+IntimacyLevel.ROLES.length));

			itsTreeTable.getTree().setCellRenderer(new MyTreeRenderer());
			IntimacyLevelCellEditor theEditor = new IntimacyLevelCellEditor();
			itsTreeTable.setDefaultRenderer(IntimacyLevel.class, theEditor);
			itsTreeTable.setDefaultEditor(IntimacyLevel.class, theEditor);
			
			JScrollPane theScrollPane = new JScrollPane(itsTreeTable);
			theScrollPane.setPreferredSize(new Dimension(300, 200));

			setLayout(new StackLayout());
			add(theScrollPane);
		}
		
		@Override
		public void addNotify()
		{
			super.addNotify();
			itsGUIManager.getSettings().getIntimacySettings().eChanged.addListener(this);
		}
		
		@Override
		public void removeNotify()
		{
			super.removeNotify();
			itsGUIManager.getSettings().getIntimacySettings().eChanged.removeListener(this);
		}

		public void fired(IEvent< ? extends Void> aEvent, Void aData)
		{
			itsTreeTable.repaint();
		}
	}
	
	private void setLevel(ILocationInfo aLocation, IntimacyLevel aLevel)
	{
		IntimacySettings theSettings = itsGUIManager.getSettings().getIntimacySettings();
		if (aLocation instanceof IAspectInfo)
		{
			IAspectInfo theAspect = (IAspectInfo) aLocation;
			for (IAdviceInfo theAdvice : theAspect.getAdvices())
			{
				theSettings.setIntimacyLevel(theAdvice.getId(), aLevel);
			}
		}
		else if (aLocation instanceof IAdviceInfo)
		{
			IAdviceInfo theAdvice = (IAdviceInfo) aLocation;
			theSettings.setIntimacyLevel(theAdvice.getId(), aLevel);
		}
	}
	
	private IntimacyLevel getLevel(LocationNode aNode)
	{
		IntimacySettings theSettings = itsGUIManager.getSettings().getIntimacySettings();
		if (aNode instanceof AspectNode)
		{
			AspectNode theAspectNode = (AspectNode) aNode;
			IAspectInfo theAspect = theAspectNode.getAspectInfo();
			
			boolean theFullObliviousness = true;
			Set<BytecodeRole> theRoles = new HashSet<BytecodeRole>();
			
			for(IAdviceInfo theAdvice : theAspect.getAdvices())
			{
				IntimacyLevel theLevel = theSettings.getIntimacyLevel(theAdvice.getId());
				if (theLevel != null)
				{
					theFullObliviousness = false;
					for(BytecodeRole theRole : IntimacyLevel.ROLES) if (theLevel.showRole(theRole))
					{
						theRoles.add(theRole);
					}
				}
			}
			
			return theFullObliviousness ? IntimacyLevel.FULL_OBLIVIOUSNESS : new IntimacyLevel(theRoles);
		}
		else if (aNode instanceof AdviceNode)
		{
			AdviceNode theAdviceNode = (AdviceNode) aNode;
			IAdviceInfo theAdvice = theAdviceNode.getAdvice();
			return theSettings.getIntimacyLevel(theAdvice.getId());
		}
		else throw new RuntimeException("Not handled: "+aNode);
	}
	
	private static final Object NO_VALUE = new Object();
	
	private class MyModel extends ZTreeTableModel<SimpleTreeNode<ILocationInfo>, ILocationInfo>
	{

		public MyModel(ITree<SimpleTreeNode<ILocationInfo>, ILocationInfo> aTree)
		{
			super(aTree);
		}

		public int getColumnCount()
		{
			return 2;
		}

		public Class getColumnClass(int aColumn)
		{
			switch(aColumn)
			{
			case 0: return null; // handled by jtreetable
			case 1: return IntimacyLevel.class;
			default: throw new RuntimeException("Invalid column: "+aColumn);
			}
		}

		public String getColumnName(int aColumn)
		{
			switch(aColumn)
			{
			case 0: return "advice";
			case 1: return "show details";
			default: throw new RuntimeException("Invalid column: "+aColumn);
			}
		}
		
		@Override
		public boolean isCellEditable(Object aNode, int aColumn)
		{
			if (aColumn == 0) return super.isCellEditable(aNode, aColumn);
			
			if (aNode instanceof AspectNode) return true;
			else if (aNode instanceof AdviceNode) return true;
			else return false;

		}
		
		@Override
		public Object getValueAt(Object aNode, int aColumn)
		{
			if (aColumn == 0) return super.getValueAt(aNode, aColumn);
			if (aNode instanceof LocationNode) return getLevel((LocationNode) aNode);
			else return NO_VALUE;
		}
		
		@Override
		public void setValueAt(Object aValue, Object aNode, int aColumn)
		{
		}
	}
	
	/**
	 * Renderer for the classes tree.
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

	/**
	 * Intimacy editor for a single advice/aspect
	 * @author gpothier
	 */
	private class IndividualIntimacyEditor extends JPanel
	implements ChangeListener
	{
		private AbstractButton itsFullObliviousButton;
		private AbstractButton[] itsRoleCheckBoxes;
		private ILocationInfo itsLocation;

		public IndividualIntimacyEditor()
		{
			createUI();
		}

		private void createUI()
		{
			setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			
			itsFullObliviousButton = new JToggleButton();
			itsFullObliviousButton.setIcon(Resources.ICON_INTIMACY.asIcon(ROLE_ICON_SIZE));
			itsFullObliviousButton.setSelectedIcon(Resources.ICON_FULL_OBLIVIOUSNESS.asIcon(ROLE_ICON_SIZE));
			itsFullObliviousButton.setMargin(UIUtils.NULL_INSETS);
			itsFullObliviousButton.addChangeListener(this);
			
			add(itsFullObliviousButton);
			
			JPanel theRolesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			itsRoleCheckBoxes = new AbstractButton[IntimacyLevel.ROLES.length];
			
			int i=0;
			for(BytecodeRole theRole : IntimacyLevel.ROLES)
			{
				itsRoleCheckBoxes[i] = new JToggleButton();
				ImageIcon theIcon = GUIUtils.getRoleIcon(theRole).asIcon(ROLE_ICON_SIZE);
				itsRoleCheckBoxes[i].setSelectedIcon(theIcon);
				itsRoleCheckBoxes[i].setIcon(new ImageIcon(GrayFilter.createDisabledImage(theIcon.getImage())));
				itsRoleCheckBoxes[i].setMargin(UIUtils.NULL_INSETS);
				itsRoleCheckBoxes[i].addChangeListener(this);
				
				theRolesPanel.add(itsRoleCheckBoxes[i]);
				i++;
			}
			
			add(theRolesPanel);
			
		}

		public void setLocation(ILocationInfo aLocation)
		{
			itsLocation = aLocation;
		}

		public void setValue(IntimacyLevel aLevel)
		{
			if (aLevel == null)
			{
				for (AbstractButton theCheckBox : itsRoleCheckBoxes) theCheckBox.setSelected(false);
				itsFullObliviousButton.setSelected(true);
			}
			else
			{
				int i=0;
				for(BytecodeRole theRole : IntimacyLevel.ROLES)
				{
					itsRoleCheckBoxes[i++].setSelected(aLevel.showRole(theRole));
				}
				
				itsFullObliviousButton.setSelected(false);
			}
		}
		
		public IntimacyLevel getValue()
		{
			if (itsFullObliviousButton.isSelected()) return null;
			else
			{
				Set<BytecodeRole> theRoles = new HashSet<BytecodeRole>();
				int i=0;
				for(BytecodeRole theRole : IntimacyLevel.ROLES)
				{
					if (itsRoleCheckBoxes[i++].isSelected()) theRoles.add(theRole);
				}
				return new IntimacyLevel(theRoles);
			}
		}
		
		public void stateChanged(ChangeEvent aE)
		{
			for (AbstractButton theCheckBox : itsRoleCheckBoxes)
			{
				theCheckBox.setEnabled(! itsFullObliviousButton.isSelected());
			}
			
			IntimacyLevelEditor.this.setLevel(itsLocation, getValue());
		}

	}
	
	private class IntimacyLevelCellEditor extends AbstractCellEditor
	implements TableCellRenderer, TableCellEditor
	{
		private IndividualIntimacyEditor itsRenderer = new IndividualIntimacyEditor();
		private IndividualIntimacyEditor itsEditor = new IndividualIntimacyEditor();
		private JPanel itsNoValueEditor = new JPanel();
		
		public Component getTableCellRendererComponent(
				JTable aTable,
				Object aValue,
				boolean aIsSelected,
				boolean aHasFocus, 
				int aRow,
				int aColumn)
		{
			if (aValue == NO_VALUE) return itsNoValueEditor;
			
			itsRenderer.setValue((IntimacyLevel) aValue);
			return itsRenderer;
		}

		public Component getTableCellEditorComponent(
				JTable aTable,
				Object aValue,
				boolean aIsSelected,
				int aRow,
				int aColumn)
		{
			if (aValue == NO_VALUE) return itsNoValueEditor;

			ILocationInfo theLocation = (ILocationInfo) aTable.getModel().getValueAt(aRow, 0);
			itsEditor.setLocation(theLocation);
			itsEditor.setValue((IntimacyLevel) aValue);
			return itsEditor;
		}

		public Object getCellEditorValue()
		{
			return itsEditor.getValue();
		}
	}
}
