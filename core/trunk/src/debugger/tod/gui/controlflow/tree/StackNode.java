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
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;

import tod.Util;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.FontConfig;
import tod.gui.JobProcessor;
import tod.gui.controlflow.CFlowView;
import tod.gui.seed.CFlowSeed;
import zz.utils.ui.GridStackLayout;
import zz.utils.ui.UIUtils;
import zz.utils.ui.ZLabel;

/**
 * Represents a stack item in the control flow.
 * @author gpothier
 */
public class StackNode extends AbstractCFlowNode
{
	private IBehaviorCallEvent itsEvent;

	private boolean itsMouseOver = false;
	
	public StackNode(
			CFlowView aView,
			JobProcessor aJobProcessor,
			IBehaviorCallEvent aEvent)
	{
		super(aView, aJobProcessor);
		itsEvent = aEvent;
		createUI();
	}

	public IBehaviorCallEvent getEvent()
	{
		return itsEvent;
	}

	private void createUI()
	{
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
		
		if (! getEvent().isDirectParent())
		{
			add(createDots());
		}
		
		setLayout(new GridStackLayout(1));
	}
	
	private JComponent createDots()
	{
		return new JLabel("...");
	}
	
	@Override
	protected void paintComponent(Graphics aG)
	{
		Color theColor = Color.ORANGE;
		if (itsMouseOver) theColor = UIUtils.getLighterColor(theColor);
		aG.setColor(theColor);
		aG.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	public void mouseEntered(MouseEvent aE)
	{
		itsMouseOver = true;
		repaint();
	}

	
	@Override
	public void mouseExited(MouseEvent aE)
	{
		itsMouseOver = false;
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent aE)
	{
		CFlowSeed theSeed = getView().getSeed();
		theSeed.pParentEvent().set(getEvent().getParent());
		theSeed.pSelectedEvent().set(getEvent());

		aE.consume();
	}
}
