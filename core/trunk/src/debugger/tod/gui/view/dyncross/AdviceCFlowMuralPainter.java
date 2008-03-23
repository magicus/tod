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
package tod.gui.view.dyncross;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.BrowserData;
import tod.gui.eventsequences.mural.AbstractMuralPainter;
import tod.gui.seed.DynamicCrosscuttingSeed.Highlight;
import zz.utils.properties.IListProperty;
import zz.utils.ui.UIUtils;

public class AdviceCFlowMuralPainter extends AbstractMuralPainter
{
	private IListProperty<Highlight> itsHighlightsProperty;
	
	public AdviceCFlowMuralPainter(IListProperty<Highlight> aHighlightsProperty)
	{
		itsHighlightsProperty = aHighlightsProperty;
	}

	@Override
	public long[][] paintMural(
			Graphics2D aGraphics, 
			Rectangle aBounds, 
			long t1, 
			long t2,
			List<BrowserData> aBrowserDatas)
	{
		long[][] theValues = getValues(aBounds, t1, t2, aBrowserDatas);
		int theCount = theValues.length;
		if (theCount == 0) return theValues;
		
		int theTotalMarkSize = 0;
		int[] theMarkYs = new int[theCount];
		int i = 0;
		for (BrowserData theBrowserData : aBrowserDatas) 
		{
			theMarkYs[i++] = theTotalMarkSize;
			theTotalMarkSize += theBrowserData.markSize;
		}
		
		int theHeight = aBounds.height-theTotalMarkSize;
		int theY = aBounds.y;
		
		long theMaxT = 4; // We want to be able to see when a bar corresponds to only one event.
		
		// Determine maximum value
		for (i = 0; i < theValues[0].length; i++)
		{
			long theTotal = 0;
			for (int j = 0; j < theValues.length; j++) theTotal += theValues[j][i]; 
			theMaxT = Math.max(theMaxT, theTotal);
		}
		
		float theBarWidth = 1f*aBounds.width/theValues[0].length;
		
		aGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Draw aspect marks
		for (int k=0;k<theCount;k++)
		{
			boolean theSkip0 = false;
			for (int x = 0; x < theValues[0].length; x++)
			{
				float theX = aBounds.x + (x*theBarWidth);
				long v = theValues[k][x];
				BrowserData theBrowserData = aBrowserDatas.get(k);
				
				if (v > 0)
				{
					// There is something here so we draw the mark anyway
					aGraphics.setColor(theBrowserData.color);
					
					aGraphics.fill(makeTrapezoid(
							theX+(theBarWidth/2), 
							theHeight+theMarkYs[k], 
							k == 0 ? theBarWidth+4 : theBrowserData.markSize, 
							k== 0 ? theBarWidth : 0,
							theBrowserData.markSize));
					
					theSkip0 = false;
				}
				else if (k > 0)
				{
					if (theSkip0) continue;
					theSkip0 = true;
					
					// Nothing at this location, check if previous event expands in time.
					
					// Find out timestamp of current location
					long w = t2-t1;
					long t = t1+(long)(1f*w*theX/aBounds.width);

					IEventBrowser theBrowser = theBrowserData.browser.clone();
					theBrowser.setNextTimestamp(t);
					if (theBrowser.hasPrevious())
					{
						ILogEvent thePrevious = theBrowser.previous();
						theBrowser.next();
						ILogEvent theNext = theBrowser.hasNext() ? theBrowser.next() : null;
						
						// Check if both events have the same advice cflow
						if (theNext != null && Arrays.equals(thePrevious.getAdviceCFlow(), theNext.getAdviceCFlow()))
						{
							long dt = theNext.getTimestamp()-t1;
							int theNextX = (int) (1f*dt*aBounds.width/w);
							aGraphics.setColor(theBrowserData.color);
							aGraphics.fill(new Rectangle2D.Float(
									theX, 
									theHeight+theMarkYs[k]+1, 
									theNextX-theX, 
									theBrowserData.markSize-1));
						}
					}
				}
				
				
			}
		}
		
		for (int x = 0; x < theValues[0].length; x++)
		{
			float theX = aBounds.x + (x*theBarWidth);
			
			int theCurrentMarkY = theHeight;
			float theCurrentBarHeight = 0;
			
			// Draw marks and compute total bar height
			for (int k=0;k<theCount;k++)
			{
				long t = theValues[k][x];
				BrowserData theBrowserData = aBrowserDatas.get(k);
				
				if (t>0)
				{
					aGraphics.setColor(theBrowserData.color);
					
//					aGraphics.fill(makeTrapezoid(
//							theX+(theBarWidth/2), 
//							theY+theCurrentMarkY, 
//							k == 0 ? theBarWidth+4 : theBrowserData.markSize, 
//							k== 0 ? theBarWidth : 0,
//							theBrowserData.markSize));
					
					float h = (theHeight * t) / theMaxT;
					theCurrentBarHeight += h;
				}
				
				theCurrentMarkY += theBrowserData.markSize;
			}
			
			// Draw proportional bars
			for (int j=theCount-1;j>=0;j--)
			{
				long t = theValues[j][x];
				
				if (t>0)
				{
					BrowserData theBrowserData = aBrowserDatas.get(j);

					float h = (theHeight * t) / theMaxT;
					aGraphics.setColor(UIUtils.getLighterColor(theBrowserData.color, 0.7f));
					
					aGraphics.fill(makeTriangle(
							theX+(theBarWidth/2), 
							theHeight-theCurrentBarHeight, 
							theBarWidth, 
							theCurrentBarHeight));
					
					theCurrentBarHeight -= h;
				}
			}
		}
		
		return theValues;
	}
}
