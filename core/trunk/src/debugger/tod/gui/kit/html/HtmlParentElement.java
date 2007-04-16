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

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract html element that can contain children elements.
 * @author gpothier
 */
public abstract class HtmlParentElement extends HtmlElement
{
	private List<HtmlElement> itsChildren = null;

	@Override
	public void setDoc(HtmlDoc aDoc)
	{
		super.setDoc(aDoc);
		if (itsChildren != null) for (HtmlElement theElement : itsChildren)
		{
			theElement.setDoc(aDoc);
		}
	}
	
	public void add(HtmlElement aElement)
	{
		if (itsChildren == null) itsChildren = new ArrayList<HtmlElement>();
		itsChildren.add(aElement);
		if (getDoc() != null)
		{
			aElement.setDoc(getDoc());
			update();
		}
	}
	
	public void remove(HtmlElement aElement)
	{
		itsChildren.remove(aElement);
		aElement.setDoc(null);
		update();
	}
	
	public void clear()
	{
		if (itsChildren != null) 
		{
			for (HtmlElement theElement : itsChildren) theElement.setDoc(null);
			itsChildren.clear();
			update();
		}
	}
	
	public void addText(String aText)
	{
		add(new HtmlText(aText));
	}
	
	public void addBr()
	{
		add(new HtmlRaw("<br>"));
	}
	
	@Override
	public void render(StringBuilder aBuilder)
	{
		aBuilder.append('<');
		aBuilder.append(getTag());
		aBuilder.append(" id='");
		aBuilder.append(getId());
		aBuilder.append("' ");
		renderAttributes(aBuilder);
		aBuilder.append('>');
		renderChildren(aBuilder);
		aBuilder.append("</");
		aBuilder.append(getTag());
		aBuilder.append('>');
	}
	
	protected abstract String getTag();
	protected abstract void renderAttributes(StringBuilder aBuilder);
	
	
	protected void renderChildren(StringBuilder aBuilder)
	{
		if (itsChildren != null) for (HtmlElement theElement : itsChildren)
		{
			theElement.render(aBuilder);
		}
	}
}
