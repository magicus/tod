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
package tod.gui.kit.html;

import java.awt.Color;

import tod.gui.FontConfig;
import zz.utils.ui.ZLabel;
import zz.utils.ui.text.XFont;

public class HtmlText extends HtmlElement
{
	/**
	 * Relative font size, in percent. 
	 */
	private int itsFontSize;
	private Color itsColor;

	private String itsText;

	public HtmlText(String aText)
	{
		this(aText, FontConfig.NORMAL);
	}
	
	public HtmlText(String aText, int aFontSize)
	{
		this(aText, null, aFontSize);		
	}
	
	public HtmlText(String aText, Color aColor)
	{
		this(aText, aColor, FontConfig.NORMAL);
	}
	
	public HtmlText(String aText, Color aColor, int aFontSize)
	{
		itsText = aText;
		itsFontSize = aFontSize;
		itsColor = aColor;
	}
	
	public void setText(String aText)
	{
		itsText = aText;
		update();
	}

	@Override
	public void render(StringBuilder aBuilder)
	{
		aBuilder.append("<span id='");
		aBuilder.append(getId());
		aBuilder.append("' style='");
		
		if (itsFontSize != 100)
		{
			aBuilder.append("font-size: ");
			aBuilder.append(itsFontSize);
			aBuilder.append("%; ");
		}
		
		if (itsColor != null)
		{
			aBuilder.append("color: ");
			aBuilder.append(HtmlUtils.toString(itsColor));
			aBuilder.append("; ");
		}
		
		aBuilder.append("'>");
		aBuilder.append(itsText);
		aBuilder.append("</span>");
	}
	
	public static HtmlText create(String aText, int aFontSize, Color aColor)
	{
		return new HtmlText(aText, aColor, aFontSize);
	}
	
	public static HtmlText create(String aText)
	{
		return create(aText, FontConfig.NORMAL, Color.BLACK);
	}

	
}
