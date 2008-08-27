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
package tod.gui.components.eventsequences.mural;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import tod.gui.BrowserData;
import zz.utils.ui.UIUtils;

public class AvgMuralPainter extends AbstractMuralPainter
{
	private static AvgMuralPainter INSTANCE = new AvgMuralPainter();

	public static AvgMuralPainter getInstance()
	{
		return INSTANCE;
	}

	private AvgMuralPainter()
	{
	}
	
	/**
	 * Paints the mural, summing the values of all series and averaging the colors
	 */
	@Override
	public long[][] paintMural(
			EventMural aMural, 
			Graphics2D aGraphics, 
			Rectangle aBounds, 
			long aT1, 
			long aT2, List<BrowserData> aBrowserDatas)
	{
		long[][] theValues = getValues(aBounds, aT1, aT2, aBrowserDatas);
		if (theValues.length == 0) return theValues;
		
		int theHeight = aBounds.height;
		int theY = aBounds.y;
		int bh = 4; // base height

		long theMaxT = 0;
		
		// Determine maximum value
		for (int i = 0; i < theValues[0].length; i++)
		{
			long t = 0; 
			for (int j = 0; j < theValues.length; j++) t += theValues[j][i];
			theMaxT = Math.max(theMaxT, t);
		}
		
		for (int i = 0; i < theValues[0].length; i++)
		{
			int t = 0; // Total for current column
			int r = 0;
			int g = 0;
			int b = 0;
			
			for (int j = 0; j < theValues.length; j++)
			{
				long theValue = theValues[j][i];
				Color theColor = aBrowserDatas.get(j).color;
				
				t += theValue;
				r += theValue * theColor.getRed();
				g += theValue * theColor.getGreen();
				b += theValue * theColor.getBlue();
			}
			
			if (t == 0) continue;
			
			Color c1 = new Color(r/t, g/t, b/t);
			Color c2 = UIUtils.getLighterColor(c1);

			// Draw main bar
			aGraphics.setColor(c1);
			aGraphics.fillRect(aBounds.x + i, theY+theHeight-bh, 1, bh);
			
			// Draw proportional bar
			int h = (int) ((theHeight-bh) * t / theMaxT);
			aGraphics.setColor(c2);
			aGraphics.fillRect(aBounds.x + i, theY+theHeight-bh-h, 1, h);
		}
		
		return theValues;
	}
	

}
