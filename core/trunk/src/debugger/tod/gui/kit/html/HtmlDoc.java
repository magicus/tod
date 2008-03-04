/*
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
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import tod.gui.FontConfig;

public class HtmlDoc implements HyperlinkListener
{
	private HtmlComponent itsComponent;
	
	private HTMLEditorKit itsEditorKit;
	private HTMLDocument itsDocument;
	private HtmlBody itsRoot;
	private int itsCurrentId;
	
	private boolean itsUpdatePosted = false;
	private boolean itsUpToDate = true;
	
	private Map<String, IHyperlinkListener> itsHyperlinkListeners =
		new HashMap<String, IHyperlinkListener>();
	
	public HtmlDoc()
	{
		this(new HtmlBody());
	}
	
	public HtmlDoc(HtmlBody aRoot)
	{
		itsEditorKit = new HTMLEditorKit();
		itsRoot = aRoot;
		itsRoot.setDoc(this);
	}
	
	public HtmlComponent getComponent()
	{
		return itsComponent;
	}

	public void setComponent(HtmlComponent aComponent)
	{
		itsComponent = aComponent;
	}
	
	public HtmlBody getRoot()
	{
		return itsRoot;
	}

	public HTMLEditorKit getEditorKit()
	{
		return itsEditorKit;
	}

	public HTMLDocument createDocument()
	{
		try
		{
			itsDocument = (HTMLDocument) itsEditorKit.createDefaultDocument();
			StringBuilder theBuilder = new StringBuilder("<html>");
			if (itsRoot != null) itsRoot.render(theBuilder);
			theBuilder.append("</html>");
			itsEditorKit.read(new StringReader(theBuilder.toString()), itsDocument, 0);

			return itsDocument;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (BadLocationException e)
		{
			throw new RuntimeException(e);
		}
	}

	public String createId()
	{
		return ""+(itsCurrentId++);
	}
	
	public void registerListener(String aId, IHyperlinkListener aListener)
	{
		itsHyperlinkListeners.put(aId, aListener);
	}
	
	public void unregisterListener(String aId)
	{
		itsHyperlinkListeners.remove(aId);
	}
	
	public synchronized void update(HtmlElement aElement)
	{
		if (! itsUpdatePosted)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					synchronized (HtmlDoc.this)
					{
						updateHtml();
						itsUpdatePosted = false;
					}
				}
			});
			itsUpdatePosted = true;
		}
	}
	
	public void updateHtml()
	{
		if (itsDocument == null) return;
		
		try
		{
			Element theElement = itsDocument.getElement(itsRoot.getId());
			
			StringBuilder theText = new StringBuilder();
			itsRoot.render(theText);

			itsDocument.setOuterHTML(theElement, theText.toString());
		}
		catch (BadLocationException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void hyperlinkUpdate(HyperlinkEvent aEvent)
	{
		if (aEvent.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
		
		String theId = aEvent.getDescription();
		IHyperlinkListener theListener = itsHyperlinkListeners.get(theId);
		if (theListener != null) theListener.traverse();
	}
	
	/**
	 * Creates a simple document whose content is the given string.
	 */
	public static HtmlDoc create(String aText)
	{
		return create(aText, FontConfig.NORMAL, Color.BLACK);
	}
	
	/**
	 * Creates a simple document whose content is the given string, 
	 * with the specified font size and color.
	 */
	public static HtmlDoc create(String aText, int aFontSize, Color aColor)
	{
		HtmlDoc theDoc = new HtmlDoc();
		HtmlBody theBody = theDoc.getRoot();
		
		theBody.add(HtmlText.create(aText, aFontSize, aColor));
		
		return theDoc;
	}
}
