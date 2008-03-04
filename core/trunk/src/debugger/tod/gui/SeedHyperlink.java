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

import java.awt.Color;

import tod.gui.seed.Seed;
import zz.utils.ui.ZHyperlink;
import zz.utils.ui.text.XFont;

public class SeedHyperlink extends ZHyperlink
{
	private Seed itsSeed;
	
	public SeedHyperlink(String aText, XFont aFont, Color aColor, Seed aSeed)
	{
		super(aText, aFont, aColor);
		itsSeed = aSeed;
	}

	public void setSeed(Seed aSeed)
	{
		itsSeed = aSeed;
	}
	
	@Override
	protected void traverse()
	{
		itsSeed.open();
	}
	
	/**
	 * Creates a new flow text with default size computer.
	 */
	public static SeedHyperlink create(
			Seed aSeed, 
			String aText, 
			XFont aFont, 
			Color aColor)
	{
		XFont theFont = aFont.isUnderline() ?
				new XFont(aFont.getAWTFont(), false) 
				: aFont;
		
		SeedHyperlink theHyperlink = new SeedHyperlink(aText, theFont, aColor, aSeed);
		
		return theHyperlink;
	}
	
	/**
	 * Creates a new flow text with default size computer and font.
	 */
	public static SeedHyperlink create(Seed aSeed, String aText, Color aColor)
	{
		return create(aSeed, aText, XFont.DEFAULT_XUNDERLINED, aColor);
	}

	/**
	 * Creates a new flow text with default size computer and default font
	 * of the given size.
	 */
	public static SeedHyperlink create(Seed aSeed, String aText, float aFontSize, Color aColor)
	{
		return create(aSeed, aText, XFont.DEFAULT_XUNDERLINED.deriveFont(aFontSize), aColor);
	}

}
