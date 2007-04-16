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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JToolTip;

import tod.Util;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import tod.gui.controlflow.CFlowView;
import tod.gui.kit.html.AsyncHtmlGroup;
import tod.gui.kit.html.HtmlBody;
import tod.gui.kit.html.HtmlElement;
import tod.gui.kit.html.HtmlGroup;
import tod.gui.kit.html.HtmlParentElement;
import tod.gui.kit.html.HtmlText;
import zz.utils.ui.StackLayout;
import zz.utils.ui.UIUtils;
import zz.utils.ui.text.XFont;

public abstract class BehaviorCallNode extends AbstractEventNode
{
	
	private IBehaviorCallEvent itsEvent;
	
	private ExpanderWidget itsExpanderWidget;
	
	public BehaviorCallNode(
			CFlowView aView,
			JobProcessor aJobProcessor,
			IBehaviorCallEvent aEvent)
	{
		super (aView, aJobProcessor);
		itsEvent = aEvent;
		createUI();
	}
	
	protected void createUI()
	{
		super.createUI();
		
		itsExpanderWidget = new ExpanderWidget(Color.GRAY);
		itsExpanderWidget.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent aE)
			{
				getView().getSeed().pParentEvent().set(getEvent());
				aE.consume();
			}
		});
		
		add(itsExpanderWidget, BorderLayout.WEST);
	}
	
	@Override
	protected void createHtmlUI(HtmlBody aBody)
	{
		if (isSelected()) createFullView(aBody);
		else createShortView(aBody);
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

		XFont theFont = FontConfig.STD_FONT;
		aParent.addText("(");
		
		if (theArguments != null)
		{
			boolean theFirst = true;
			for (Object theArgument : theArguments)
			{
				if (theFirst) theFirst = false;
				else aParent.addText(", ");
				
				aParent.add(Hyperlinks.object(
						Hyperlinks.HTML,
						getLogBrowser(),
						getJobProcessor(),
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
				Hyperlinks.HTML,
				getLogBrowser(),
				getJobProcessor(),
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
		return HtmlText.create(
				Util.getPackageName(getBehavior().getType().getName()), 
				FontConfig.SMALL, 
				Color.BLACK);
	}
	
	protected HtmlElement createResult(final String aResultPrefix)
	{
		return new AsyncHtmlGroup(getJobProcessor(), aResultPrefix + " ...")
		{
			@Override
			protected void runJob()
			{
				getEvent().getExitEvent(); // This call caches call information
			}

			@Override
			protected void updateUI()
			{
				IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
				Object theResult = getResult();
				IBehaviorInfo theBehavior = getBehavior();
				
				// Set final expander color.
				Color theExpanderColor;
				if (theExitEvent == null) theExpanderColor = Color.BLACK;
				else theExpanderColor = theExitEvent.hasThrown() ? Color.RED : Color.BLUE;
				
				if (!getEvent().hasRealChildren()) 
					theExpanderColor = UIUtils.getLighterColor(theExpanderColor, 0.2f);
				
				itsExpanderWidget.setColor(theExpanderColor);

				// Add result components
				
				if (theExitEvent == null)
				{
					add(HtmlText.create(" [Behavior never returned]"));
				}
				else if (theExitEvent.hasThrown())
				{
					add(HtmlText.create("["));
					add(HtmlText.create("Thrown ", FontConfig.NORMAL, Color.RED));
					add(HtmlText.create("]"));

					add(Hyperlinks.object(
							Hyperlinks.HTML,
							getLogBrowser(), 
							getJobProcessor(),
							theExitEvent.getResult(),
							theExitEvent,
							showPackageNames()));
				}
				else
				{
					if (theResult != null)
					{
						add(HtmlText.create(aResultPrefix + " "));						
						add(Hyperlinks.object(
								Hyperlinks.HTML,
								getLogBrowser(), 
								getJobProcessor(),
								theResult,
								theExitEvent,
								showPackageNames()));
					}
					else if (! theBehavior.getReturnType().isVoid())
					{
						add(HtmlText.create(aResultPrefix + " "));						
						add(HtmlText.create("null"));
					}
				}
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
