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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JTextPane;

public class HtmlComponent extends JTextPane
{
	private HtmlDoc itsDoc;

	public HtmlComponent()
	{
		setEditable(false);
	}
	
	public void setDoc(HtmlDoc aDoc)
	{
		if (itsDoc != null) 
		{
			itsDoc.setComponent(null);
			removeHyperlinkListener(itsDoc);
		}
		
		itsDoc = aDoc;
		
		if (itsDoc != null)
		{
			itsDoc.setComponent(this);
			setEditorKit(itsDoc.getEditorKit());
			setDocument(itsDoc.createDocument());
			addHyperlinkListener(itsDoc);
		}
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paintComponent(g2);
	}

}
