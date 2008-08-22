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
package tod.gui.kit.html;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * An abstract html element that can contain children elements.
 * @author gpothier
 */
public abstract class HtmlParentElement extends HtmlElement
{
	private List<HtmlElement> itsChildren = null;

	@Override
	public synchronized void setDoc(HtmlDoc aDoc)
	{
		super.setDoc(aDoc);
		if (itsChildren != null) for (HtmlElement theElement : itsChildren)
		{
			theElement.setDoc(aDoc);
		}
	}
	
	public void add(HtmlElement aElement)
	{
		assert aElement != null;
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
		add(HtmlText.create(aText));
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
		// We don't use an iterator here to avoid concurrency issues.
		if (itsChildren != null) for (int i=0;i<itsChildren.size();i++)
		{
			HtmlElement theElement = itsChildren.get(i);
			theElement.render(aBuilder);
		}
	}
}
