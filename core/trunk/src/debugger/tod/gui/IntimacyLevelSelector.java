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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.gui.eventlist.IntimacyLevel;
import tod.gui.kit.Bus;
import tod.gui.kit.StdProperties;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyListener;
import zz.utils.ui.UIUtils;

/**
 * A panel that permits to select an {@link IntimacyLevel}.
 * @author gpothier
 */
public class IntimacyLevelSelector extends JPanel
{
	private IRWProperty<IntimacyLevel> itsIntimacyLevelProperty;
	
	private IPropertyListener<IntimacyLevel> itsIntimacyListener = new PropertyListener<IntimacyLevel>()
	{
		public void propertyChanged(IProperty<IntimacyLevel> aProperty, IntimacyLevel aOldValue, IntimacyLevel aNewValue)
		{
			updateState();
		}
	};
	
	private ActionListener itsButtonsListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent aE)
		{
			updateProperty();
		}
	};
	
	private JToggleButton[] itsButtons;
	
	public IntimacyLevelSelector()
	{
		createUI();
	}

	private void createUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		BytecodeRole[] theRoles = IntimacyLevel.ROLES;
		itsButtons = new JToggleButton[theRoles.length];
		Insets theInsets = new Insets (3, 3, 3, 3);
		for(int i=0;i<theRoles.length;i++)
		{
			itsButtons[i] = new JToggleButton(GUIUtils.getRoleIcon(theRoles[i]));
			itsButtons[i].setMargin(theInsets);
			itsButtons[i].addActionListener(itsButtonsListener);
			add(itsButtons[i]);
		}
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		itsIntimacyLevelProperty = Bus.get(this).getRWProperty(StdProperties.INTIMACY_LEVEL);
		if (itsIntimacyLevelProperty != null)
		{
			itsIntimacyLevelProperty.addHardListener(itsIntimacyListener);
		}
		updateState();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		if (itsIntimacyLevelProperty != null)
		{
			itsIntimacyLevelProperty.removeListener(itsIntimacyListener);
		}
	}
	
	private void updateState()
	{
		BytecodeRole[] theRoles = IntimacyLevel.ROLES;
		IntimacyLevel theLevel = itsIntimacyLevelProperty.get();
		for(int i=0;i<itsButtons.length;i++) 
		{
			itsButtons[i].setSelected(theLevel.showRole(theRoles[i]));
		}
	}
	
	private void updateProperty()
	{
		BytecodeRole[] theRoles = IntimacyLevel.ROLES;
		Set<BytecodeRole> theSelectedRoles = new HashSet<BytecodeRole>();
		for(int i=0;i<theRoles.length;i++) 
		{
			if (itsButtons[i].isSelected()) theSelectedRoles.add(theRoles[i]);
		}
		
		IntimacyLevel theLevel = itsIntimacyLevelProperty.get();
		IntimacyLevel theNewLevel = new IntimacyLevel(theSelectedRoles);
		
		if (! theNewLevel.equals(theLevel)) itsIntimacyLevelProperty.set(theNewLevel);
	}
	
}
