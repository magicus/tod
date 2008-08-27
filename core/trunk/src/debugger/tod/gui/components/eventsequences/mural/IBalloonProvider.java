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
package tod.gui.components.eventsequences.mural;

import java.util.Comparator;
import java.util.List;

import tod.core.database.event.ILogEvent;

import zz.utils.notification.IEvent;

/**
 * Provides ballons for murals
 * @author gpothier
 */
public interface IBalloonProvider
{
	/**
	 * Gets the balloons to show in the given interval.
	 */
	public List<Balloon> getBaloons(long aStartTimestamp, long aEndTimestamp);
	
	/**
	 * Returns an event that is fired when the set of balloons changes.
	 */
	public IEvent<Void> eChanged();
	
	public static class Balloon
	{
		private ILogEvent itsEvent;
		
		/**
		 * HTML text of the balloon
		 */
		private final String itsText;

		public Balloon(ILogEvent aEvent, String aText)
		{
			itsEvent = aEvent;
			itsText = aText;
		}

		public ILogEvent getEvent()
		{
			return itsEvent;
		}

		public long getTimestamp()
		{
			return getEvent().getTimestamp();
		}
		
		public String getText()
		{
			return itsText;
		}
		
		

	}

	/**
	 * Compares the timestamp of balloons.
	 */
	public static Comparator<Balloon> COMPARATOR = new Comparator<Balloon>()
	{
		public int compare(Balloon aO1, Balloon aO2)
		{
			long dt = aO1.getTimestamp() - aO2.getTimestamp();
			
			if (dt < 0) return -1;
			else if (dt == 0) return 0;
			else return 1;
		}
	};
}
