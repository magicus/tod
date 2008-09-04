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
package tod.gui.components.eventlist;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JToolTip;

import tod.Util;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.gui.FontConfig;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import tod.gui.kit.html.AsyncHtmlGroup;
import tod.gui.kit.html.HtmlBody;
import tod.gui.kit.html.HtmlElement;
import tod.gui.kit.html.HtmlParentElement;
import tod.gui.kit.html.HtmlText;
import tod.tools.scheduling.IJobScheduler.JobPriority;
import zz.utils.ui.StackLayout;
import zz.utils.ui.UIUtils;

public abstract class BehaviorCallNode extends AbstractSimpleEventNode
{
	
	private IBehaviorCallEvent itsEvent;
	
	private ExpanderWidget itsExpanderWidget;
	private boolean itsExpanded = false;
	
	public BehaviorCallNode(
			IGUIManager aGUIManager, 
			EventListPanel aListPanel,
			IBehaviorCallEvent aEvent)
	{
		super (aGUIManager, aListPanel);
		itsEvent = aEvent;
		createUI();
	}
	
	@Override
	protected void createUI()
	{
		super.createUI();
		
		itsExpanderWidget = new ExpanderWidget(Color.GRAY);
		itsExpanderWidget.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent aE)
			{
				itsExpanded = ! itsExpanded;
				updateHtml();
				aE.consume();
			}
		});
		
		addToGutter(itsExpanderWidget);
	}
	
	@Override
	protected void createHtmlUI(HtmlBody aBody)
	{
		if (itsExpanded) createFullView(aBody);
		else createShortView(aBody);
		
		createDebugInfo(aBody);
	}
	
	@Override
	public JToolTip createToolTip()
	{
		System.out.println("BehaviorCallNode.createToolTip()");
		return new MyToolTip();
	}
	
	@Override
	protected IBehaviorCallEvent getEvent()
	{
		return itsEvent;
	}
	
	/**
	 * Returns the result of the call.
	 * By default, the result of the exit event.
	 * @return
	 */
	protected Object getResult()
	{
		IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
		return theExitEvent != null ? theExitEvent.getResult() : null;		
	}
	
	protected IBehaviorInfo getBehavior()
	{
		IBehaviorInfo theBehavior = getEvent().getExecutedBehavior();
		if (theBehavior == null) theBehavior = getEvent().getCalledBehavior();
		
		return theBehavior;
	}
	
	/**
	 * Adds the components corresponding to the arguments of the call
	 * (short version).
	 * @param aComponent
	 */
	protected void fillShortArgs(HtmlParentElement aParent)
	{
		Object[] theArguments = getEvent().getArguments();

		aParent.addText("(");
		
		if (theArguments != null)
		{
			boolean theFirst = true;
			for (Object theArgument : theArguments)
			{
				if (theFirst) theFirst = false;
				else aParent.addText(", ");
				
				aParent.add(Hyperlinks.object(
						getGUIManager(),
						Hyperlinks.HTML,
						getJobScheduler(),
						theArgument, 
						getEvent(),
						showPackageNames()));
			}
		}
		else if (! getBehavior().getReturnType().isVoid())
		{
			aParent.addText("...");
		}
		
		aParent.addText(")");

	}
	
	/**
	 * Adds the components corresponding to the arguments of the call
	 * (full version).
	 * @param aComponent
	 */
	protected void fillFullArgs(HtmlParentElement aParent)
	{
		fillShortArgs(aParent);
	}
	
	/**
	 * Creates a summarized view of the event
	 */
	protected void createShortView(HtmlParentElement aParent)
	{
		HtmlElement theNamePrefix = createBehaviorNamePrefix();
		if (theNamePrefix != null) aParent.add(theNamePrefix);
		aParent.add(createShortBehaviorName());
		fillShortArgs(aParent);
		aParent.add(createResult(" ->"));
	}
	
	/**
	 * Creates a complete view of the event.
	 */
	protected void createFullView(HtmlParentElement aParent)
	{
		HtmlElement theNamePrefix = createBehaviorNamePrefix();
		if (theNamePrefix != null) aParent.add(theNamePrefix);
		aParent.add(createFullBehaviorName());
		fillFullArgs(aParent);
		
		aParent.addBr();
		
		aParent.addText("On: ");
		
		Object theTarget = getEvent().getTarget();
		if (theTarget == null) aParent.addText(" (static)");
		else aParent.add(Hyperlinks.object(
				getGUIManager(),
				Hyperlinks.HTML,
				getJobScheduler(),
				theTarget, 
				getEvent(),
				showPackageNames()));
		
		aParent.addBr();

		aParent.add(createResult(getResultPrefix() + ":"));
	}
	
	/**
	 * Creates a component that displays the behavior name.
	 */
	protected abstract HtmlElement createShortBehaviorName();
	
	/**
	 * Creates a component that displays the behavior name.
	 */
	protected abstract HtmlElement createFullBehaviorName();
	
	/**
	 * Returns a component that contains a prefix for the behavior name,
	 * such as "new "
	 */
	protected HtmlElement createBehaviorNamePrefix()
	{
		return null;
	}
	
	/**
	 * Returns a component that contains a prefix for the result,
	 * such as "Returned " or "Created ".
	 */
	protected String getResultPrefix()
	{
		return "";
	}
	
	protected HtmlElement createPackageName()
	{
		String thePackageName = Util.getPackageName(getBehavior().getType().getName());
		if (thePackageName.length() > 0) thePackageName += ".";
		return HtmlText.create(thePackageName, FontConfig.SMALL, Color.BLACK);
	}
	
	protected HtmlElement createResult(final String aResultPrefix)
	{
		return new AsyncHtmlGroup(getJobScheduler(), JobPriority.AUTO, aResultPrefix + " ...")
		{
			private List<HtmlElement> itsElements = new ArrayList<HtmlElement>();
			private Color itsExpanderColor;
			
			@Override
			protected void runJob()
			{
				IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
				Object theResult = getResult();
				IBehaviorInfo theBehavior = getBehavior();
				
				// Determine final expander color.
				if (theExitEvent == null) itsExpanderColor = Color.BLACK;
				else itsExpanderColor = theExitEvent.hasThrown() ? Color.RED : Color.BLUE;
				
				if (!getEvent().hasRealChildren()) 
					itsExpanderColor = UIUtils.getLighterColor(itsExpanderColor, 0.2f);
				
				// Create components
				if (theExitEvent == null)
				{
					itsElements.add(HtmlText.create(" [Behavior never returned]"));
				}
				else if (theExitEvent.hasThrown())
				{
					itsElements.add(HtmlText.create("["));
					itsElements.add(HtmlText.create("Thrown ", FontConfig.NORMAL, Color.RED));
					itsElements.add(HtmlText.create("]"));

					itsElements.add(Hyperlinks.object(
							getGUIManager(),
							Hyperlinks.HTML,
							null,
							theExitEvent.getResult(),
							theExitEvent,
							showPackageNames()));
				}
				else
				{
					if (theResult != null)
					{
						itsElements.add(HtmlText.create(aResultPrefix + " "));						
						itsElements.add(Hyperlinks.object(
								getGUIManager(),
								Hyperlinks.HTML,
								null,
								theResult,
								theExitEvent,
								showPackageNames()));
					}
					else if (! theBehavior.getReturnType().isVoid())
					{
						itsElements.add(HtmlText.create(aResultPrefix + " "));						
						itsElements.add(HtmlText.create("null"));
					}
				}
			}

			@Override
			protected void updateSuccess()
			{
				itsExpanderWidget.setColor(itsExpanderColor);
				for (HtmlElement theElement : itsElements) add(theElement);
			}
		};
	}
	
	private class MyToolTip extends JToolTip
	{
		
		public MyToolTip()
		{
			setLayout(new StackLayout());
			add(new JLabel("toto"));
		}

		@Override
		public String getUIClassID()
		{
			return "ComponentUI";
		}
	}
}
