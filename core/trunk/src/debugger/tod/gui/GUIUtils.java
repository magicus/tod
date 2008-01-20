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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.core.database.structure.IBehaviorInfo.BytecodeRole;

import zz.utils.ui.GridStackLayout;
import zz.utils.ui.ZLabel;
import zz.utils.ui.text.XFont;

/**
 * Utility methods for creating common SVG components
 * @author gpothier
 */
public class GUIUtils
{
	private static final int ROLE_ICON_WIDTH = 15;
	public static Map<BytecodeRole, ImageIcon> itsRoleIconMap;
	
	public static JComponent createMessage(
			String aHeader, 
			Color aHeaderColor,
			String aText, 
			Color aTextColor)
	{
		// Create container
		JPanel theContainer = new JPanel(new GridStackLayout(1, 0, 0, false, false));
		
		if (aHeader != null && aHeader.length() > 0)theContainer.add(ZLabel.create(aHeader, FontConfig.STD_HEADER_FONT, aHeaderColor));
		if (aText != null && aText.length() > 0) theContainer.add(ZLabel.create(aText, FontConfig.STD_FONT, aTextColor));

		return theContainer;
	}
	
	/**
	 * Creates a standard sequence layout.
	 */
	public static FlowLayout createSequenceLayout()
	{
		return new FlowLayout(FlowLayout.LEFT, 0, 0);
	}
	
	/**
	 * Creates a standard stack layout.
	 */
	public static LayoutManager createStackLayout()
	{
		return new GridStackLayout(1, 0, 0, true, false);
	}
	
	/**
	 * Creates a standard border layout.
	 */
	public static LayoutManager createBorderLayout()
	{
		return new BorderLayout(0, 0);
	}
	
	/**
	 * return a label that delegates mouse events to its parents 
	 */
	public static ZLabel createLabel(String aText, XFont aFont, Color aColor)
	{
		ZLabel theLabel = ZLabel.create(aText, aFont, aColor);
		return theLabel;
	}
	
	public static ZLabel createLabel(String aText)
	{
		return createLabel(aText, FontConfig.STD_FONT, Color.BLACK);
	}
	
	/**
	 * Returns the icon corresponding to a given bytecode role.
	 */
	public static ImageIcon getRoleIcon(BytecodeRole aRole)
	{
		if (itsRoleIconMap == null)
		{
			itsRoleIconMap = new HashMap<BytecodeRole, ImageIcon>();
			itsRoleIconMap.put(BytecodeRole.ADVICE_ARG_SETUP, Resources.ICON_ROLE_CONTEXT_EXPOSURE.asIconW(ROLE_ICON_WIDTH));
			itsRoleIconMap.put(BytecodeRole.ADVICE_EXECUTE, Resources.ICON_ROLE_ADVICE_EXECUTION.asIconW(ROLE_ICON_WIDTH));
			itsRoleIconMap.put(BytecodeRole.ADVICE_TEST, Resources.ICON_ROLE_RESIDUE_EVALUATION.asIconW(ROLE_ICON_WIDTH));
			itsRoleIconMap.put(BytecodeRole.INLINED_ADVICE, Resources.ICON_ROLE_ADVICE_EXECUTION.asIconW(ROLE_ICON_WIDTH));
		}
		
		return itsRoleIconMap.get(aRole);
	}
}
