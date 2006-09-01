/*
 * Created on Aug 29, 2006
 */
package tod.impl.dbgrid.messages;

import java.util.HashMap;
import java.util.Map;

import tod.core.database.structure.ObjectId;
import tod.core.database.structure.ObjectId.ObjectUID;
import zz.utils.bit.BitStruct;
import zz.utils.bit.BitUtils;

/**
 * Provides methods to read and write objects to {@link BitStruct}s.
 * @author gpothier
 */
public class ObjectCodec
{
	/**
	 * Enumerates the different kinds of objects that can be stored or read.
	 * @author gpothier
	 */
	private static enum ObjectType
	{
		NULL()
		{
			@Override
			public boolean matches(Object aObject)
			{
				return aObject == null;
			}
			
			@Override
			public int getObjectBits(Object aObject)
			{
				return 0;
			}

			@Override
			public Object readObject(BitStruct aStruct)
			{
				return null;
			}

			@Override
			public void writeObject(BitStruct aStruct, Object aObject)
			{
			}
		},
		UID()
		{
			@Override
			public boolean matches(Object aObject)
			{
				return aObject instanceof ObjectId.ObjectUID;
			}
			
			@Override
			public int getObjectBits(Object aObject)
			{
				return 64;
			}

			@Override
			public Object readObject(BitStruct aStruct)
			{
				long theUid = aStruct.readLong(64);
				assert theUid != 0;
				return new ObjectId.ObjectUID(theUid);
			}

			@Override
			public void writeObject(BitStruct aStruct, Object aObject)
			{
				ObjectId.ObjectUID theId = (ObjectUID) aObject;
				long theUid = theId.getId();
				assert theUid != 0;
				aStruct.writeLong(theUid, 64);
			}
		}, 
		LONG()
		{
			@Override
			public boolean matches(Object aObject)
			{
				return aObject instanceof Long;
			}
			
			@Override
			public int getObjectBits(Object aObject)
			{
				return 64;
			}

			@Override
			public Object readObject(BitStruct aStruct)
			{
				return aStruct.readLong(64);
			}

			@Override
			public void writeObject(BitStruct aStruct, Object aObject)
			{
				Long theLong = (Long) aObject;
				aStruct.writeLong(theLong, 64);
			}
		}, 
		INT()
		{
			@Override
			public boolean matches(Object aObject)
			{
				return aObject instanceof Integer;
			}
			
			@Override
			public int getObjectBits(Object aObject)
			{
				return 32;
			}

			@Override
			public Object readObject(BitStruct aStruct)
			{
				return aStruct.readInt(32);
			}

			@Override
			public void writeObject(BitStruct aStruct, Object aObject)
			{
				Integer theInteger = (Integer) aObject;
				aStruct.writeInt(theInteger, 32);
			}
		}, 
		CHAR()
		{
			@Override
			public boolean matches(Object aObject)
			{
				return aObject instanceof Character;
			}
			
			@Override
			public int getObjectBits(Object aObject)
			{
				return 16;
			}

			@Override
			public Object readObject(BitStruct aStruct)
			{
				int theValue = aStruct.readInt(16);
				return new Character((char) theValue);
			}

			@Override
			public void writeObject(BitStruct aStruct, Object aObject)
			{
				Character theCharacter = (Character) aObject;
				aStruct.writeInt(theCharacter.charValue(), 16);
			}
		}, 
		SHORT()
		{
			@Override
			public boolean matches(Object aObject)
			{
				return aObject instanceof Short;
			}
			
			@Override
			public int getObjectBits(Object aObject)
			{
				return 16;
			}

			@Override
			public Object readObject(BitStruct aStruct)
			{
				int theValue = aStruct.readInt(16);
				return new Short((short) theValue);
			}

			@Override
			public void writeObject(BitStruct aStruct, Object aObject)
			{
				Short theShort = (Short) aObject;
				aStruct.writeInt(theShort.shortValue(), 16);
			}
		}, 
		BYTE()
		{
			@Override
			public boolean matches(Object aObject)
			{
				return aObject instanceof Byte;
			}
			
			@Override
			public int getObjectBits(Object aObject)
			{
				return 8;
			}

			@Override
			public Object readObject(BitStruct aStruct)
			{
				return aStruct.readByte(8);
			}

			@Override
			public void writeObject(BitStruct aStruct, Object aObject)
			{
				Byte theByte = (Byte) aObject;
				aStruct.writeInt(theByte.byteValue(), 8);
			}
		}, 
		DOUBLE()
		{
			@Override
			public boolean matches(Object aObject)
			{
				return aObject instanceof Double;
			}
			
			@Override
			public int getObjectBits(Object aObject)
			{
				return 64;
			}

			@Override
			public Object readObject(BitStruct aStruct)
			{
				long theBits = aStruct.readLong(64);
				return Double.longBitsToDouble(theBits);
			}

			@Override
			public void writeObject(BitStruct aStruct, Object aObject)
			{
				Double theDouble = (Double) aObject;
				aStruct.writeLong(Double.doubleToRawLongBits(theDouble), 64);
			}
		}, 
		FLOAT()
		{
			@Override
			public boolean matches(Object aObject)
			{
				return aObject instanceof Float;
			}
			
			@Override
			public int getObjectBits(Object aObject)
			{
				return 32;
			}

			@Override
			public Object readObject(BitStruct aStruct)
			{
				int theBits = aStruct.readInt(32);
				return Float.intBitsToFloat(theBits);
			}

			@Override
			public void writeObject(BitStruct aStruct, Object aObject)
			{
				Float theFloat = (Float) aObject;
				aStruct.writeInt(Float.floatToRawIntBits(theFloat), 32);
			}
		};
		
