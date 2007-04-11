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

import javax.swing.JComponent;
import javax.swing.JPanel;

import zz.utils.ui.GridStackLayout;
import zz.utils.ui.ZLabel;
import zz.utils.ui.text.XFont;

/**
 * Utility methods for creating common SVG components
 * @author gpothier
 */
public class GUIUtils
{
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
	
	public static ZLabel createLabel(String aText, XFont aFont, Color aColor)
	{
		return ZLabel.create(aText, aFont, aColor);
	}
	
	public static ZLabel createLabel(String aText)
	{
		return createLabel(aText, FontConfig.STD_FONT, Color.BLACK);
	}
}
