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

import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import tod.core.database.browser.ILogBrowser;
import tod.gui.IGUIManager;
import tod.gui.JobProcessor;
import tod.gui.Hyperlinks.ISeedFactory;
import tod.gui.controlflow.CFlowView;

/**
 * Base for all the graphic objects used in the representation 
 * of the control flow
 * @author gpothier
 */
public abstract class AbstractCFlowNode extends JPanel
implements MouseListener, MouseMotionListener
{
	private final CFlowView itsView;
	private final JobProcessor itsJobProcessor;

	public AbstractCFlowNode(
			CFlowView aView,
			JobProcessor aJobProcessor)
	{
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		itsView = aView;
		itsJobProcessor = aJobProcessor;
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public JobProcessor getJobProcessor()
	{
		return itsJobProcessor;
	}

	public ISeedFactory getSeedFactory()
	{
		return itsView.getLogViewSeedFactory();
	}
	
	public IGUIManager getGUIManager()
	{
		return itsView.getGUIManager();
	}

	public ILogBrowser getLogBrowser()
	{
		return itsView.getLogBrowser();
	}
	
	public CFlowView getView()
	{
		return itsView;
	}

	public void mouseClicked(MouseEvent aE)
	{
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

	public void mouseDragged(MouseEvent aE)
	{
	}

	public void mouseMoved(MouseEvent aE)
	{
	}
	

}
