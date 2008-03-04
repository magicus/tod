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
	
	/**
	 * Creates a new html text element using {@link String#format(String, Object...)}.
	 */
	public static HtmlText createf(String aFormat, Object... aArgs)
	{
		return create(String.format(aFormat, aArgs));
	}


	/**
	 * From http://www.rgagnon.com/javadetails/java-0306.html
	 */
	public static final String escapeHTML(String s)
	{
		StringBuilder sb = new StringBuilder();
		int n = s.length();
		for (int i=0;i<n;i++)
		{
			char c = s.charAt(i);
			switch (c)
			{
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case 'à':
				sb.append("&agrave;");
				break;
			case 'À':
				sb.append("&Agrave;");
				break;
			case 'â':
				sb.append("&acirc;");
				break;
			case 'Â':
				sb.append("&Acirc;");
				break;
			case 'ä':
				sb.append("&auml;");
				break;
			case 'Ä':
				sb.append("&Auml;");
				break;
			case 'å':
				sb.append("&aring;");
				break;
			case 'Å':
				sb.append("&Aring;");
				break;
			case 'æ':
				sb.append("&aelig;");
				break;
			case 'Æ':
				sb.append("&AElig;");
				break;
			case 'ç':
				sb.append("&ccedil;");
				break;
			case 'Ç':
				sb.append("&Ccedil;");
				break;
			case 'é':
				sb.append("&eacute;");
				break;
			case 'É':
				sb.append("&Eacute;");
				break;
			case 'è':
				sb.append("&egrave;");
				break;
			case 'È':
				sb.append("&Egrave;");
				break;
			case 'ê':
				sb.append("&ecirc;");
				break;
			case 'Ê':
				sb.append("&Ecirc;");
				break;
			case 'ë':
				sb.append("&euml;");
				break;
			case 'Ë':
				sb.append("&Euml;");
				break;
			case 'ï':
				sb.append("&iuml;");
				break;
			case 'Ï':
				sb.append("&Iuml;");
				break;
			case 'ô':
				sb.append("&ocirc;");
				break;
			case 'Ô':
				sb.append("&Ocirc;");
				break;
			case 'ö':
				sb.append("&ouml;");
				break;
			case 'Ö':
				sb.append("&Ouml;");
				break;
			case 'ø':
				sb.append("&oslash;");
				break;
			case 'Ø':
				sb.append("&Oslash;");
				break;
			case 'ß':
				sb.append("&szlig;");
				break;
			case 'ù':
				sb.append("&ugrave;");
				break;
			case 'Ù':
				sb.append("&Ugrave;");
				break;
			case 'û':
				sb.append("&ucirc;");
				break;
			case 'Û':
				sb.append("&Ucirc;");
				break;
			case 'ü':
				sb.append("&uuml;");
				break;
			case 'Ü':
				sb.append("&Uuml;");
				break;
			case '®':
				sb.append("&reg;");
				break;
			case '©':
				sb.append("&copy;");
				break;
			case '€':
				sb.append("&euro;");
				break;
			// be carefull with this one (non-breaking whitee space)
//			case ' ':
//				sb.append("&nbsp;");
//				break;

			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}
}
