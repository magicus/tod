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
import tod.agent.transport.ObjectValue;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.formatter.CustomFormatterRegistry;
import tod.gui.formatter.CustomObjectFormatter;
import tod.gui.kit.Bus;
import tod.gui.kit.html.HtmlElement;
import tod.gui.kit.html.HtmlLink;
import tod.gui.kit.html.HtmlText;
import tod.gui.kit.messages.Message;
import tod.gui.kit.messages.ShowBehaviorMsg;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.kit.messages.ShowObjectHistoryMsg;
import tod.gui.kit.messages.ShowObjectMsg;
import tod.gui.kit.messages.ShowTypeMsg;
import zz.utils.ui.ZLabel;

/**
 * This class contains static methods that created standard
 * hyperlinks for types, methods, etc.
 * @author gpothier
 */
public class Hyperlinks
{
	public static final HtmlLabelFactory HTML = new HtmlLabelFactory();
	public static final SwingLabelFactory SWING = new SwingLabelFactory();
	public static final TextLabelFactory TEXT = new TextLabelFactory();
	
	public static <T> T history(LabelFactory<T> aFactory, ObjectId aObject)
	{
		return aFactory.createLink("show history", new ShowObjectHistoryMsg(aObject));
	}
	
	public static <T> T type (LabelFactory<T> aFactory, ITypeInfo aType)
	{
		return aFactory.createLink(aType.getName(), new ShowTypeMsg(aType));
	}
	
	public static <T> T behavior(LabelFactory<T> aFactory, IBehaviorInfo aBehavior)
	{
		return aFactory.createLink(
				Util.getPrettyName(aBehavior.getName()),
				new ShowBehaviorMsg(aBehavior));		
	}
	
	/**
	 * An hyperlink that jumps to the cflow of the given event.
	 */
	public static <T> T event(LabelFactory<T> aFactory, String aText, ILogEvent aEvent)
	{
		return aFactory.createLink(aText, new ShowCFlowMsg(aEvent));
	}
	
	public static <T> T object(
			LabelFactory<T> aFactory, 
			IGUIManager aGUIManager,
			JobProcessor aJobProcessor,
			Object aObject,
			ILogEvent aRefEvent,
			boolean aShowPackageNames)
	{
		return object(aFactory, aGUIManager, aJobProcessor, null, aObject, aRefEvent, aShowPackageNames);
	}
	
	/**
	 * Creates a hyperlink that permits to show an object. 
	 * @param aCurrentObject If provided, reference to the current object will
	 * be displayed as "this" 
	 * @param aObject The object to link to.
	 */
	public static <T> T object(
			LabelFactory<T> aFactory, 
			IGUIManager aGUIManager,
			JobProcessor aJobProcessor,
			Object aCurrentObject, 
			Object aObject, 
			ILogEvent aRefEvent,
			boolean aShowPackageNames)
	{
		ILogBrowser theLogBrowser = aGUIManager.getSession().getLogBrowser();
		
		// Check if this is a registered object.
		if (aObject instanceof ObjectId)
		{
			ObjectId theObjectId = (ObjectId) aObject;
			Object theRegistered = theLogBrowser.getRegistered(theObjectId);
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
				IObjectInspector theInspector = theLogBrowser.createObjectInspector(theId);
				theInspector.setReferenceEvent(aRefEvent);
				theText = CustomFormatterRegistry.formatObjectShort(
						aGUIManager, 
						theInspector, 
						aShowPackageNames);
			}
			
			return aFactory.createLink(theText, new ShowObjectMsg(theText, theId, aRefEvent));
		}
		else if (aObject instanceof String)
		{
			String theString = (String) aObject;
			return aFactory.createText("\""+theString+"\"", Color.GRAY);
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
			return aFactory.createText(theBuilder.toString(), Color.RED);
		}
		else if (aObject instanceof ObjectValue)
		{
			ObjectValue theObjectValue = (ObjectValue) aObject;
			
			if (theObjectValue.isThrowable())
			{
				StringBuilder theBuilder = new StringBuilder();
				theBuilder.append(theObjectValue.getClassName());
				
				theBuilder.append(" (");
				theBuilder.append(theObjectValue.getFieldValue("detailMessage"));
				theBuilder.append(')');

				return aFactory.createText(theBuilder.toString(), Color.RED);
			}
			else
			{
				StringBuilder theBuilder = new StringBuilder();
				theBuilder.append(theObjectValue.getClassName());
				
				theBuilder.append(" !(");
				theBuilder.append(theObjectValue.asString());
				theBuilder.append(')');

				return aFactory.createText(theBuilder.toString(), Color.BLACK);
			}
		}
		else 
		{
			return aFactory.createText(""+aObject, Color.GRAY);
		}
	}
	
	private static abstract class LabelFactory<T>
	{
		public abstract T createLink(String aLabel, Message aMessage);
		public abstract T createText(String aLabel, Color aColor);
	}
	
	private static class SwingLabelFactory extends LabelFactory<JComponent>
	{
		@Override
		public JComponent createLink(String aLabel, Message aMessage)
		{
			return MessageHyperlink.create(aMessage, aLabel, FontConfig.STD_FONT, Color.BLUE);		}

		@Override
		public JComponent createText(String aLabel, Color aColor)
		{
			return ZLabel.create(aLabel, FontConfig.STD_FONT, aColor);
		}
	}
	
	private static class HtmlLabelFactory extends LabelFactory<HtmlElement>
	{

		@Override
		public HtmlElement createLink(String aLabel, final Message aMessage)
		{
			return new HtmlLink(aLabel)
			{
				public void traverse()
				{
					Bus.get(getComponent()).postMessage(aMessage);				
				}
			};
		}

		@Override
		public HtmlElement createText(String aLabel, Color aColor)
		{
			return new HtmlText(aLabel, aColor);
		}
	}
	
	private static class TextLabelFactory extends LabelFactory<String>
	{

		@Override
		public String createLink(String aLabel, Message aMessage)
		{
			return aLabel;
		}

		@Override
		public String createText(String aLabel, Color aColor)
		{
			return aLabel;
		}
		
	}
	
}
