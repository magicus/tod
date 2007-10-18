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
package tod.gui;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import tod.gui.seed.Seed;
import zz.utils.ArrayStack;
import zz.utils.ItemAction;
import zz.utils.Stack;

/**
 * Implements the web browser-like navigation: backward and forward stack of
 * seeds.
 * @author gpothier
 */
public class BrowserNavigator<S extends Seed>
{
	private Stack<S> itsBackwardSeeds = new ArrayStack<S>(50);
	private Stack<S> itsForwardSeeds = new ArrayStack<S>();
	
	private Action itsBackwardAction = new BackwardAction();
	private Action itsForwardAction = new ForwardAction();
	
	private S itsCurrentSeed;
	
	public S getCurrentSeed()
	{
		return itsCurrentSeed;
	}

	protected void setSeed (S aSeed)
	{
		itsCurrentSeed = aSeed;
	}
	
	/**
	 * Jumps to the previous view
	 */
	public void backward()
	{
		if (! itsBackwardSeeds.isEmpty())
		{
			S theSeed = itsBackwardSeeds.pop();
			if (itsCurrentSeed != null) itsForwardSeeds.push(itsCurrentSeed);
			setSeed(theSeed);
			updateActions();
		}
	}

	/**
	 * Jumps to the view that was active before jumping backwards
	 */
	public void forward()
	{
		if (! itsForwardSeeds.isEmpty())
		{
			S theSeed = itsForwardSeeds.pop();
			if (itsCurrentSeed != null) itsBackwardSeeds.push(itsCurrentSeed);
			setSeed(theSeed);
			updateActions();
		}
	}
	
	/**
	 * Opens a view for the given seed.
	 */
	public void open (S aSeed)
	{
		if (itsCurrentSeed != null) itsBackwardSeeds.push(itsCurrentSeed);
		itsForwardSeeds.clear();
		setSeed(aSeed);
		updateActions();
	}
	
	/**
	 * Clears the forward/backward history of this navigator.
	 */
	public void clear()
	{
		setSeed(null);
		itsBackwardSeeds.clear();
		itsForwardSeeds.clear();
		updateActions();
	}
	
	private void updateActions()
	{
		itsBackwardAction.setEnabled(itsBackwardAction.isEnabled());
		itsForwardAction.setEnabled(itsForwardAction.isEnabled());
	}
	
	/**
	 * Returns an action that corresponds to the {@link #backward()} operation.
	 */
	public Action getBackwardAction()
	{
		return itsBackwardAction;
	}
	
	/**
	 * Returns an action that corresponds to the {@link #forward()} operation.
	 */
	public Action getForwardAction()
	{
		return itsForwardAction;
	}
	
	private class BackwardAction extends ItemAction
	{
		public BackwardAction()
		{
			setTitle("<");
		}
		
		public void actionPerformed(ActionEvent aE)
		{
			backward();
		}
		
		public boolean isEnabled()
		{
			return ! itsBackwardSeeds.isEmpty();
		}
	}
	
	private class ForwardAction extends ItemAction
	{
		public ForwardAction()
		{
			setTitle(">");
		}
		
		public void actionPerformed(ActionEvent aE)
		{
			forward();
		}
		
		public boolean isEnabled()
		{
			return ! itsForwardSeeds.isEmpty();
		}
	}
}