		/**
		 * Indicates whether the given object can be handled by this enum value
		 */
		public abstract boolean matches(Object aObject);
		public abstract void writeObject(BitStruct aStruct, Object aObject);
		public abstract Object readObject(BitStruct aStruct);
		public abstract int getObjectBits(Object aObject);
	}
	
	/**
	 * Number of bits necessary to represent an object type.
	 */
	private static final int TYPE_BITS = BitUtils.log2ceil(ObjectType.values().length);
	
	private static final Map<Class, ObjectType> TYPES_MAP = new HashMap<Class, ObjectType>();
	
	static
	{
		TYPES_MAP.put(Byte.class, ObjectType.BYTE);
		TYPES_MAP.put(Character.class, ObjectType.CHAR);
		TYPES_MAP.put(Double.class, ObjectType.DOUBLE);
		TYPES_MAP.put(Float.class, ObjectType.FLOAT);
		TYPES_MAP.put(Integer.class, ObjectType.INT);
		TYPES_MAP.put(Long.class, ObjectType.LONG);
		TYPES_MAP.put(Short.class, ObjectType.SHORT);
		TYPES_MAP.put(ObjectId.ObjectUID.class, ObjectType.UID);
	}
	
	private static void writeType(BitStruct aStruct, ObjectType aType)
	{
		aStruct.writeInt(aType.ordinal(), TYPE_BITS);
	}
	
	private static ObjectType readType(BitStruct aStruct)
	{
		int theIndex = aStruct.readInt(TYPE_BITS);
		return ObjectType.values()[theIndex];
	}
	
	private static ObjectType findType(Object aObject)
	{
		ObjectType theType;
		if (aObject == null) theType = ObjectType.NULL;
		else theType = TYPES_MAP.get(aObject.getClass());
		if (theType == null) throw new RuntimeException("Not handled: "+aObject);
		return theType;
	}
	
	/**
	 * Returns the number of bits necessary to serialize the given object.
	 */
	protected static int getObjectBits(Object aObject)
	{
		ObjectType theType = findType(aObject);
		return theType.getObjectBits(aObject) + TYPE_BITS;
	}
	
	/**
	 * Writes an object to the specified struct. This method should be used by
	 * subclasses to serialize values.
	 */
	protected static void writeObject(BitStruct aBitStruct, Object aObject)
	{
		ObjectType theType = findType(aObject);
		writeType(aBitStruct, theType);
		theType.writeObject(aBitStruct, aObject);
	}
	
	/**
	 * Reads an object from the specified struct. This method should be used by
	 * subclasses to deserialize values.
	 */
	protected static Object readObject(BitStruct aBitStruct)
	{
		ObjectType theType = readType(aBitStruct);
		return theType.readObject(aBitStruct);
	}
	
	/**
	 * Returns the internal object id that corresponds to the given object.
	 * If the object is a {@link ObjectUID}, then its id converted to int
	 * is returned.
	 * @param aFail If true, the method fails with an exception if the object
	 * is not an {@link ObjectUID}. If false and the object is not an {@link ObjectUID},
	 * the method returns 0;
	 */
	public static int getObjectId(Object aObject, boolean aFail)
	{
		if (aObject instanceof ObjectId.ObjectUID)
		{
			ObjectId.ObjectUID theUid = (ObjectId.ObjectUID) aObject;
			long theId = theUid.getId();
			if ((theId & ~0xffffffffL) != 0) throw new RuntimeException("Object id overflow");
			return (int) theId;
		}
		else if (aFail) throw new RuntimeException("Not handled: "+aObject);
		else return 0;
	}
	

}
