/*
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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
