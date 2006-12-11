/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.gui.kit;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import tod.gui.IGUIManager;

/**
 * A hyperlink-like label whose behaviour is defined by subclasses
 * @author gpothier
 */
public abstract class LinkLabel extends JLabel implements MouseListener
{
	private IGUIManager itsGUIManager;
	private String itsText;
	
	public LinkLabel(IGUIManager aGUIManager, String aText)
	{
		itsText = aText;
		itsGUIManager = aGUIManager;
		
		addMouseListener(this);
		
		update();
	}

	public void setEnabled(boolean aEnabled)
	{
		super.setEnabled(aEnabled);
		update();
	}
	
	private void update ()
	{
		if (isEnabled())
		{
			setText("<html><font color='blue'><u>"+itsText);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));		
		}
		else
		{
			setText("<html><font color='gray'>"+itsText);
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));					
		}
	}
	
	/**
	 * Defines the behaviour of the link.
	 * @param aButton The mouse button that was pressed
	 * @param aClickCount The button click count
	 */
	protected abstract void link (int aButton, int aClickCount, boolean aCtrl, boolean aShift, boolean aAlt);
	
	public IGUIManager getGUIManager()
	{
		return itsGUIManager;
	}
	
	public void mouseClicked(MouseEvent aE)
	{
		if (isEnabled()) link(
				aE.getButton(), 
				aE.getClickCount(), 
				(aE.getModifiersEx() | MouseEvent.CTRL_DOWN_MASK) != 0, 
				(aE.getModifiersEx() | MouseEvent.SHIFT_DOWN_MASK) != 0, 
				(aE.getModifiersEx() | MouseEvent.ALT_DOWN_MASK) != 0);
	}
	
	public void mouseEntered(MouseEvent aE)
	{
	}
	
	public void mouseExited(MouseEvent aE)
	{
	}
	
	public void mousePressed(MouseEvent aE)
	{
	}
	
	public void mouseReleased(MouseEvent aE)
	{
	}
}
