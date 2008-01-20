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
package tod.gui.view.controlflow.tree;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import tod.core.database.event.IParentEvent;
import tod.gui.GUIUtils;
import tod.gui.JobProcessor;
import zz.utils.ui.MousePanel;
import zz.utils.ui.UIUtils;

/**
 * Represents a stack item in the control flow.
 * @author gpothier
 */
public abstract class AbstractStackNode extends MousePanel
{
	private final JobProcessor itsJobProcessor;
	private final IParentEvent itsEvent;
	
	/**
	 * Indicates if this stack node corresponds to the currently
	 * selected stack frame. 
	 */
	private boolean itsCurrentStackFrame;

	private boolean itsMouseOver = false;
	
	private CallStackPanel itsCallStackPanel;
	
	public AbstractStackNode(
			JobProcessor aJobProcessor,
			IParentEvent aEvent,
			boolean aCurrentStackFrame, CallStackPanel aCallStackPanel)
	{
		itsJobProcessor = aJobProcessor;
		itsEvent = aEvent;
		itsCurrentStackFrame = aCurrentStackFrame;
		itsCallStackPanel = aCallStackPanel;
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
			JComponent theDots = createDots();
			theDots.addMouseListener(this);
			add(theDots);
		}		
		
		JComponent theHeader = createHeader();
		theHeader.addMouseListener(this);
		add(theHeader);
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
		if (! itsMouseOver || itsCurrentStackFrame) 
			theColor = UIUtils.getLighterColor(theColor);
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
			//removed old behavior
			//Bus.get(this).postMessage(new EventSelectedMsg(getEvent(), SelectionMethod.SELECT_IN_CALL_STACK));
			itsCallStackPanel.selectChildOf(itsEvent);
						
		}
		
		
		aE.consume();
	}
}
