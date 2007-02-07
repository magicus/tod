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
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import tod.gui.controlflow.CFlowView;
import tod.gui.controlflow.CFlowViewUtils;
import zz.utils.ui.GridStackLayout;
import zz.utils.ui.UIUtils;
import zz.utils.ui.ZLabel;
import zz.utils.ui.text.XFont;

public abstract class BehaviorCallNode extends AbstractEventNode
{
	private IBehaviorCallEvent itsEvent;
	
	private JComponent itsHeader;
	private JComponent itsFooter;
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

	private void createUI()
	{
		setLayout(new BorderLayout(0, 0));
		IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
		
		Color theExpanderColor;
		if (theExitEvent == null) theExpanderColor = Color.BLACK;
		else theExpanderColor = theExitEvent.hasThrown() ? Color.RED : Color.BLUE;
		
		if (!getEvent().hasRealChildren()) 
			theExpanderColor = UIUtils.getLighterColor(theExpanderColor, 0.2f);
		
		itsExpanderWidget = new ExpanderWidget(theExpanderColor);
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
		
		JPanel theContainer = new JPanel (new BorderLayout(0, 0));
		theContainer.setOpaque(false);
		
		itsHeader = createHeader(FontConfig.STD_FONT);
		itsFooter = createFooter(FontConfig.STD_FONT);
		
		theContainer.add(itsHeader, BorderLayout.NORTH);
		theContainer.add(itsFooter, BorderLayout.SOUTH);
		
		add(theContainer, BorderLayout.CENTER);
	}
	
	@Override
	protected IBehaviorCallEvent getEvent()
	{
		return itsEvent;
	}
	
	protected JComponent createHeader(XFont aFont)
	{
		JPanel theContainer = new JPanel(GUIUtils.createSequenceLayout());
		theContainer.setOpaque(false);
		
		fillHeaderPrefix(theContainer, aFont);
		Object[] theArguments = getEvent().getArguments();
		CFlowViewUtils.addArguments(
				getSeedFactory(), 
				getLogBrowser(), 
				getJobProcessor(),
				theContainer,
				theArguments,
				aFont);

		return theContainer;
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
	
	protected JComponent createFooter(XFont aFont)
	{
		JPanel theContainer = new JPanel(GUIUtils.createSequenceLayout());
		theContainer.setOpaque(false);

		IBehaviorInfo theBehavior = getEvent().getExecutedBehavior();
		if (theBehavior == null) theBehavior = getEvent().getCalledBehavior();
		
		IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
		Object theResult = getResult();
		
		if (theExitEvent == null)
		{
			theContainer.add(ZLabel.create("Behavior never returned", aFont, Color.BLACK));
		}
		else if (theExitEvent.hasThrown())
		{
			theContainer.add(ZLabel.create("Thrown ", aFont, Color.RED));

			theContainer.add(Hyperlinks.object(
					getSeedFactory(), 
					getLogBrowser(), 
					getJobProcessor(),
					theExitEvent.getResult(), 
					aFont));
		}
		else
		{
			fillFooterPrefix(theContainer, aFont);

			if (theResult != null)
			{
				theContainer.add(Hyperlinks.object(
						getSeedFactory(), 
						getLogBrowser(), 
						getJobProcessor(),
						theResult, 
						aFont));
			}
			else if (theBehavior.getReturnType().isVoid())
			{
				theContainer.add(ZLabel.create("void", aFont, Color.BLACK));
			}
			else 
			{
				theContainer.add(ZLabel.create("null", aFont, Color.BLACK));
			}
		}
		
		return theContainer;
	}
	
	/**
	 * Adds the prefix to the header. Eg.: "new MyClass" or "MyClass.myMethod" 
	 */
	protected abstract void fillHeaderPrefix(
			JComponent aContainer,
			XFont aFont);
	
	/**
	 * Adds the prefix to the footer. Eg.: "Created " or "Returned "
	 */
	protected abstract void fillFooterPrefix(
			JComponent aContainer,
			XFont aFont);
	
}
