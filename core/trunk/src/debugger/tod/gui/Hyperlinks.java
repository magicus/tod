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

import javax.swing.JComponent;

import tod.Util;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.kit.messages.ShowBehaviorMsg;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.kit.messages.ShowObjectHistoryMsg;
import tod.gui.kit.messages.ShowObjectMsg;
import tod.gui.kit.messages.ShowTypeMsg;
import tod.gui.seed.Seed;
import zz.utils.ui.ZLabel;
import zz.utils.ui.text.XFont;

/**
 * This class contains static methods that created standard
 * hyperlinks for types, methods, etc.
 * @author gpothier
 */
public class Hyperlinks
{
	public static JComponent history(ObjectId aObject, XFont aFont)
	{
		return MessageHyperlink.create(
				new ShowObjectHistoryMsg(aObject),
				"history",
				aFont,
				Color.BLUE);
	}
	
	public static JComponent type (ITypeInfo aType, XFont aFont)
	{
		return MessageHyperlink.create(
				new ShowTypeMsg(aType),
				aType.getName(), 
				aFont, 
				Color.BLUE);
	}
	
	public static JComponent behavior(IBehaviorInfo aBehavior, XFont aFont)
	{
		return MessageHyperlink.create(
				new ShowBehaviorMsg(aBehavior), 
				Util.getPrettyName(aBehavior.getName()),
				aFont, 
				Color.BLUE);		
	}
	
	/**
	 * An hyperlink that jumps to the cflow of the given event.
	 */
	public static JComponent event(String aText, ILogEvent aEvent, XFont aFont)
	{
		return MessageHyperlink.create(
				new ShowCFlowMsg(aEvent), 
				aText,
				aFont, 
				Color.BLUE);
	}
	
	public static JComponent object(
			ILogBrowser aLogBrowser, 
			JobProcessor aJobProcessor,
			Object aObject,
			ILogEvent aRefEvent,
			XFont aFont)
	{
		return object(aLogBrowser, aJobProcessor, null, aObject, aRefEvent, aFont);
	}
	
	/**
	 * Creates a hyperlink that permits to show an object. 
	 * @param aCurrentObject If provided, reference to the current object will
	 * be displayed as "this" 
	 * @param aObject The object to link to.
	 */
	public static JComponent object(
			ILogBrowser aLogBrowser,
			JobProcessor aJobProcessor,
			Object aCurrentObject, 
			Object aObject, 
			ILogEvent aRefEvent,
			XFont aFont)
	{
		// Check if this is a registered object.
		if (aObject instanceof ObjectId)
		{
			ObjectId theObjectId = (ObjectId) aObject;
			Object theRegistered = aLogBrowser.getRegistered(theObjectId);
			if (theRegistered != null) aObject = theRegistered;
		}
		
		if (aObject instanceof ObjectId)
		{
			ObjectId theId = (ObjectId) aObject;
			
			String theText;
			if (aCurrentObject != null && aCurrentObject.equals(aObject)) theText = "this";
//			else if (aJobProcessor != null) 
//			{
//				return new ObjectHyperlink(
//						aSeedFactory.objectSeed(theId),
//						aLogBrowser,
//						aJobProcessor,
//						theId,
//						aFont);
//			}
			else 
			{
				ITypeInfo theType = aLogBrowser.createObjectInspector(theId).getType();
				theText = theType.getName() + " (" + theId + ")";
			}
			
			return MessageHyperlink.create(
					new ShowObjectMsg(theId, aRefEvent), 
					theText, 
					aFont, 
					Color.BLUE);
		}
		else if (aObject instanceof String)
		{
			String theString = (String) aObject;
			return ZLabel.create("\""+theString+"\"", aFont, Color.GRAY);
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
			return ZLabel.create(theBuilder.toString(), aFont, Color.RED);
		}
		else 
		{
			return ZLabel.create(""+aObject, aFont, Color.GRAY);
		}

		
	}
	
}
