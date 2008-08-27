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
package tod.gui.settings;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import tod.gui.components.eventlist.IntimacyLevel;
import zz.utils.FireableTreeModel;
import zz.utils.notification.IEvent;
import zz.utils.notification.IFireableEvent;
import zz.utils.notification.SimpleEvent;

/**
 * Holds the intimacy/obliviousness settings for each aspect/advice.
 * @author gpothier
 */
public class IntimacySettings implements Serializable
{
	private Map<Integer, IntimacyLevel> itsIntimacyMap = new HashMap<Integer, IntimacyLevel>();
	
	/**
	 * This event is fired whenever the intimacy settings change.
	 */
	public final IEvent<Void> eChanged = new SimpleEvent<Void>();
	
	public IntimacyLevel getIntimacyLevel(int aAdviceSourceId)
	{
		return itsIntimacyMap.get(aAdviceSourceId);
	}
	
	public void setIntimacyLevel(int aAdviceSourceId, IntimacyLevel aLevel)
	{
		itsIntimacyMap.put(aAdviceSourceId, aLevel);
		((IFireableEvent<Void>) eChanged).fire(null);
	}
	
	public void clear()
	{
		itsIntimacyMap.clear();
		((IFireableEvent<Void>) eChanged).fire(null);		
	}
}
