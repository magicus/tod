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
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
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
	private static Column cPc = new Column("pc", 50)
	{
		@Override
		public Object getValue(Instruction aInstruction, IBehaviorInfo aBehavior)
		{
			return aInstruction.pc;
		}
	};
	
	private static Column cLine = new Column("line", 50)
	{
		@Override
		public Object getValue(Instruction aInstruction, IBehaviorInfo aBehavior)
		{
			return "";
		}
	};
	
	private static Column cRole = new TagColumn<BytecodeRole>("role", 150, BytecodeTagType.ROLE);
	private static Column cShadow = new IntTagColumn("shadow", 50, BytecodeTagType.INSTR_SHADOW);
	private static Column cSource = new IntTagColumn("source", 50, BytecodeTagType.ADVICE_SOURCE_ID);
	
	private static Column cCode = new Column("code", 500)
	{
		@Override
		public Object getValue(Instruction aInstruction, IBehaviorInfo aBehavior)
		{
			return aInstruction.text;
		}
	};

	/**
	 * The columns displayed by the table.
	 */
	private static Column[] columns = {cPc, cLine, cRole, cShadow, cSource, cCode};
	
	/**
	 * The inspected behavior.
	 */
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
		
		for (int i=0;i<columns.length;i++) 
			theTable.getColumnModel().getColumn(i).setPreferredWidth(columns[i].width);
		
		setLayout(new StackLayout());
		add(new JScrollPane(theTable));
	}
	
	private static class MyTableModel extends AbstractTableModel
	{
		private IBehaviorInfo itsBehavior;
		private Instruction[] itsInstructions;
		
		public MyTableModel(IBehaviorInfo aBehavior, Instruction[] aInstructions)
		{
			itsBehavior = aBehavior;
			itsInstructions = aInstructions;
		}

		public int getColumnCount()
		{
			return columns.length;
		}

		@Override
		public String getColumnName(int aColumn)
		{
			return columns[aColumn].name;
		}
		
		public int getRowCount()
		{
			return itsInstructions.length;
		}

		public Object getValueAt(int aRowIndex, int aColumnIndex)
		{
			Instruction theInstruction = itsInstructions[aRowIndex];
			if (theInstruction.label) return aColumnIndex == 1 ? theInstruction.text : "";
			else return columns[aColumnIndex].getValue(theInstruction, itsBehavior);
		}
		
	}
	
	/**
	 * Column descriptor for the table.
	 * Contains the static info (name, width) and also permits to
	 * retrieve the cell content for a given {@link Instruction}.
	 * @author gpothier
	 */
	private static abstract class Column
	{
		public final String name;
		public final int width;
		
		public Column(String aName, int aWidth)
		{
			name = aName;
			width = aWidth;
		}
		
		public abstract Object getValue(Instruction aInstruction, IBehaviorInfo aBehavior);
	}
	
	/**
	 * A column that show tags.
	 * @author gpothier
	 */
	private static class TagColumn<T> extends Column
	{
		private BytecodeTagType<T> itsType;
		
		public TagColumn(String aName, int aWidth, BytecodeTagType<T> aType)
		{
			super(aName, aWidth);
			itsType = aType;
		}

		@Override
		public Object getValue(Instruction aInstruction, IBehaviorInfo aBehavior)
		{
			T theTag = aBehavior.getTag(itsType, aInstruction.pc);
			return theTag != null ? getValue(theTag) : null;
		}
		
		protected Object getValue(T aTag)
		{
			return aTag.toString();
		}
	}
	
	private static class IntTagColumn extends TagColumn<Integer>
	{
		public IntTagColumn(String aName, int aWidth, BytecodeTagType<Integer> aType)
		{
			super(aName, aWidth, aType);
		}

		@Override
		protected Object getValue(Integer aTag)
		{
			int v = aTag;
			return v != -1 ? v : "";
		}
		
	}
}
