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
package tod.gui.activities.structure;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
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
import tod.gui.GUIUtils;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.PropertyEditor;
import zz.utils.ui.StackLayout;

/**
 * This panel displays the disassembled bytecode of a behavior.
 * @author gpothier
 */
public class DisassemblyPanel extends JPanel
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

	private DisassembledBehavior itsDisassembled;

	private JTable itsTable;
	private JLabel itsTitleLabel;

	private IRWProperty<Boolean> pShowTODInstructions = new SimpleRWProperty<Boolean>(this, false)
	{
		@Override
		protected void changed(Boolean aOldValue, Boolean aNewValue)
		{
			updateInstructions();
		}
	};

	public DisassemblyPanel(IBehaviorInfo aBehavior)
	{
		itsBehavior = aBehavior;
		itsDisassembled = Disassembler.disassemble(itsBehavior);
		createUI();
	}

	private void createUI()
	{
		String theTitle = ""+itsBehavior.getType().getName()+"."+itsBehavior.getName();
		if (itsDisassembled == null) theTitle += " - bytecode unavailable";
		itsTitleLabel = new JLabel(theTitle);
		
		itsTable = new JTable();
		itsTable.setShowHorizontalLines(false);
		itsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		
		setLayout(new BorderLayout());
		
		add(new JScrollPane(itsTable), BorderLayout.CENTER);
		
		JPanel theNorth = new JPanel(GUIUtils.createSequenceLayout());
		theNorth.add(itsTitleLabel);
		
		if (itsDisassembled != null)
		{
			theNorth.add(PropertyEditor.createCheckBox(pShowTODInstructions, "Show TOD instructions"));
		}
		add(theNorth, BorderLayout.NORTH);
		
		updateInstructions();
	}
	
	private void updateInstructions()
	{
		if (itsDisassembled == null) return;
		Instruction[] theInstructions = itsDisassembled.getInstructions();
		
		if (! pShowTODInstructions.get())
		{
			// Filter TOD instructions
			List<Instruction> theFilteredInstructions = new ArrayList<Instruction>();
			for (Instruction theInstruction : theInstructions)
			{
				BytecodeRole theRole = itsBehavior.getTag(BytecodeTagType.ROLE, theInstruction.pc);
				if (theRole != BytecodeRole.TOD_CODE) theFilteredInstructions.add(theInstruction);
			}
			theInstructions = theFilteredInstructions.toArray(new Instruction[theFilteredInstructions.size()]);
		}
		
		itsTable.setModel(new MyTableModel(itsBehavior, theInstructions));

		for (int i=0;i<columns.length;i++) 
			itsTable.getColumnModel().getColumn(i).setPreferredWidth(columns[i].width);
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