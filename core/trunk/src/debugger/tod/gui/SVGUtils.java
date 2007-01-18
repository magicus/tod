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

import java.awt.Color;

import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.StackLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;

/**
 * Utility methods for creating common SVG components
 * @author gpothier
 */
public class SVGUtils
{
	public static IRectangularGraphicObject createMessage(
			String aHeader, 
			Color aHeaderColor,
			String aText, 
			Color aTextColor)
	{
		// Create container
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		if (aHeader != null && aHeader.length() > 0)theContainer.pChildren().add(SVGFlowText.create(aHeader, FontConfig.STD_HEADER_FONT, aHeaderColor));
		if (aText != null && aText.length() > 0) theContainer.pChildren().add(SVGFlowText.create(aText, FontConfig.STD_FONT, aTextColor));

		theContainer.setLayoutManager(new StackLayout());
		return theContainer;
	}
}
