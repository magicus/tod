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
package tod.gui.components.intimacyeditor;

import java.awt.Component;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.tree.PackageInfo;
import tod.core.database.structure.tree.StructureTreeBuilders;
import tod.gui.components.LocationTreeTable;
import tod.gui.components.eventlist.IntimacyLevel;
import tod.gui.settings.IntimacySettings;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.tree.ITree;
import zz.utils.tree.SimpleTreeNode;
import zz.utils.ui.StackLayout;

/**
 * Editor for the intimacy settings (see {@link IntimacySettings}).
 * @author gpothier
 */
public class IntimacyLevelEditor extends JPanel implements IEventListener<Void>
{
	public static final int ROLE_ICON_SIZE = 13;
	private static final ILocationInfo NO_VALUE = new PackageInfo(null);
	private final IntimacySettings itsSettings;
	private final IStructureDatabase itsStructureDatabase;
	
	private LocationTreeTable itsTreeTable;
	
	public IntimacyLevelEditor(IntimacySettings aSettings, IStructureDatabase aStructureDatabase)
	{
		itsSettings = aSettings;
		itsStructureDatabase = aStructureDatabase;
		createUI();
	}

	private void createUI()
	{
		itsTreeTable = new MyTreeTable(StructureTreeBuilders.createAspectTree(itsStructureDatabase, true));
		
		// Setup cell size
		Dimension theSize = new IndividualIntimacyEditor(this).getPreferredSize();
		itsTreeTable.setRowHeight(theSize.height + 1);
		itsTreeTable.setColumnWidth(0, theSize.width);

		IntimacyLevelCellEditor theEditor = new IntimacyLevelCellEditor();
		itsTreeTable.setDefaultRenderer(IntimacyLevel.class, theEditor);
		itsTreeTable.setDefaultEditor(IntimacyLevel.class, theEditor);
		
		JScrollPane theScrollPane = new JScrollPane(itsTreeTable);
		theScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		theScrollPane.setPreferredSize(new Dimension(300, 200));

		setLayout(new StackLayout());
		add(theScrollPane);
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		itsSettings.eChanged.addListener(this);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsSettings.eChanged.removeListener(this);
	}

	public void fired(IEvent< ? extends Void> aEvent, Void aData)
	{
		itsTreeTable.repaint();
	}
	
	/**
	 * Sets a new intimacy level for the given location. 
	 */
	void setLevel(ILocationInfo aLocation, IntimacyLevel aLevel)
	{
		if (aLocation instanceof IAspectInfo)
		{
			IAspectInfo theAspect = (IAspectInfo) aLocation;
			for (IAdviceInfo theAdvice : theAspect.getAdvices())
			{
				itsSettings.setIntimacyLevel(theAdvice.getId(), aLevel);
			}
		}
		else if (aLocation instanceof IAdviceInfo)
		{
			IAdviceInfo theAdvice = (IAdviceInfo) aLocation;
			itsSettings.setIntimacyLevel(theAdvice.getId(), aLevel);
		}
	}
	
	/**
	 * Retrieves the current intimacy level for a node in the location tree (aspect or advice)
	 */
	private IntimacyLevel getLevel(ILocationInfo aLocation)
	{
		if (aLocation instanceof IAspectInfo)
		{
			IAspectInfo theAspect = (IAspectInfo) aLocation;
			
			boolean theFullObliviousness = true;
			Set<BytecodeRole> theRoles = new HashSet<BytecodeRole>();
			
			for(IAdviceInfo theAdvice : theAspect.getAdvices())
			{
				IntimacyLevel theLevel = itsSettings.getIntimacyLevel(theAdvice.getId());
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
		else if (aLocation instanceof IAdviceInfo)
		{
			IAdviceInfo theAdvice = (IAdviceInfo) aLocation;
			return itsSettings.getIntimacyLevel(theAdvice.getId());
		}
		else throw new RuntimeException("Not handled: "+aLocation);
	}
	
	/**
	 * Creates an individual intimacy editor.
	 * By defaults creates an {@link IndividualIntimacyEditor}, but subclasses can
	 * override this behavior.
	 */
	protected AbstractIndividualIntimacyEditor createIndividualEditor()
	{
		return new IndividualIntimacyEditor(this);
	}
	
	private class MyTreeTable extends LocationTreeTable
	{
		public MyTreeTable(ITree<SimpleTreeNode<ILocationInfo>, ILocationInfo> aTree)
		{
			super(aTree);
		}

		@Override
		protected int getColumnCount()
		{
			return 1;
		}

		@Override
		protected Class getColumnClass(int aColumn)
		{
			switch(aColumn)
			{
			case 0: return IntimacyLevel.class;
			default: throw new RuntimeException("Invalid column: "+aColumn);
			}
		}

		@Override
		protected Object getValueAt(ILocationInfo aLocation, int aColumn)
		{
			switch(aColumn)
			{
			case 0: return getLevel(aLocation);
			default: throw new RuntimeException("Invalid column: "+aColumn);
			}
		}

		@Override
		protected boolean isCellEditable(ILocationInfo aLocation, int aColumn)
		{
			switch(aColumn)
			{
			case 0: return true;
			default: throw new RuntimeException("Invalid column: "+aColumn);
			}
		}
		
	}

	/**
	 * The table cell editor/renderer for the intimacy level column.
	 * @author gpothier
	 */
	private class IntimacyLevelCellEditor extends AbstractCellEditor
	implements TableCellRenderer, TableCellEditor
	{
		private AbstractIndividualIntimacyEditor itsRenderer = createIndividualEditor();
		private AbstractIndividualIntimacyEditor itsEditor = createIndividualEditor();
		private JPanel itsNoValueEditor = new JPanel();
		
		private void setup(
				AbstractIndividualIntimacyEditor aEditor,
				JTable aTable,
				boolean aIsSelected,
				boolean aHasFocus)
		{
			aEditor.setBackground(aIsSelected ? aTable.getSelectionBackground() : aTable.getBackground());
		}
		
		public Component getTableCellRendererComponent(
				JTable aTable,
				Object aValue,
				boolean aIsSelected,
				boolean aHasFocus, 
				int aRow,
				int aColumn)
		{
			if (aValue == NO_VALUE) return itsNoValueEditor;
			
			setup(itsRenderer, aTable, aIsSelected, aHasFocus);
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

			setup(itsEditor, aTable, aIsSelected, true);
			ILocationInfo theLocation = (ILocationInfo) aTable.getModel().getValueAt(aRow, 0);
			itsEditor.setLocationInfo(theLocation);
			itsEditor.setValue((IntimacyLevel) aValue);
			return itsEditor;
		}

		public Object getCellEditorValue()
		{
			return itsEditor.getValue();
		}
	}
}
