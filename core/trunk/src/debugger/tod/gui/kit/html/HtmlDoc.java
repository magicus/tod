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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

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
}