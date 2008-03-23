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

import tod.core.database.browser.IEventBrowser;

/**
 * Data agregate for browsers that are used in an {@link tod.gui.eventsequences.mural.EventMural}
 * or a {@link tod.gui.TimeScale}. Apart from an {@link tod.core.database.browser.IEventBrowser}
 * it contains a color that indicates how the events of the broswser should be rendered,
 * and the size of an optional marker displayed below columns that contain events.
 * @author gpothier
 */
public class BrowserData
{
	public static final int DEFAULT_MARK_SIZE = 5;
	public static final Color DEFAULT_COLOR = Color.BLACK;
	
	public final IEventBrowser browser;
	public final Color color;
	public final int markSize;
	
	public BrowserData(IEventBrowser aBrowser)
	{
		this(aBrowser, DEFAULT_COLOR, DEFAULT_MARK_SIZE);
	}
	
	public BrowserData(IEventBrowser aBrowser, Color aColor)
	{
		this(aBrowser, aColor, DEFAULT_MARK_SIZE);
	}
	
	public BrowserData(IEventBrowser aBrowser, Color aColor, int aMarkSize)
	{
		browser = aBrowser;
		color = aColor;
		markSize = aMarkSize;
	}
}
