/*
TOD - Trace Oriented Debugger.
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
package tod.gui.view.controlflow;

import java.awt.Color;

import javax.swing.JPanel;

import tod.core.database.event.IBehaviorCallEvent;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import tod.tools.scheduling.IJobScheduler;
import zz.utils.ui.ZLabel;
import zz.utils.ui.text.XFont;

public class CFlowViewUtils
{
	/**
	 * Adds the hyperlinks representing the behavior's arguments to the given container.
	 */
	public static void addArguments(
			IGUIManager aGUIManager,
			IJobScheduler aJobScheduler,
			JPanel aContainer,
			IBehaviorCallEvent aRefEvent,
			Object[] aArguments, 
			XFont aFont,
			boolean aShowPackageNames)
	{
		aContainer.add(ZLabel.create("(", aFont, Color.BLACK));
		
		if (aArguments != null)
		{
			boolean theFirst = true;
			for (Object theArgument : aArguments)
			{
				if (theFirst) theFirst = false;
				else aContainer.add(ZLabel.create(", ", aFont, Color.BLACK));
				
				aContainer.add(Hyperlinks.object(
						aGUIManager,
						Hyperlinks.SWING, 
						aJobScheduler,
						theArgument, 
						aRefEvent,
						aShowPackageNames));
			}
		}
		else
		{
			aContainer.add(ZLabel.create("...", aFont, Color.BLACK));
		}
		
		aContainer.add(ZLabel.create(")", aFont, Color.BLACK));
	}
	

}
