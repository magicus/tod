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
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
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
public abstract class AbstractStackNode extends AbstractCFlowNode
{
	private IParentEvent itsEvent;
	
	/**
	 * Indicates if this stack node corresponds to the currently
	 * selected stack frame. 
	 */
	private boolean itsCurrentStackFrame;

	private boolean itsMouseOver = false;
	
	public AbstractStackNode(
			CFlowView aView,
			JobProcessor aJobProcessor,
			IParentEvent aEvent,
			boolean aCurrentStackFrame)
	{
		super(aView, aJobProcessor);
		itsEvent = aEvent;
		itsCurrentStackFrame = aCurrentStackFrame;
		createUI();
	}

	public IParentEvent getEvent()
	{
		return itsEvent;
	}

	protected abstract JComponent createHeader();
	
	protected void createUI()
	{
		setLayout(GUIUtils.createStackLayout());

		if (! getEvent().isDirectParent())
		{
			add(createDots());
		}		
		
		add(createHeader());
	}
	
	private JComponent createDots()
	{
		return GUIUtils.createLabel("···");
	}
	
	@Override
	protected void paintComponent(Graphics aG)
	{
		Color theColor = Color.ORANGE;
		if (itsCurrentStackFrame) theColor = theColor.darker();
		if (! itsMouseOver || itsCurrentStackFrame) theColor = UIUtils.getLighterColor(theColor);
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
		if (! itsCurrentStackFrame)
		{
			CFlowSeed theSeed = getView().getSeed();
			theSeed.pParentEvent().set(getEvent());
			theSeed.pSelectedEvent().set(null);
		}

		aE.consume();
	}
}
