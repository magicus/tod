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
package tod.gui.eventlist;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import tod.core.database.browser.LocationUtils;
import tod.core.database.browser.ShadowId;
import tod.core.database.browser.GroupingEventBrowser.EventGroup;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.Resources;
import tod.gui.settings.IntimacySettings;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.ui.ResourceUtils;
import zz.utils.ui.ResourceUtils.ImageResource;

/**
 * Represents a group of events that correspond to the same joinpoint shadow.
 * @author gpothier
 */
public class ShadowGroupNode extends AbstractEventGroupNode<ShadowId>
{
	private static final int ROLE_ICON_SIZE = 15;
	
	private Set<BytecodeRole> itsHiddenRoles = new HashSet<BytecodeRole>();
	private Set<BytecodeRole> itsRoles = new HashSet<BytecodeRole>();
	private List<ILogEvent> itsShownEvents = new ArrayList<ILogEvent>();
	private List<AbstractEventNode> itsChildrenNodes = new ArrayList<AbstractEventNode>();
	private boolean itsFullObliviousness;
	
	private IEventListener<Void> itsIntimacyListener = new IEventListener<Void>()
	{
		public void fired(IEvent< ? extends Void> aEvent, Void aData)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					createUI();
				}
			});
		}
	};
	
	public ShadowGroupNode(IGUIManager aGUIManager, EventListPanel aListPanel, EventGroup<ShadowId> aGroup)
	{
		super(aGUIManager, aListPanel, aGroup);
	}
	
	private IntimacySettings getIntimacySettings()
	{
		return getGUIManager().getSettings().getIntimacySettings();
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		getIntimacySettings().eChanged.addListener(itsIntimacyListener);
		createUI(); // (re)create the UI here because intimacy can have changed while out of gui.
	}

	@Override
	public void removeNotify()
	{
		super.removeNotify();
		getIntimacySettings().eChanged.removeListener(itsIntimacyListener);
	}
	
	private void setup()
	{
		itsHiddenRoles.clear();
		itsShownEvents.clear();
		itsRoles.clear();
		
		IntimacyLevel theIntimacyLevel = getIntimacySettings().getIntimacyLevel(getGroupKey().adviceSourceId);

		itsFullObliviousness = theIntimacyLevel == null;
		if (itsFullObliviousness) return; 
				
		for (ILogEvent theEvent : getGroup().getEvents())
		{
			BytecodeRole theRole = LocationUtils.getEventRole(theEvent);
			if (theIntimacyLevel == null || theIntimacyLevel.showRole(theRole))
			{
				itsShownEvents.add(theEvent);
			}
			else itsHiddenRoles.add(theRole);
			
			itsRoles.add(theRole);
		}
	}
	
	private IAdviceInfo getAdvice()
	{
		ShadowId theShadowId = getGroup().getGroupKey();
		ILogEvent theFirst = getGroup().getFirst();
		
		if (theFirst instanceof ICallerSideEvent)
		{
			ICallerSideEvent theEvent = (ICallerSideEvent) theFirst;
			IStructureDatabase theDatabase = theEvent.getOperationBehavior().getDatabase();
			return theDatabase.getAdvice(theShadowId.adviceSourceId);
		}
		
		return null;
	}
	
	@Override
	protected void createUI()
	{
		setup();
		super.createUI();
		
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		if (itsFullObliviousness) return;
		
		if (itsShownEvents.size() > 0) addToGutter(new JLabel("  "));
		
		String theAdviceName;
		
		IAdviceInfo theAdvice = getAdvice();
		theAdviceName = theAdvice != null ? theAdvice.getName() : "???";
		
		addToCaption(new JLabel(theAdviceName+"  "));
		
		// We iterate over the values of the enum so as to always have the same display order
		for (BytecodeRole theRole : IntimacyLevel.ROLES)
		{
			if (! itsRoles.contains(theRole)) continue;
			addToCaption(new RoleLabel(theRole));
			addToCaption(new JLabel(" "));
		}
	}

	@Override
	protected JComponent getCenterComponent()
	{
		itsChildrenNodes.clear();
		JPanel thePanel = new JPanel(GUIUtils.createStackLayout());
		for (ILogEvent theEvent : itsShownEvents)
		{
			AbstractEventNode theNode = EventListPanel.getEventNode(
					getGUIManager(), 
					getListPanel(), 
					theEvent);
			
			thePanel.add(theNode);
			itsChildrenNodes.add(theNode);
		}
		return thePanel;
	}
	
	@Override
	public void mousePressed(MouseEvent aE)
	{
		getGUIManager().gotoSource(getAdvice().getSourceRange());
	}
	
	@Override
	public Iterable<AbstractEventNode> getChildrenNodes()
	{
		return itsChildrenNodes;
	}
	
	/**
	 * A caption label for a particular bytecode role
	 * @author gpothier
	 */
	private class RoleLabel extends JLabel
	implements IEventListener<Void>
	{
		private final BytecodeRole itsRole;
		private final ImageIcon itsVisibleIcon;
		private final ImageIcon itsHiddenIcon;

		public RoleLabel(BytecodeRole aRole)
		{
			itsRole = aRole;

			ImageResource theRoleIcon = GUIUtils.getRoleIcon(itsRole);
			if (theRoleIcon != null)
			{
				ImageResource theHiddenImg =  ResourceUtils.overlay(
						theRoleIcon,
						Resources.ICON_ROLEUNSELECTEDMARKER);
				
				ImageResource theVisibleImg = ResourceUtils.overlay(
						theRoleIcon,
						Resources.ICON_ROLESELECTEDMARKER);
				
//				itsVisibleIcon = theRoleIcon.asIcon(ROLE_ICON_SIZE);
//				itsHiddenIcon = new ImageIcon(GrayFilter.createDisabledImage(itsVisibleIcon.getImage()));
				
				itsVisibleIcon = theVisibleImg.asIcon(ROLE_ICON_SIZE);
				itsHiddenIcon =  theHiddenImg.asIcon(ROLE_ICON_SIZE);
				
				updateIcon();
				
				addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent aE)
					{
						final IntimacySettings theSettings = getGUIManager().getSettings().getIntimacySettings();
						IntimacyLevel theLevel = theSettings.getIntimacyLevel(getAdvice().getId());
						
						final Set<BytecodeRole> theRoles = new HashSet<BytecodeRole>();
						theRoles.addAll(theLevel.getRoles());
						if (! theRoles.remove(itsRole)) theRoles.add(itsRole);
						
						theSettings.setIntimacyLevel(getAdvice().getId(), new IntimacyLevel(theRoles));
					}
				});
			}
			else
			{
				itsVisibleIcon = null;
				itsHiddenIcon = null;
				setText("?? - "+itsRole);
			}
		}
		
		@Override
		public void addNotify()
		{
			super.addNotify();
			getGUIManager().getSettings().getIntimacySettings().eChanged.addListener(this);
		}
		
		@Override
		public void removeNotify()
		{
			super.removeNotify();
			getGUIManager().getSettings().getIntimacySettings().eChanged.removeListener(this);
		}

		private void updateIcon()
		{
			IntimacySettings theSettings = getGUIManager().getSettings().getIntimacySettings();
			IntimacyLevel theLevel = theSettings.getIntimacyLevel(getAdvice().getId());
			setIcon(theLevel.showRole(itsRole) ? itsVisibleIcon : itsHiddenIcon);
		}
		
		public void fired(IEvent< ? extends Void> aEvent, Void aData)
		{
			updateIcon();
		}
	}
}
