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

import javax.swing.JComponent;
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
import tod.gui.kit.AsyncPanel;
import zz.utils.ui.UIUtils;
import zz.utils.ui.ZLabel;
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

	private synchronized void createUI()
	{
		setLayout(new BorderLayout(0, 0));
		
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
		
		add(createShortView(), BorderLayout.CENTER);
	}
	
	@Override
	public JToolTip createToolTip()
	{
		System.out.println("BehaviorCallNode.createToolTip()");
		return super.createToolTip();
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
	protected void fillShortArgs(JComponent aContainer)
	{
		Object[] theArguments = getEvent().getArguments();

		XFont theFont = FontConfig.STD_FONT;
		aContainer.add(ZLabel.create("(", theFont, Color.BLACK));
		
		if (theArguments != null)
		{
			boolean theFirst = true;
			for (Object theArgument : theArguments)
			{
				if (theFirst) theFirst = false;
				else aContainer.add(ZLabel.create(", ", theFont, Color.BLACK));
				
				aContainer.add(Hyperlinks.object(
						getLogBrowser(),
						getJobProcessor(),
						theArgument, 
						getEvent(),
						theFont));
			}
		}
		else if (! getBehavior().getReturnType().isVoid())
		{
			aContainer.add(ZLabel.create("...", theFont, Color.BLACK));
		}
		
		aContainer.add(ZLabel.create(")", theFont, Color.BLACK));

	}
	
	/**
	 * Adds the components corresponding to the arguments of the call
	 * (full version).
	 * @param aComponent
	 */
	protected void fillFullArgs(JComponent aComponent)
	{
		fillShortArgs(aComponent);
	}
	
	/**
	 * Creates a summarized view of the event
	 */
	protected abstract JComponent createShortView();
	
	/**
	 * Creates a complete view of the event.
	 */
	protected abstract JComponent createFullView();
	
	protected abstract JComponent createBehaviorName();
	
	/**
	 * Returns a component that contains a prefix for the behavior name,
	 * such as "new "
	 * @return
	 */
	protected JComponent createBehaviorNamePrefix()
	{
		return null;
	}
	
	/**
	 * Returns a component that contains a prefix for the result,
	 * such as "Returned " or "Created ".
	 */
	protected JComponent createResultPrefix()
	{
		return null;
	}
	
	protected JComponent createPackageName()
	{
		return GUIUtils.createLabel(
				Util.getPackageName(getBehavior().getType().getName()), 
				FontConfig.SMALL_FONT, 
				Color.BLACK);
	}
	
	protected JComponent createResult()
	{
		return new AsyncPanel(getJobProcessor())
		{
			@Override
			protected void runJob()
			{
				getEvent().getExitEvent(); // This call caches call information
			}

			@Override
			protected void update()
			{
				IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
				Object theResult = getResult();
				IBehaviorInfo theBehavior = getBehavior();
				XFont theFont = FontConfig.STD_FONT;
				
				// Set final expander color.
				Color theExpanderColor;
				if (theExitEvent == null) theExpanderColor = Color.BLACK;
				else theExpanderColor = theExitEvent.hasThrown() ? Color.RED : Color.BLUE;
				
				if (!getEvent().hasRealChildren()) 
					theExpanderColor = UIUtils.getLighterColor(theExpanderColor, 0.2f);
				
				itsExpanderWidget.setColor(theExpanderColor);

				// Add result components
				setLayout(GUIUtils.createSequenceLayout());
				
				if (theExitEvent == null)
				{
					add(ZLabel.create("Behavior never returned", theFont, Color.BLACK));
				}
				else if (theExitEvent.hasThrown())
				{
					add(ZLabel.create("Thrown ", theFont, Color.RED));

					add(Hyperlinks.object(
							getLogBrowser(), 
							getJobProcessor(),
							theExitEvent.getResult(),
							theExitEvent,
							theFont));
				}
				else
				{
					JComponent thePrefix = createResultPrefix();
					if (thePrefix != null) add(thePrefix);

					if (theResult != null)
					{
						add(Hyperlinks.object(
								getLogBrowser(), 
								getJobProcessor(),
								theResult,
								theExitEvent,
								theFont));
					}
					else if (theBehavior.getReturnType().isVoid())
					{
						add(ZLabel.create("void", theFont, Color.BLACK));
					}
					else 
					{
						add(ZLabel.create("null", theFont, Color.BLACK));
					}
				}
			}
		};
	}
}
