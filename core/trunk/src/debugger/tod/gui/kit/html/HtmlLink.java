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

public abstract class HtmlLink extends HtmlElement implements IHyperlinkListener
{
	private String itsText;
	
	public HtmlLink(String aText)
	{
		itsText = aText;
	}
	
	@Override
	public void setDoc(HtmlDoc aDoc)
	{
		if (getDoc() != null) getDoc().unregisterListener(getId());
		super.setDoc(aDoc);
		if (getDoc() != null) getDoc().registerListener(getId(), this);
	}
	
	@Override
	public void render(StringBuilder aBuilder)
	{
		aBuilder.append("<a href='");
		aBuilder.append(getId());
		aBuilder.append("' id='");
		aBuilder.append(getId());
		aBuilder.append("'>");
		aBuilder.append(itsText);
		aBuilder.append("</a>");
	}
}