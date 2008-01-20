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
