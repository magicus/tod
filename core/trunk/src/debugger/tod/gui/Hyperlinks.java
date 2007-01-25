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

import tod.Util;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.seed.LogViewSeedFactory;
import tod.gui.seed.Seed;
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
	public static IRectangularGraphicObject history(
			LogViewSeedFactory aFactory,
			ObjectId aObject,
			XFont aFont)
	{
		return SVGHyperlink.create(
				aFactory.objectHistory(aObject),
				"history",
				aFont,
				Color.BLUE);
	}
	
	public static IRectangularGraphicObject type (
			ISeedFactory aSeedFactory, 
			ITypeInfo aType, 
			XFont aFont)
	{
		return SVGHyperlink.create(
				aSeedFactory.typeSeed(aType),
				aType.getName(), 
				aFont, 
				Color.BLUE);
	}
	
	public static IRectangularGraphicObject behavior(
			ISeedFactory aSeedFactory,
			IBehaviorInfo aBehavior,
			XFont aFont)
	{
		return SVGHyperlink.create(
				aSeedFactory.behaviorSeed(aBehavior), 
				Util.getPrettyName(aBehavior.getName()),
				aFont, 
				Color.BLUE);		
	}
	
	/**
	 * An hyperlink that jumps to the cflow of the given event.
	 */
	public static IRectangularGraphicObject event(
			ISeedFactory aSeedFactory, 
			String aText,
			ILogEvent aEvent, 
			XFont aFont)
	{
		return SVGHyperlink.create(
				aSeedFactory.cflowSeed(aEvent), 
				aText,
				aFont, 
				Color.BLUE);
	}
	
	public static IRectangularGraphicObject object(
			ISeedFactory aSeedFactory, 
			ILogBrowser aEventTrace, 
			Object aObject,
			XFont aFont)
	{
		return object(aSeedFactory, aEventTrace, null, aObject, aFont);
	}
	
	/**
	 * Creates a hyperlink that permits to jump to an object inspector. 
	 * @param aCurrentObject If provided, reference to the current object will
	 * be displayed as "this" 
	 * @param aObject The object to link to.
	 */
	public static IRectangularGraphicObject object(
			ISeedFactory aSeedFactory,
			ILogBrowser aLogBrowser,
			Object aCurrentObject, 
			Object aObject, 
			XFont aFont)
	{
		// Check if this is a registered object.
		if (aObject instanceof ObjectId.ObjectUID)
		{
			ObjectId.ObjectUID theObject = (ObjectId.ObjectUID) aObject;
			Object theRegistered = aLogBrowser.getRegistered(theObject.getId());
			if (theRegistered != null) aObject = theRegistered;
		}
		
		if (aObject instanceof ObjectId)
		{
			ObjectId theId = (ObjectId) aObject;
			
			ITypeInfo theType = aLogBrowser.createObjectInspector(theId).getType();

			String theText;
			if (aCurrentObject != null && aCurrentObject.equals(aObject)) theText = "this";
			else theText = theType.getName() + " (" + theId + ")";

			return SVGHyperlink.create(
					aSeedFactory.objectSeed(theId), 
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
	
	public interface ISeedFactory
	{
		public Seed objectSeed(ObjectId aObjectId);
		public Seed cflowSeed(ILogEvent aEvent);
		public Seed behaviorSeed(IBehaviorInfo aBehavior);
		public Seed typeSeed(ITypeInfo aType);
	}
}
