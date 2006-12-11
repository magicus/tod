/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.gui;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JPanel;

import tod.gui.seed.Seed;
import tod.gui.view.LogView;
import zz.utils.ArrayStack;
import zz.utils.ItemAction;
import zz.utils.Stack;
import zz.utils.ui.StackLayout;

/**
 * Implements the web browser-like navigation: backward and forward stack of
 * seeds.
 * @author gpothier
 */
public class BrowserNavigator
{
	private Stack<Seed> itsBackwardSeeds = new ArrayStack<Seed>(50);
	private Stack<Seed> itsForwardSeeds = new ArrayStack<Seed>();
	
	private Action itsBackwardAction = new BackwardAction();
	private Action itsForwardAction = new ForwardAction();
	
	private JPanel itsViewContainer;
	private Seed itsCurrentSeed;

	public BrowserNavigator()
	{
		itsViewContainer = new JPanel (new StackLayout());
		
	}
	
	
	private void setSeed (Seed aSeed)
	{
		if (itsCurrentSeed != null) 
		{
			try
			{
				LogView theComponent = itsCurrentSeed.getComponent();
				if (theComponent != null) itsViewContainer.remove(theComponent);
				itsCurrentSeed.deactivate();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		itsCurrentSeed = aSeed;
		
		if (itsCurrentSeed != null) 
		{
			try
			{
				itsCurrentSeed.activate();
				LogView theComponent = itsCurrentSeed.getComponent();
				itsViewContainer.add(theComponent);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		itsViewContainer.revalidate();
		itsViewContainer.repaint();
		itsViewContainer.validate();
	}
	

	public JPanel getViewContainer()
	{
		return itsViewContainer;
	}
	
	/**
	 * Jumps to the previous view
	 */
	public void backward()
	{
		if (! itsBackwardSeeds.isEmpty())
		{
			Seed theSeed = itsBackwardSeeds.pop();
			if (itsCurrentSeed != null) itsForwardSeeds.push(itsCurrentSeed);
			setSeed(theSeed);
			updateActions();
		}
	}

	/**
	 * Jumps to the view that was active before jumping backwards
	 *
	 */
	public void forward()
	{
		if (! itsForwardSeeds.isEmpty())
		{
			Seed theSeed = itsForwardSeeds.pop();
			if (itsCurrentSeed != null) itsBackwardSeeds.push(itsCurrentSeed);
			setSeed(theSeed);
			updateActions();
		}
	}
	
	/**
	 * Opens a view for the given seed.
	 */
	public void open (Seed aSeed)
	{
		if (itsCurrentSeed != null) itsBackwardSeeds.push(itsCurrentSeed);
		itsForwardSeeds.clear();
		setSeed(aSeed);
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
