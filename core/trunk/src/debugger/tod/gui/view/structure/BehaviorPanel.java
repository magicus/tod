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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IBehaviorInfo.BytecodeTagType;
import tod.core.database.structure.analysis.DisassembledBehavior;
import tod.core.database.structure.analysis.Disassembler;
import tod.core.database.structure.analysis.DisassembledBehavior.Instruction;
import zz.utils.ui.StackLayout;

/**
 * This panel displays detailed information about a behavior.
 * @author gpothier
 */
public class BehaviorPanel extends JPanel
{
	private final IBehaviorInfo itsBehavior;

	public BehaviorPanel(IBehaviorInfo aBehavior)
	{
		itsBehavior = aBehavior;
		createUI();
	}

	private void createUI()
	{
		DisassembledBehavior theDisassembled = Disassembler.disassemble(itsBehavior);
		JTable theTable = new JTable(new MyTableModel(itsBehavior, theDisassembled.getInstructions()));
		theTable.setShowHorizontalLines(false);
		theTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		theTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		theTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		theTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		theTable.getColumnModel().getColumn(3).setPreferredWidth(500);
		
		setLayout(new StackLayout());
		add(new JScrollPane(theTable));
	}
	
	private static class MyTableModel extends AbstractTableModel
	{
		private static String[] columnNames = {"pc", "line", "tags", "code"};
		
		private IBehaviorInfo itsBehavior;
		private Instruction[] itsInstructions;
		
		public MyTableModel(IBehaviorInfo aBehavior, Instruction[] aInstructions)
		{
			itsBehavior = aBehavior;
			itsInstructions = aInstructions;
		}

		public int getColumnCount()
		{
			return 4;
		}

		@Override
		public String getColumnName(int aColumn)
		{
			return columnNames[aColumn];
		}
		
		public int getRowCount()
		{
			return itsInstructions.length;
		}

		public Object getValueAt(int aRowIndex, int aColumnIndex)
		{
			Instruction theInstruction = itsInstructions[aRowIndex];
			switch(aColumnIndex)
			{
			case 0: return theInstruction.pc;
			case 1: return "?";
			case 2: return getTags(theInstruction);
			case 3: return theInstruction.text;
			default: throw new RuntimeException("Not handled: "+aColumnIndex);
			}
		}
		
		private String getTags(Instruction aInstruction)
		{
			StringBuilder theBuilder = new StringBuilder();
			
			boolean theFirst = true;
			for (BytecodeTagType theType : BytecodeTagType.ALL)
			{
				Object theTag = itsBehavior.getTag(theType, aInstruction.pc);
				if (theTag != null)
				{
					if (theFirst) theFirst = false;
					else theBuilder.append(", ");
					
					theBuilder.append(theTag);
				}
			}
			
			return theBuilder.toString();
		}
	}
}
