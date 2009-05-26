/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.activities.structure;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
import zz.utils.ui.StackLayout;
import zz.utils.ui.UniversalRenderer;

/**
 * This panel displays the line number info table of a behavior
 * @author gpothier
 */
public class LineNumberInfoPanel extends JPanel
{
	private final IBehaviorInfo itsBehavior;

	public LineNumberInfoPanel(IBehaviorInfo aBehavior)
	{
		itsBehavior = aBehavior;
		createUI();
	}

	private void createUI()
	{
		LineNumberInfo[] theLineNumbers = itsBehavior.getLineNumbers();
		JList theList = theLineNumbers != null ? new JList(theLineNumbers) : new JList();
		theList.setCellRenderer(new UniversalRenderer<LineNumberInfo>()
				{
					@Override
					protected String getName(LineNumberInfo aObject)
					{
						return String.format("Line: %d, pc: %d", aObject.getLineNumber(), aObject.getStartPc());
					}
				});
		
		setLayout(new StackLayout());
		add(new JScrollPane(theList));
	}
	
}