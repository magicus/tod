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
package tod.impl.local.filter;

import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.local.LocalBrowser;

/**
 * Instantiation-related filter.
 * @author gpothier
 */
public class InstantiationFilter extends AbstractStatelessFilter
{
	private ITypeInfo itsTypeInfo;
	private ObjectId itsObject;
	
	/**
	 * Creates a filter that accepts any instantiation event.
	 */
	public InstantiationFilter(LocalBrowser aBrowser)
	{
		super (aBrowser);
	}

	/**
	 * Creates a filer that accepts only the instantiation events
	 * for a specific type.
	 */
	public InstantiationFilter(LocalBrowser aBrowser, ITypeInfo aTypeInfo)
	{
		super (aBrowser);
		itsTypeInfo = aTypeInfo;
	}
	
	/**
	 * Creates a filer that accepts only the instantiation events
	 * for a specific object.
	 */
	public InstantiationFilter(LocalBrowser aBrowser, ObjectId aObject)
	{
		super (aBrowser);
		itsObject = aObject;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			
			if (itsTypeInfo != null && theEvent.getType() != itsTypeInfo) return false;
			if (itsObject != null && ! itsObject.equals(theEvent.getInstance())) return false;
			return true;
		}
		else return false;
	}

}
