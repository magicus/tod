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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tod.core.database.browser.LocationUtils;
import tod.core.database.browser.ShadowId;
import tod.core.database.browser.GroupingEventBrowser.EventGroup;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.gui.GUIUtils;
import tod.gui.kit.StdProperties;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;

/**
 * Represents a group of events that correspond to the same joinpoint shadow.
 * @author gpothier
 */
public class ShadowGroupNode extends AbstractEventGroupNode<ShadowId>
{
	private Set<BytecodeRole> itsHiddenRoles = new HashSet<BytecodeRole>();
	private List<ILogEvent> itsShownEvents = new ArrayList<ILogEvent>();
	
	private IProperty<IntimacyLevel> itsIntimacyLevelProperty;
	
	private IPropertyListener<IntimacyLevel> itsIntimacyListener = new PropertyListener<IntimacyLevel>()
	{
		public void propertyChanged(IProperty<IntimacyLevel> aProperty, IntimacyLevel aOldValue, IntimacyLevel aNewValue)
		{
			createUI();
		}
	};
	

	
	public ShadowGroupNode(EventListPanel aListPanel, EventGroup<ShadowId> aGroup)
	{
		super(aListPanel, aGroup);
		itsIntimacyLevelProperty = getBus().getProperty(StdProperties.INTIMACY_LEVEL);
		if (itsIntimacyLevelProperty != null)
		{
			itsIntimacyLevelProperty.addListener(itsIntimacyListener);
		}
		createUI();
	}
	
	private void setup()
	{
		itsHiddenRoles.clear();
		itsShownEvents.clear();
		
		IntimacyLevel theIntimacyLevel = itsIntimacyLevelProperty != null ?
				itsIntimacyLevelProperty.get()
				: IntimacyLevel.FULL;
		
		for (ILogEvent theEvent : getGroup().getEvents())
		{
			BytecodeRole theRole = LocationUtils.getEventRole(theEvent);
			if (theIntimacyLevel == null || theIntimacyLevel.showRole(theRole))
			{
				itsShownEvents.add(theEvent);
			}
			else itsHiddenRoles.add(theRole);
		}
	}
	
	@Override
	protected void createUI()
	{
		setup();
		super.createUI();
		
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		if (itsShownEvents.size() > 0) addToGutter(new JLabel("  "));
		
		String theAdvice;
		
		ShadowId theShadowId = getGroup().getGroupKey();
		ILogEvent theFirst = getGroup().getFirst();
		if (theFirst instanceof ICallerSideEvent)
		{
			ICallerSideEvent theEvent = (ICallerSideEvent) theFirst;
			IStructureDatabase theDatabase = theEvent.getOperationBehavior().getDatabase();
			SourceRange theAdviceSource = theDatabase.getAdviceSource(theShadowId.adviceSourceId);
			theAdvice = theAdviceSource.sourceFile+":"+theAdviceSource.startLine;
		}
		else theAdvice = "???";
		
		addToCaption(new JLabel(theAdvice+"  "));
		
		// We iterate over the values of the enum so as to always have the same display order
		for (BytecodeRole theRole : BytecodeRole.values())
		{
			if (itsHiddenRoles.contains(theRole))
			{
				addToCaption(new JLabel(GUIUtils.getRoleIcon(theRole)));
			}
		}
	}

	@Override
	protected JComponent getCenterComponent()
	{
		JPanel thePanel = new JPanel(GUIUtils.createStackLayout());
		for (ILogEvent theEvent : itsShownEvents)
		{
			AbstractEventNode theNode = EventListPanel.buildEventNode(getListPanel(), theEvent);
			thePanel.add(theNode);
		}
		return thePanel;
	}
}
