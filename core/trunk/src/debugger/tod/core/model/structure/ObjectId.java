/*
 * Created on Oct 18, 2004
 */
package tod.core.model.structure;

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
public interface ObjectId 
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
		
		public boolean equals(Object aObj)
		{
			if (aObj instanceof ObjectUID)
			{
				ObjectUID theOther = (ObjectUID) aObj;
				return itsId == theOther.itsId;
			}
			else return false;
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
		
		public boolean equals(Object aObj)
		{
			if (aObj instanceof ObjectHash)
			{
				ObjectHash theOther = (ObjectHash) aObj;
				return itsHascode == theOther.itsHascode;
			}
			else return false;
		}
		
		@Override
		public String toString()
		{
			return "Hash: "+itsHascode;
		}
	}
}
