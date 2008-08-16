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
package tod.gui.view.controlflow.tree;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.Util;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.tools.scheduling.IJobScheduler;
import zz.utils.ui.ZLabel;

/**
 * A normal stack node, corresponding to a behavior call event
 * @author gpothier
 */
public class NormalStackNode extends AbstractStackNode
{
	private ITypeInfo itsType;
	private Object[] itsArguments;
	private String itsBehaviorName;

	public NormalStackNode(
			IJobScheduler aJobScheduler, 
			ILogEvent aEvent,
			CallStackPanel aCallStackPanel)
	{
		super(aJobScheduler, aEvent, aCallStackPanel);
	}

	@Override
	public IBehaviorCallEvent getFrameEvent()
	{
		return (IBehaviorCallEvent) super.getFrameEvent();
	}
	
	@Override
	protected void runJob()
	{
		super.runJob();
		
		StringBuilder theBuilder = new StringBuilder();

		// Create caption
		IBehaviorInfo theBehavior = getFrameEvent().getExecutedBehavior();
		if (theBehavior == null) theBehavior = getFrameEvent().getCalledBehavior();
		itsType = theBehavior.getType();
		itsArguments = getFrameEvent().getArguments();
		
		// Type.method
		theBuilder.append(Util.getSimpleName(itsType.getName()));
		theBuilder.append(".");
		theBuilder.append(theBehavior.getName());
		
		itsBehaviorName = theBuilder.toString();
	}
	
	@Override
	protected JComponent createHeader()
	{
		JPanel theContainer = new JPanel(GUIUtils.createStackLayout());
		theContainer.setOpaque(false);
		
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

		ZLabel theLabel1 = ZLabel.create(
				Util.getPackageName(itsType.getName()), 
				FontConfig.TINY_FONT, 
				Color.DARK_GRAY);
		theLabel1.addMouseListener(this);
		add(theLabel1);
		
		ZLabel theLabel2 = ZLabel.create(
				itsBehaviorName, 
				FontConfig.SMALL_FONT, 
				Color.BLACK);
		theLabel2.addMouseListener(this);
		add(theLabel2);
		
		return theContainer;
	}
}
