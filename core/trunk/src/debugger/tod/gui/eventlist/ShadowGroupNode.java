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
import tod.core.database.structure.IClassInfo;
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
			IClassInfo theType = theEvent.getOperationBehavior().getType();
			SourceRange theAdviceSource = theType.getAdviceSource(theShadowId.adviceSourceId);
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
