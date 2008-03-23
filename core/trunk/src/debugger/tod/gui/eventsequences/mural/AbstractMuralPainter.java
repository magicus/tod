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
package tod.gui.eventsequences.mural;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.List;

import tod.gui.BrowserData;
import tod.utils.TODUtils;

public abstract class AbstractMuralPainter
{
	/**
	 * The width, in pixels, of each drawn bar.
	 */
	private static final int BAR_WIDTH = 3;
	

	public abstract long[][] paintMural(
			Graphics2D aGraphics, 
			Rectangle aBounds, 
			long aT1, 
			long aT2, 
			List<BrowserData> aBrowserDatas);

	protected static Shape makeTriangle(float aX, float aY, float aBaseW, float aHeight)
	{
		GeneralPath thePath = new GeneralPath();
		thePath.moveTo(aX, aY);
		thePath.lineTo(aX+(aBaseW/2), aY+aHeight);
		thePath.lineTo(aX-(aBaseW/2), aY+aHeight);
		thePath.closePath();
		
		return thePath;
	}
	
	protected static Shape makeTrapezoid(float aX, float aY, float aBaseW, float aTopW, float aHeight)
	{
		GeneralPath thePath = new GeneralPath();
		thePath.moveTo(aX-(aTopW/2), aY);
		thePath.lineTo(aX+(aTopW/2), aY);
		thePath.lineTo(aX+(aBaseW/2), aY+aHeight);
		thePath.lineTo(aX-(aBaseW/2), aY+aHeight);
		thePath.closePath();
		
		return thePath;
	}
	
	protected long[][] getValues(Rectangle aBounds, long aT1, long aT2, List<BrowserData> aBrowserData)
	{
		if (aT1 == aT2) return null;
		long[][] theValues = new long[aBrowserData.size()][];
		
		int theSamplesCount = aBounds.width / BAR_WIDTH;
		
		int i = 0;
		for (BrowserData theBrowserData : aBrowserData)
		{
			// TODO: check conversion
			TODUtils.log(2, "[EventMural] Requesting counts: "+aT1+"-"+aT2);
			theValues[i] = theBrowserData.browser.getEventCounts(aT1, aT2, theSamplesCount, false);
			i++;
		}
		
		return theValues;
	}


}
