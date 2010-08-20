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
package tod.impl.replay2;

import static tod.impl.bci.asm2.BCIUtils.*;

import org.objectweb.asm.Type;

import tod.impl.bci.asm2.SyntaxInsnList;

public class SList extends SyntaxInsnList
{
	public SList()
	{
		super();
	}
	
	/**
	 * Creates a {@link RuntimeException} with the given message and leaves it on the stack
	 */
	public void createRTEx(String aMessage)
	{
		LDC(aMessage);
		INVOKESTATIC(CLS_THREADREPLAYER, "createRtEx", "(Ljava/lang/String;)Ljava/lang/Exception;");
	}

	/**
	 * Creates a {@link UnsupportedOperationException} with the given message and leaves it on the stack
	 */
	public void createUnsupportedEx(String aMessage)
	{
		LDC(aMessage);
		INVOKESTATIC(CLS_THREADREPLAYER, "createUnsupportedEx", "(Ljava/lang/String;)Ljava/lang/Exception;");
	}
	
	/**
	 * Creates a runtime exception with an int arg that must be on the stack. Leaves the resulting
	 * exception on the stack
	 */
	public void createRTExArg(String aMessage)
	{
		LDC(aMessage);
		INVOKESTATIC(CLS_THREADREPLAYER, "createRtEx", "(ILjava/lang/String;)Ljava/lang/Exception;");
	}
	
	public void invokeReadRef(int aThreadReplayerSlot)
	{
		ALOAD(aThreadReplayerSlot);
		INVOKEVIRTUAL(CLS_THREADREPLAYER, "readRef", "()"+DSC_OBJECTID);
	}
	
	public void invokeReadInt(int aThreadReplayerSlot)
	{
		ALOAD(aThreadReplayerSlot);
		INVOKEVIRTUAL(CLS_THREADREPLAYER, "readInt", "()I");
	}
	
	public void invokeReadBoolean(int aThreadReplayerSlot)
	{
		ALOAD(aThreadReplayerSlot);
		INVOKEVIRTUAL(CLS_THREADREPLAYER, "readBoolean", "()Z");
	}
	
	public void invokeReadByte(int aThreadReplayerSlot)
	{
		ALOAD(aThreadReplayerSlot);
		INVOKEVIRTUAL(CLS_THREADREPLAYER, "readByte", "()B");
	}
	
	public void invokeReadChar(int aThreadReplayerSlot)
	{
		ALOAD(aThreadReplayerSlot);
		INVOKEVIRTUAL(CLS_THREADREPLAYER, "readChar", "()C");
	}
	
	public void invokeReadShort(int aThreadReplayerSlot)
	{
		ALOAD(aThreadReplayerSlot);
		INVOKEVIRTUAL(CLS_THREADREPLAYER, "readShort", "()S");
	}
	
	public void invokeReadFloat(int aThreadReplayerSlot)
	{
		ALOAD(aThreadReplayerSlot);
		INVOKEVIRTUAL(CLS_THREADREPLAYER, "readFloat", "()F");
	}
	
	public void invokeReadLong(int aThreadReplayerSlot)
	{
		ALOAD(aThreadReplayerSlot);
		INVOKEVIRTUAL(CLS_THREADREPLAYER, "readLong", "()J");
	}
	
	public void invokeReadDouble(int aThreadReplayerSlot)
	{
		ALOAD(aThreadReplayerSlot);
		INVOKEVIRTUAL(CLS_THREADREPLAYER, "readDouble", "()D");
	}
	
	public void invokeRead(Type aType, int aThreadReplayerSlot)
	{
		switch(aType.getSort())
		{
		case Type.ARRAY:
		case Type.OBJECT: invokeReadRef(aThreadReplayerSlot); break;
		case Type.INT: invokeReadInt(aThreadReplayerSlot); break;
		case Type.BOOLEAN: invokeReadBoolean(aThreadReplayerSlot); break;
		case Type.BYTE: invokeReadByte(aThreadReplayerSlot); break;
		case Type.CHAR: invokeReadChar(aThreadReplayerSlot); break;
		case Type.SHORT: invokeReadShort(aThreadReplayerSlot); break;
		case Type.FLOAT: invokeReadFloat(aThreadReplayerSlot); break;
		case Type.LONG: invokeReadLong(aThreadReplayerSlot); break;
		case Type.DOUBLE: invokeReadDouble(aThreadReplayerSlot); break;
		default: throw new RuntimeException("Not handled: "+aType);
		}
	}
}