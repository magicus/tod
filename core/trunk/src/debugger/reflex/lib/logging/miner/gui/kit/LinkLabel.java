/*
 * Created on Nov 13, 2004
 */
package reflex.lib.logging.miner.gui.kit;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.seed.Seed;
import zz.utils.properties.IPropertyListener;

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
