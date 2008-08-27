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
package tod.gui;

import java.awt.Color;

import javax.swing.JComponent;

import tod.Util;
import tod.agent.transport.ObjectValue;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.activities.ActivitySeed;
import tod.gui.activities.cflow.CFlowSeed;
import tod.gui.activities.objecthistory.ObjectHistorySeed;
import tod.gui.activities.structure.StructureSeed;
import tod.gui.kit.Bus;
import tod.gui.kit.html.HtmlElement;
import tod.gui.kit.html.HtmlLink;
import tod.gui.kit.html.HtmlText;
import tod.gui.kit.messages.Message;
import tod.gui.kit.messages.ShowObjectMsg;
import tod.tools.scheduling.IJobScheduler;
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
	
	public static <T> T seed(IGUIManager aGUIManager, LabelFactory<T> aFactory, String aText, ActivitySeed aSeed)
	{
		return aFactory.createLink(aGUIManager, aText, aSeed);
	}
	
	
	public static <T> T history(IGUIManager aGUIManager, LabelFactory<T> aFactory, ObjectId aObject)
	{
		ObjectHistorySeed theSeed = new ObjectHistorySeed(aGUIManager.getSession().getLogBrowser(), aObject);
		return seed(aGUIManager, aFactory, "show history", theSeed);
	}
	
	public static <T> T type (IGUIManager aGUIManager, LabelFactory<T> aFactory, ITypeInfo aType)
	{
		StructureSeed theSeed = new StructureSeed(aGUIManager.getSession().getLogBrowser(), aType);
		return seed(aGUIManager, aFactory, aType.getName(), theSeed);
	}
	
	public static <T> T behavior(IGUIManager aGUIManager, LabelFactory<T> aFactory, IBehaviorInfo aBehavior)
	{
		StructureSeed theSeed = new StructureSeed(aGUIManager.getSession().getLogBrowser(), aBehavior);
		return seed(aGUIManager, aFactory, Util.getPrettyName(aBehavior.getName()), theSeed);		
	}
	
	/**
	 * An hyperlink that jumps to the cflow of the given event.
	 */
	public static <T> T event(IGUIManager aGUIManager, LabelFactory<T> aFactory, String aText, ILogEvent aEvent)
	{
		CFlowSeed theSeed = new CFlowSeed(aGUIManager.getSession().getLogBrowser(), aEvent);
		return seed(aGUIManager, aFactory, aText, theSeed);
	}
	
	public static <T> T object(
			IGUIManager aGUIManager, 
			LabelFactory<T> aFactory, 
			IJobScheduler aJobProcessor,
			Object aObject,
			ILogEvent aRefEvent,
			boolean aShowPackageNames)
	{
		return object(aGUIManager, aFactory, aJobProcessor, null, aObject, aRefEvent, aShowPackageNames);
	}
	
	/**
	 * Creates a hyperlink that permits to show an object. 
	 * @param aCurrentObject If provided, reference to the current object will
	 * be displayed as "this" 
	 * @param aObject The object to link to.
	 */
	public static <T> T object(
			IGUIManager aGUIManager, 
			LabelFactory<T> aFactory, 
			IJobScheduler aJobProcessor,
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
			String theText = Util.getObjectName(aGUIManager, theId, aCurrentObject, aRefEvent);
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
		public abstract T createLink(IGUIManager aGUIManager, String aLabel, ActivitySeed aSeed);
		public abstract T createLink(String aLabel, Message aMessage);
		public abstract T createText(String aLabel, Color aColor);
	}
	
	private static class SwingLabelFactory extends LabelFactory<JComponent>
	{
		@Override
		public JComponent createLink(IGUIManager aGUIManager, String aLabel, ActivitySeed aSeed)
		{
			return SeedHyperlink.create(aGUIManager, aSeed, aLabel, FontConfig.STD_FONT, Color.BLUE);		
		}
		
		@Override
		public JComponent createLink(String aLabel, Message aMessage)
		{
			return MessageHyperlink.create(aMessage, aLabel, FontConfig.STD_FONT, Color.BLUE);		
		}


		@Override
		public JComponent createText(String aLabel, Color aColor)
		{
			return ZLabel.create(aLabel, FontConfig.STD_FONT, aColor);
		}
	}
	
	private static class HtmlLabelFactory extends LabelFactory<HtmlElement>
	{

		@Override
		public HtmlElement createLink(final IGUIManager aGUIManager, String aLabel, final ActivitySeed aSeed)
		{
			return new HtmlLink(aLabel)
			{
				public void traverse()
				{
					aGUIManager.openSeed(aSeed, false);
				}
			};
		}

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
			return HtmlText.create(aLabel, aColor);
		}
	}
	
	private static class TextLabelFactory extends LabelFactory<String>
	{

		@Override
		public String createLink(IGUIManager aGUIManager, String aLabel, ActivitySeed aSeed)
		{
			return aLabel;
		}

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
