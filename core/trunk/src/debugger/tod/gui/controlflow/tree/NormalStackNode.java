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
package tod.gui.controlflow.tree;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.Util;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.JobProcessor;
import tod.gui.controlflow.CFlowView;
import zz.utils.ui.ZLabel;

/**
 * A normal stack node, corresponding to a behavior call event
 * @author gpothier
 */
public class NormalStackNode extends AbstractStackNode
{
	public NormalStackNode(
			CFlowView aView, 
			JobProcessor aJobProcessor, 
			IBehaviorCallEvent aEvent,
			boolean aCurrentStackFrame)
	{
		super(aView, aJobProcessor, aEvent, aCurrentStackFrame);
		
	}

	@Override
	public IBehaviorCallEvent getEvent()
	{
		return (IBehaviorCallEvent) super.getEvent();
	}
	
	@Override
	protected JComponent createHeader()
	{
		JPanel theContainer = new JPanel(GUIUtils.createStackLayout());
		theContainer.setOpaque(false);
		StringBuilder theBuilder = new StringBuilder();

		// Create caption
		IBehaviorInfo theBehavior = getEvent().getExecutedBehavior();
		if (theBehavior == null) theBehavior = getEvent().getCalledBehavior();
		ITypeInfo theType = theBehavior.getType();
		Object[] theArguments = getEvent().getArguments();
		
		// Type.method
		theBuilder.append(Util.getSimpleName(theType.getName()));
		theBuilder.append(".");
		theBuilder.append(theBehavior.getName());
		
		// Arguments
//		theBuilder.append("(");
//		
//		if (theArguments != null)
//		{
//			boolean theFirst = true;
//			for (Object theArgument : theArguments)
//			{
//				if (theFirst) theFirst = false;
//				else theBuilder.append(", ");
//				
//				theBuilder.append(getView().getFormatter().formatObject(theArgument));
//			}
//		}
//		else
//		{
//			theBuilder.append("...");
//		}
//		
//		theBuilder.append(")");

		add(ZLabel.create(
				Util.getPackageName(theType.getName()), 
				FontConfig.TINY_FONT, 
				Color.DARK_GRAY));
		
		add(ZLabel.create(
				theBuilder.toString(), 
				FontConfig.SMALL_FONT, 
				Color.BLACK));
		
		return theContainer;
	}
}
