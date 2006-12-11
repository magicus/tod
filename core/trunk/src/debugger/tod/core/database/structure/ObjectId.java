/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.core.database.structure;

import java.io.Serializable;

/**
 * Permits to identify an object.
 * There are two identification schemes:
 * <li>Instances of classes that were elegible to the 
 * {@link reflex.lib.logging.core.impl.mop.identification.ObjectIdentifier} 
 * mechanism have a truly unique identifier; they are represented
 * by the {@link ObjectUID} inner class.
 * <li>Instances of other classes are represented by a 
 * {@link ObjectHash}, which provides only a hint to a object's identity,
 * as several objects can have the same hash code.
 * @author gpothier
 */
public interface ObjectId extends Serializable
{
	
	public static class ObjectUID implements ObjectId
	{
		private long itsId;
		
		
		public ObjectUID(long aId)
		{
			itsId = aId;
		}
		
		public long getId()
		{
			return itsId;
		}
		
		
		@Override
		public int hashCode()
		{
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + (int) (itsId ^ (itsId >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			final ObjectUID other = (ObjectUID) obj;
			if (itsId != other.itsId) return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "UID: "+itsId;
		}
	}
	
	public static class ObjectHash implements ObjectId
	{
		private int itsHascode;
		
		public ObjectHash(int aHascode)
		{
			itsHascode = aHascode;
		}
		
		public int getHascode()
		{
			return itsHascode;
		}
		
		@Override
		public int hashCode()
		{
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + itsHascode;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			final ObjectHash other = (ObjectHash) obj;
			if (itsHascode != other.itsHascode) return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "Hash: "+itsHascode;
		}
	}
}
