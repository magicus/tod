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
package tod.core.transport;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;

import tod.agent.AgentConfig;
import tod.agent.transport.ValueType;
import tod.core.DebugFlags;
import tod.core.database.structure.ObjectId;

/**
 * Permits to read registered objects.
 * @author gpothier
 */
public class ValueReader
{
	public static Object readRegistered (byte[] aData)
	{
		int theSize = aData.length;
		
		Object theObject;
		try
		{
			ObjectInputStream theObjectStream = new ObjectInputStream(new ByteArrayInputStream(aData));
			theObject = theObjectStream.readObject();
		}
		catch (ClassNotFoundException e)
		{
//			System.err.println("Warning - class no found: "+e.getMessage());
			theObject = "Unknown ("+e.getMessage()+")";
		}
		catch (InvalidClassException e)
		{
			System.err.println("Warning - invalid class: "+e.getMessage());
			theObject = "Unknown ("+e.getMessage()+")";					
		}
		catch (Throwable e)
		{
			System.err.println("Error while deserializing object");
			e.printStackTrace();
			System.err.println(" packet size: "+theSize);
			for(int i=0;i<theSize;i++) System.err.print(Integer.toHexString(aData[i])+" ");			
			System.err.println();
			theObject = "Deserialization error";
		}

		return theObject;
	}
	
	private static ValueType readValueType (DataInputStream aStream) throws IOException
	{
		byte theByte = aStream.readByte();
		return ValueType.VALUES[theByte];
	}
	
    public static Object[] readArguments(DataInputStream aStream) throws IOException
    {
        int theCount = aStream.readInt();
        Object[] theArguments = new Object[theCount];
        
        for (int i=0;i<theCount;i++)
        {
            theArguments[i] = readValue(aStream);
        }
        return theArguments;
    }
    
	public static Object readValue (DataInputStream aStream) throws IOException
	{
		ValueType theType = readValueType(aStream);
		switch (theType)
		{
			case NULL:
				return null;
				
			case BOOLEAN:
				return new Boolean (aStream.readByte() != 0);
				
			case BYTE:
				return new Byte (aStream.readByte());
				
			case CHAR:
				return new Character (aStream.readChar());
				
			case INT:
				return new Integer (aStream.readInt());
				
			case LONG:
				return new Long (aStream.readLong());
				
			case FLOAT:
				return new Float (aStream.readFloat());
				
			case DOUBLE:
				return new Double (aStream.readDouble());
				
			case OBJECT_UID:
			{
				long theObjectId = aStream.readLong();
				if (DebugFlags.IGNORE_HOST) theObjectId >>>= AgentConfig.HOST_BITS;
				return new ObjectId(theObjectId);
			}
				
			default:
				throw new RuntimeException("Unexpected message: "+theType);
		}
	}
	

}
