import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

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

public class TextTest
{
	public static void main(String[] args) throws IOException, BadLocationException
	{
		JFrame theFrame = new JFrame("Text test");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		String theText = 
			"<html>" +
				"<head>" +
					"<style type='text/css'>" +
						"body {color: purple;}" +
					"</style>" +
				"</head>" +
				"<body style='font-size: 14pt'> " +
					"Toto, let's <span style='font-size: 150%' id='pop1'>start</span> a long text run so that " +
					"we can see if hyperlinks are broken <a href='http://bob'>titi, this is the hyperlink I'm talkin about poipoipoi.popopopoy</a> Tata. over, Roger, Popo<p>Popo" +
					"<div>d1</div><div>d2</div>" +
					"<span id='s1' style='font-size: 150%;'>toto<span id='s2' style='font-size: 150%;'>titi</span></span>" +
				"</body>" +
			"</html>";
		JEditorPane thePane = new AAEditorPane();
		thePane.setEditable(false);
		
		HTMLEditorKit theEditorKit = new HTMLEditorKit();
		HTMLDocument theDocument = (HTMLDocument) theEditorKit.createDefaultDocument();
		
		StyleSheet theStyleSheet = new StyleSheet();
//		theStyleSheet.setBaseFontSize(1);
		theStyleSheet.addRule("body { font-size: 26pt;  background-color: #d8da3d }");
		theEditorKit.setStyleSheet(theStyleSheet);
		
		theEditorKit.read(new StringReader(theText), theDocument, 0);
		
		thePane.setEditorKit(theEditorKit);
		thePane.setDocument(theDocument);
		
		Element theElement = theDocument.getElement("pop1");
		System.out.println(theElement);
		theDocument.setOuterHTML(theElement, "<span style='font-size: 50%' id='pop1'>start (small)</span>");
				
		thePane.addHyperlinkListener(new MyHyperlinksListener());
		
		System.out.println(thePane.getText());
		
		theFrame.setContentPane(new JScrollPane(thePane));
		
		theFrame.pack();
		theFrame.setVisible(true);
	}
	
	private static class MyHyperlinksListener implements HyperlinkListener
	{
		public void hyperlinkUpdate(HyperlinkEvent e)
		{
			System.out.println(String.format(
					"desc: %s\ntype: %s\nsel: %s\nurl: %s",
					e.getDescription(),
					e.getEventType(),
					e.getSourceElement(),
					e.getURL()));
		}
	}
	
	private static class AAEditorPane extends JTextPane
	{
		public AAEditorPane()
		{
		}


		@Override
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			super.paintComponent(g2);
		}
	}
}
