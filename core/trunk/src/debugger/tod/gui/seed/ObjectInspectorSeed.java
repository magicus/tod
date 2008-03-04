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
package tod.gui.seed;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;
import tod.gui.view.ObjectInspectorView;
import zz.utils.properties.HashSetProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.ISetProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * Seed for the {@link tod.gui.view.ObjectInspectorView}
 * @author gpothier
 */
public class ObjectInspectorSeed extends LogViewSeed
{
	private ObjectId itsInspectedObject;
	
	private IRWProperty<Long> pSelectionStart = new SimpleRWProperty<Long>(this);
	private IRWProperty<Long> pSelectionEnd = new SimpleRWProperty<Long>(this);
	private IRWProperty<Long> pCurrentPosition = new SimpleRWProperty<Long>(this);
	
	private ISetProperty<IMemberInfo> pSelectedMembers = new HashSetProperty<IMemberInfo>(this);


	
	public ObjectInspectorSeed(IGUIManager aGUIManager, ILogBrowser aLog, ObjectId aInspectedObject)
	{
		super(aGUIManager, aLog);
		itsInspectedObject = aInspectedObject;
		
		pSelectionStart().set(getLogBrowser().getFirstTimestamp());
		pSelectionEnd().set(getLogBrowser().getLastTimestamp());
	}
	
	protected LogView requestComponent()
	{
		ObjectInspectorView theView = new ObjectInspectorView (getGUIManager(), getLogBrowser(), this);
		theView.init();
		return theView;
	}
	
	public ObjectId getInspectedObject()
	{
		return itsInspectedObject;
	}

	public IRWProperty<Long> pCurrentPosition()
	{
		return pCurrentPosition;
	}

	public IRWProperty<Long> pSelectionEnd()
	{
		return pSelectionEnd;
	}

	public IRWProperty<Long> pSelectionStart()
	{
		return pSelectionStart;
	}

	public ISetProperty<IMemberInfo> pSelectedMembers()
	{
		return pSelectedMembers;
	}
	
	
}
