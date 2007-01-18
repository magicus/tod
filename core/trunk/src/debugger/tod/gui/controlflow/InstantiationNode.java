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
package tod.gui.controlflow;

import java.awt.Color;

import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.gui.Hyperlinks;
import zz.csg.api.IRectangularGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

public class InstantiationNode extends AbstractBehaviorNode
{
	public InstantiationNode(
			CFlowView aView,
			IInstantiationEvent aInstantiationEvent)
	{
		super (aView, aInstantiationEvent);
	}
	
	
	@Override
	protected IInstantiationEvent getEvent()
	{
		return (IInstantiationEvent) super.getEvent();
	}
	
	@Override
	protected void fillHeader(IRectangularGraphicContainer aContainer)
	{
		XFont theFont = getHeaderFont();

		IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
		
		Color theColor = theExitEvent != null && theExitEvent.hasThrown() ?
				Color.RED
				: Color.BLACK;
		
		aContainer.pChildren().add(SVGFlowText.create(
				"new ", 
				theFont, 
				theColor));
		
		aContainer.pChildren().add(Hyperlinks.type(getGUIManager(), getEvent().getType(), theFont));

		addArguments(aContainer, getEvent().getArguments(), theFont);
	}
	
	@Override
	protected void fillFooter(IRectangularGraphicContainer aContainer)
	{
		XFont theFont = getHeaderFont();

		IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
		
		if (theExitEvent == null)
		{
			aContainer.pChildren().add(SVGFlowText.create("Behavior never returned", theFont, Color.BLACK));
		}
		else if (theExitEvent.hasThrown())
		{
			aContainer.pChildren().add(SVGFlowText.create("Thrown ", theFont, Color.RED));
			
			aContainer.pChildren().add(Hyperlinks.object(
					getGUIManager(), 
					getEventTrace(), 
					theExitEvent.getResult(),
					theFont));
		}
		else
		{
			aContainer.pChildren().add(SVGFlowText.create("Instanciated ", theFont, Color.BLACK));

			aContainer.pChildren().add(Hyperlinks.object(
					getGUIManager(), 
					getEventTrace(), 
					getEvent().getInstance(),
					theFont));
		}
		
	}
	
}
