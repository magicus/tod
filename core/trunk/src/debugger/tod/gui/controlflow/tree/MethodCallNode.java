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
import java.awt.Font;

import javax.swing.JComponent;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import tod.gui.controlflow.CFlowView;
import zz.utils.ui.ZLabel;
import zz.utils.ui.text.XFont;

public class MethodCallNode extends BehaviorCallNode
{
	public MethodCallNode(
			CFlowView aView, 
			JobProcessor aJobProcessor,
			IBehaviorCallEvent aEvent)
	{
		super(aView, aJobProcessor, aEvent);
	}

	@Override
	protected JComponent createBehaviorName()
	{
		return null;
	}

	@Override
	protected JComponent createFullView()
	{
		return null;
	}

	@Override
	protected JComponent createShortView()
	{
		return null;
	}

//	@Override
//	protected void fillHeaderPrefix(
//			JComponent aContainer,
//			XFont aFont)
//	{
////		IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
////
////		Color theColor = theExitEvent != null && theExitEvent.hasThrown() ?
////				Color.RED
////				: Color.BLACK;
////		
////		aContainer.add(ZLabel.create(aPrefix, theFont, theColor));
//
//		// Create behavior link
//		IBehaviorInfo theBehavior = getEvent().getExecutedBehavior();
//		if (theBehavior == null)
//		{
//			aFont = aFont.deriveFont(Font.ITALIC, aFont.getAWTFont().getSize2D());
//			theBehavior = getEvent().getCalledBehavior();
//		}
//		ITypeInfo theType = theBehavior.getType();
//		
//		aContainer.add(Hyperlinks.type(theType, aFont));
//		aContainer.add(ZLabel.create(".", aFont, Color.BLACK));
//		aContainer.add(Hyperlinks.behavior(theBehavior, aFont));
//	}
//	
//	@Override
//	protected void fillFooterPrefix(
//			JComponent aContainer,
//			XFont aFont)
//	{
//		aContainer.add(ZLabel.create("Returned: ", aFont, Color.BLACK));
//	}
//

}
