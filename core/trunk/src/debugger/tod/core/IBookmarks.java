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
package tod.core;

import java.awt.Color;
import java.awt.Shape;
import java.util.Comparator;

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ObjectId;
import zz.utils.notification.IEvent;

/**
 * The model for bookmarks.
 * Events and objects can be bookmarked. A name, color or symbol can be
 * associated with each bookmarked item.
 * @author gpothier
 */
public interface IBookmarks
{
	/**
	 * Returns all registered bookmarks.
	 */
	public Iterable<Bookmark> getBookmarks();
	
	public void addBookmark(Bookmark aBookmark);
	public void removeBookmark(Bookmark aBookmark);
	
	/**
	 * This event is fired whenever bookmarks are added or removed.
	 */
	public IEvent<Void> eChanged();
	
	
	public static abstract class Bookmark<T>
	{
		public final String name;
		public final Color color;
		public final Shape shape;
		
		public Bookmark(Color aColor, String aName, Shape aShape)
		{
			color = aColor;
			name = aName;
			shape = aShape;
		}
		
		/**
		 * Returns the bookmarked item
		 */
		public abstract T getItem();
	}
	
	public static class EventBookmark extends Bookmark<ILogEvent>
	{
		private ILogEvent itsEvent;
		
		/**
		 * Whether the control flow of the event should be marked
		 * (only meaningful for behavior call events)
		 */
		private boolean itsMarkControlFlow;

		public EventBookmark(Color aColor, String aName, Shape aShape, ILogEvent aEvent, boolean aMarkControlFlow)
		{
			super(aColor, aName, aShape);
			itsEvent = aEvent;
			itsMarkControlFlow = aMarkControlFlow;
		}
		
		@Override
		public ILogEvent getItem()
		{
			return itsEvent;
		}
		
		public boolean getMarkControlFlow()
		{
			return itsMarkControlFlow;
		}
	}
	
	public static class ObjectBookmark extends Bookmark<ObjectId>
	{
		private ObjectId itsObject;

		public ObjectBookmark(Color aColor, String aName, Shape aShape, ObjectId aObject)
		{
			super(aColor, aName, aShape);
			itsObject = aObject;
		}
		
		@Override
		public ObjectId getItem()
		{
			return itsObject;
		}
	}
	
	/**
	 * A comparator for event bookmarks.
	 */
	public static final Comparator<EventBookmark> EVENT_COMPARATOR = new Comparator<EventBookmark>()
	{
		public int compare(EventBookmark aO1, EventBookmark aO2)
		{
			long dt = aO1.getItem().getTimestamp() - aO2.getItem().getTimestamp();
			if (dt < 0) return -1;
			else if (dt == 0) return 0;
			else return 1;
		}
	};
}
