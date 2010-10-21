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
package tod.impl.server;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIterator;
import gnu.trove.TLongArrayList;
import gnu.trove.TLongHashSet;
import gnu.trove.TLongIterator;
import tod.core.database.structure.ObjectId;
import tod.impl.replay2.EventCollector;
import zz.utils.Utils;

public class ObjectAccessDistributionEventCollector extends CounterEventCollector
{
	private final int itsThreadId;
	private TLongHashSet itsObjectsSet = new TLongHashSet();
	
	private long itsTotalUniqueObjects = 0;
	private long itsTotalAccesses = 0;
	private int itsBlocks = 0;
	private int itsSyncs = 0;

	public ObjectAccessDistributionEventCollector(int aThreadId)
	{
		itsThreadId = aThreadId;
	}

	@Override
	public void fieldRead(ObjectId aTarget, int aFieldSlotIndex)
	{
		super.fieldRead(aTarget, aFieldSlotIndex);
//		fieldAccess(aTarget, aFieldId);
	}

	@Override
	public void fieldWrite(ObjectId aTarget, int aFieldSlotIndex)
	{
		super.fieldWrite(aTarget, aFieldSlotIndex);
		fieldAccess(aTarget, aFieldSlotIndex);
	}
	
	protected void fieldAccess(ObjectId aTarget, int aFieldSlotIndex)
	{
		long id = aTarget != null ? aTarget.getId() : -1;
		itsObjectsSet.add(id);
		itsTotalAccesses++;
	}


	@Override
	public void sync(long aTimestamp)
	{
		super.sync(aTimestamp);
		itsSyncs++;
		if (itsSyncs % 50 == 0)
		{
			itsTotalUniqueObjects += itsObjectsSet.size();
			itsObjectsSet.clear();
			itsBlocks++;
			
			if (itsBlocks % 10 == 0) 
			{
				float avgUniqueObjects = 1f*itsTotalUniqueObjects/itsBlocks;
				float avgAccesses = 1f*itsTotalAccesses/itsBlocks;
				float avgEvents = 1f*getFieldAccessCount()/itsBlocks;
				Utils.println(
						"[TOD] Access distribution: uobj: %.2f, acc: %.2f, ev: %.2f - u/a: %.2f%%", 
						avgUniqueObjects,
						avgAccesses,
						avgEvents,
						100f*avgUniqueObjects/avgAccesses);
			}
		}
	}
}
