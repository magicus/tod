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
package tod.gui.eventsequences;

import java.awt.Color;

import javax.swing.JComponent;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.structure.IMemberInfo;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;

/**
 * Abstract base class for event sequence views that displays events relative to a class member.
 * @author gpothier
 */
public abstract class AbstractMemberSequenceView extends AbstractSingleBrowserSequenceView
{
	private final IObjectInspector itsInspector;
	
	public AbstractMemberSequenceView(IGUIManager aGUIManager, Color aColor, IObjectInspector aInspector)
	{
		super(aGUIManager, aColor);
		itsInspector = aInspector;
	}

	@Override
	protected IEventBrowser getBrowser()
	{
		throw new UnsupportedOperationException("Reeimplement if needed");
//		return itsInspector.getBrowser(getMember());
	}

	/**
	 * Returns the member whose events are displayed in this sequence.
	 */
	public abstract IMemberInfo getMember();
	
	/**
	 * Helper method that creates a graphic object suitable for 
	 * representing the given object.
	 */
	protected JComponent createBaloon(Object aObject)
	{
		return Hyperlinks.object(
				getGUIManager(), 
				Hyperlinks.SWING, 
				getGUIManager().getJobProcessor(),
				itsInspector.getObject(), 
				aObject, 
				null,
				true);
	}
}
