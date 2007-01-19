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
import java.awt.Font;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.FontConfig;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import zz.csg.api.IGraphicContainer;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

public class CFlowViewUtils
{
	/**
	 * Creates a graphic object representing the method of a {@link IBehaviorCallEvent}
	 * as well as its arguments.
	 */
	public static IRectangularGraphicObject createBehaviorCallHeader(
			IGUIManager aGUIManager,
			ILogBrowser aLogBrowser,
			IBehaviorCallEvent aEvent,
			String aPrefix,
			XFont aFont)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		// Create prefix
		IBehaviorExitEvent theExitEvent = aEvent.getExitEvent();
		
		Color theColor = theExitEvent != null && theExitEvent.hasThrown() ?
				Color.RED
				: Color.BLACK;
		
		theContainer.pChildren().add(SVGFlowText.create(aPrefix, aFont, theColor));

		// Create behavior link
		IBehaviorInfo theBehavior = aEvent.getExecutedBehavior();
		if (theBehavior == null)
		{
			aFont = aFont.deriveFont(Font.ITALIC, aFont.getAWTFont().getSize2D());
			theBehavior = aEvent.getCalledBehavior();
		}
		ITypeInfo theType = theBehavior.getType();
		Object[] theArguments = aEvent.getArguments();
		
		theContainer.pChildren().add(Hyperlinks.type(aGUIManager, theType, aFont));
		theContainer.pChildren().add(SVGFlowText.create(".", aFont, Color.BLACK));
		theContainer.pChildren().add(Hyperlinks.behavior(aGUIManager, theBehavior, aFont));

		CFlowViewUtils.addArguments(aGUIManager, aLogBrowser, theContainer, theArguments, aFont);

		theContainer.setLayoutManager(new SequenceLayout());
		return theContainer;
	}

	
	/**
	 * Creates a graphic object representing the return value of 
	 * a behavior call event
	 * @param aGUIManager
	 * @param aLogBrowser
	 * @param aEvent The behavior call event 
	 * @param aPrefix The text prefix, such as "Returned " or "Instantiated ".
	 * @return
	 */
	public static IRectangularGraphicObject createBehaviorCallFooter(
			IGUIManager aGUIManager,
			ILogBrowser aLogBrowser,
			IBehaviorCallEvent aEvent,
			String aPrefix,
			XFont aFont)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();

		IBehaviorInfo theBehavior = aEvent.getExecutedBehavior();
		if (theBehavior == null) theBehavior = aEvent.getCalledBehavior();
		
		IBehaviorExitEvent theExitEvent = aEvent.getExitEvent();
		
		if (theExitEvent == null)
		{
			theContainer.pChildren().add(SVGFlowText.create("Behavior never returned", aFont, Color.BLACK));
		}
		else if (theExitEvent.hasThrown())
		{
			theContainer.pChildren().add(SVGFlowText.create("Thrown ", aFont, Color.RED));

			theContainer.pChildren().add(Hyperlinks.object(
					aGUIManager, 
					aLogBrowser, 
					theExitEvent.getResult(), 
					aFont));
		}
		else if (theBehavior.getReturnType().isVoid())
		{
			theContainer.pChildren().add(SVGFlowText.create(aPrefix, aFont, Color.BLACK));
		}
		else 
		{
			theContainer.pChildren().add(SVGFlowText.create(aPrefix, aFont, Color.BLACK));
			
			theContainer.pChildren().add(Hyperlinks.object(
					aGUIManager, 
					aLogBrowser, 
					theExitEvent.getResult(), 
					aFont));
		}
		
		theContainer.setLayoutManager(new SequenceLayout());
		return theContainer;
	}
	
	
	/**
	 * Adds the hyperlinks representing the behavior's arguments to the given container.
	 */
	public static void addArguments(
			IGUIManager aGUIManager,
			ILogBrowser aLogBrowser,
			IGraphicContainer aContainer,
			Object[] aArguments, 
			XFont aFont)
	{
		aContainer.pChildren().add(SVGFlowText.create("(", aFont, Color.BLACK));
		
		if (aArguments != null)
		{
			boolean theFirst = true;
			for (Object theArgument : aArguments)
			{
				if (theFirst) theFirst = false;
				else aContainer.pChildren().add(SVGFlowText.create(", ", aFont, Color.BLACK));
				
				aContainer.pChildren().add(Hyperlinks.object(
						aGUIManager,
						aLogBrowser,
						theArgument, 
						aFont));
			}
		}
		else
		{
			aContainer.pChildren().add(SVGFlowText.create("...", aFont, Color.BLACK));
		}
		
		aContainer.pChildren().add(SVGFlowText.create(")", aFont, Color.BLACK));
	}
	

}
