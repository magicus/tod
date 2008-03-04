/*
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
package tod.gui.eventlist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import tod.core.DebugFlags;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.LocationUtils;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IBehaviorInfo.BytecodeTagType;
import tod.gui.GUIUtils;
import tod.gui.JobProcessor;
import tod.gui.kit.Bus;
import tod.gui.kit.html.HtmlBody;
import tod.gui.kit.html.HtmlComponent;
import tod.gui.kit.html.HtmlDoc;
import tod.gui.kit.html.HtmlParentElement;
import tod.gui.kit.html.HtmlText;
import tod.gui.kit.messages.EventActivatedMsg;
import tod.gui.kit.messages.EventSelectedMsg;
import tod.gui.kit.messages.EventActivatedMsg.ActivationMethod;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;
import zz.utils.Utils;
import zz.utils.ui.MousePanel;

/**
 * Base class for simple event nodes that display a block of html.
 * @author gpothier
 */
public abstract class AbstractSimpleEventNode extends AbstractEventNode
{
	private HtmlComponent itsHtmlComponent;
	private HtmlDoc itsDoc;
	
	public AbstractSimpleEventNode(EventListPanel aListPanel)
	{
		super(aListPanel);
		
		itsDoc = new HtmlDoc();
		itsHtmlComponent = new HtmlComponent();
		itsHtmlComponent.setOpaque(false);
		itsHtmlComponent.setDoc(itsDoc);
		itsHtmlComponent.addMouseListener(this);
	}
	
	/**
	 * Default UI creation. 
	 * The html component is placed at the center of a {@link BorderLayout}.
	 */
	protected void createUI()
	{
		super.createUI();
		updateHtml();
		setupRoleIcons();
	}
	
	private void setupRoleIcons()
	{
		if (! getConfig().get(TODConfig.WITH_ASPECTS)) return;
		BytecodeRole theRole = LocationUtils.getEventRole(getEvent());
		if (theRole == null) return;

		ImageIcon theIcon = GUIUtils.getRoleIcon(theRole);
		if (theIcon == null) return;

		addToGutter(new JLabel(theIcon));
	}
	
	protected void updateHtml()
	{
		HtmlBody theBody = itsDoc.getRoot();
		theBody.clear();
		createHtmlUI(theBody);
		itsDoc.update(theBody);
	}
	
	protected abstract void createHtmlUI(HtmlBody aBody);
	
	/**
	 * Adds debugging info to the given element, if debugging info is enabled.
	 */
	protected void createDebugInfo(HtmlParentElement aParent)
	{
		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			String theLocation = "?";
			if (getEvent() instanceof ICallerSideEvent)
			{
				ICallerSideEvent theCallerSideEvent = (ICallerSideEvent) getEvent();
				theLocation = ""+theCallerSideEvent.getOperationBytecodeIndex();
			}
			
			int theSourceId = -1;
			int theShadowId = -10;
			if (getEvent() instanceof ICallerSideEvent)
			{
				ICallerSideEvent theCallerSideEvent = (ICallerSideEvent) getEvent();
				theSourceId = theCallerSideEvent.getAdviceSourceId();
				
				IBehaviorInfo theOperationBehavior = theCallerSideEvent.getOperationBehavior();
				
				if (theOperationBehavior != null)
				{
					Integer theTag = theOperationBehavior.getTag(
							BytecodeTagType.INSTR_SHADOW, 
							theCallerSideEvent.getOperationBytecodeIndex());
					
					if (theTag != null) theShadowId = theTag;
				}
			}
			
			aParent.add(HtmlText.createf(
					" (ts: %d, loc: %s, th: %d, d: %d, asid: %d, sid: %d)",
					getEvent().getTimestamp(),
					theLocation,
					getEvent().getThread().getId(),
					getEvent().getDepth(),
					theSourceId,
					theShadowId));
		}
	}
	
	@Override
	protected JComponent getCenterComponent()
	{
		return itsHtmlComponent;
	}
	
	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		getListPanel().pSelectedEvent().set(getEvent());
		ILogEvent theEvent = getEvent();
		Bus.get(this).postMessage(new EventSelectedMsg(theEvent, SelectionMethod.SELECT_IN_LIST));
		aEvent.consume();			
		
		if (aEvent.getClickCount() == 2)
		{
			Bus.get(this).postMessage(new EventActivatedMsg(getEvent(), ActivationMethod.DOUBLE_CLICK));
			getListPanel().eventActivated(getEvent());
		}
	}
	
	/**
	 * Called when this event is selected.
	 */
	protected void selected()
	{
//		updateHtml();
		repaint();
	}

	/**
	 * Called when this node is deselected.
	 */
	protected void deselected()
	{
//		updateHtml();
		repaint();
	}
	
	protected boolean isSelected()
	{
		return Utils.equalOrBothNull(getEvent(), getListPanel().pSelectedEvent().get());
	}
	
	@Override
	protected void paintComponent(Graphics aG)
	{
		aG.setColor(isSelected() ? Color.YELLOW : Color.WHITE);
		aG.fillRect(0, 0, getWidth(), getHeight());
	}
}
