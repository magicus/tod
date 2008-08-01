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
package tod.gui.kit.html;

import java.awt.Color;

import tod.gui.FontConfig;

public class HtmlText extends HtmlElement
{
	public static final int FONT_WEIGHT_NORMAL = 400;
	public static final int FONT_WEIGHT_BOLD = 800;
	
	/**
	 * Relative font size, in percent. 
	 */
	private int itsFontSize = FontConfig.NORMAL;
	private int itsFontWeight = FONT_WEIGHT_NORMAL;
	private Color itsColor;
	private StringBuilder itsExtraStyle;

	private String itsText;
	
	public HtmlText()
	{
	}

	public void setText(String aText)
	{
		itsText = aText;
		update();
	}

	public void setFontSize(int aFontSize)
	{
		itsFontSize = aFontSize;
	}

	public void setFontWeight(int aFontWeight)
	{
		itsFontWeight = aFontWeight;
	}

	public void setColor(Color aColor)
	{
		itsColor = aColor;
	}
	
	public void addExtraStyle(String aKey, String aValue)
	{
		if (itsExtraStyle == null) itsExtraStyle = new StringBuilder();
		itsExtraStyle.append(aKey+": "+aValue+"; ");
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
		
		if (itsFontWeight != FONT_WEIGHT_NORMAL)
		{
			aBuilder.append("font-weight: ");
			aBuilder.append(itsFontWeight);
			aBuilder.append("; "); 
		}
		
		if (itsExtraStyle != null) aBuilder.append(itsExtraStyle);
		
		aBuilder.append("'>");
		aBuilder.append(itsText);
		aBuilder.append("</span>");
	}
	
	public static HtmlText create(String aText, int aFontSize, int aFontWeight, Color aColor)
	{
		HtmlText theText = new HtmlText();
		theText.setText(aText);
		theText.setFontSize(aFontSize);
		theText.setColor(aColor);
		theText.setFontWeight(aFontWeight);
		return theText;
	}
	
	public static HtmlText create(String aText, int aFontSize, Color aColor)
	{
		return create(aText, aFontSize, FONT_WEIGHT_NORMAL, aColor);
	}
	
	public static HtmlText create(String aText, Color aColor)
	{
		return create(aText, FontConfig.NORMAL, FONT_WEIGHT_NORMAL, aColor);
	}
	
	public static HtmlText create(String aText)
	{
		return create(aText, FontConfig.NORMAL, Color.BLACK);
	}
	
	/**
	 * Creates a new html text element using {@link String#format(String, Object...)}.
	 */
	public static HtmlText createf(String aFormat, Object... aArgs)
	{
		return create(String.format(aFormat, aArgs));
	}
}
