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

import java.awt.Color;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.ObjectInspectorSeed;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

/**
 * This class contains static methods that created standard
 * hyperlinks for types, methods, etc.
 * @author gpothier
 */
public class Hyperlinks
{
	public static IRectangularGraphicObject type (IGUIManager aGUIManager, ITypeInfo aType, XFont aFont)
	{
		return SVGHyperlink.create(
				aGUIManager,
				null, 
				aType.getName(), 
				aFont, 
				Color.BLUE);
	}
	
	public static IRectangularGraphicObject behavior(IGUIManager aGUIManager, IBehaviorInfo aBehavior, XFont aFont)
	{
		return SVGHyperlink.create(
				aGUIManager,
				null, 
				aBehavior.getName(),
				aFont, 
				Color.BLUE);		
	}
	
	/**
	 * An hyperlink that jumps to the cflow of the given event.
	 */
	public static IRectangularGraphicObject event(
			IGUIManager aGUIManager, 
			ILogBrowser aBrowser, 
			String aText,
			ILogEvent aEvent, 
			XFont aFont)
	{
		CFlowSeed theSeed = new CFlowSeed(aGUIManager, aBrowser, aEvent);
		return SVGHyperlink.create(
				aGUIManager, 
				theSeed, 
				aText,
				aFont, 
				Color.BLUE);
	}
	
	public static IRectangularGraphicObject object(
			IGUIManager aGUIManager, 
			ILogBrowser aEventTrace, 
			Object aObject,
			XFont aFont)
	{
		return object(aGUIManager, aEventTrace, null, aObject, aFont);
	}
	
	/**
	 * Creates a hyperlink that permits to jump to an object inspector. 
	 * @param aCurrentObject If provided, reference to the current object will
	 * be displayed as "this" 
	 * @param aObject The object to link to.
	 */
	public static IRectangularGraphicObject object(
			IGUIManager aGUIManager,
			ILogBrowser aEventTrace,
			Object aCurrentObject, 
			Object aObject, 
			XFont aFont)
	{
		// Check if this is a registered object.
		if (aObject instanceof ObjectId.ObjectUID)
		{
			ObjectId.ObjectUID theObject = (ObjectId.ObjectUID) aObject;
			Object theRegistered = aEventTrace.getRegistered(theObject.getId());
			if (theRegistered != null) aObject = theRegistered;
		}
		
		if (aObject instanceof ObjectId)
		{
			ObjectId theId = (ObjectId) aObject;
			
			ITypeInfo theType = aEventTrace.createObjectInspector(theId).getType();

			String theText;
			if (aCurrentObject != null && aCurrentObject.equals(aObject)) theText = "this";
			else theText = theType.getName() + " (" + theId + ")";

			return SVGHyperlink.create(
					aGUIManager, 
					new ObjectInspectorSeed(aGUIManager, aEventTrace, theId), 
					theText, 
					aFont, 
					Color.BLUE);
		}
		else if (aObject instanceof String)
		{
			String theString = (String) aObject;
			return SVGFlowText.create("\""+theString+"\"", aFont, Color.GRAY);
		}
		else if (aObject instanceof Throwable)
		{
			Throwable theThrowable = (Throwable) aObject;
			StringBuilder theBuilder = new StringBuilder();
			theBuilder.append(theThrowable.getClass().getSimpleName());
			if (theThrowable.getMessage() != null)
			{
				theBuilder.append('(');
				theBuilder.append(theThrowable.getMessage());
				theBuilder.append(')');
			}
			return SVGFlowText.create(theBuilder.toString(), aFont, Color.RED);
		}
		else 
		{
			SVGFlowText theFlowText = SVGFlowText.create(""+aObject, aFont, Color.GRAY);
			return theFlowText;
		}

		
	}
}
