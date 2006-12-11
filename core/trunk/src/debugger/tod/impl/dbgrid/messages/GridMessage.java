/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
*/
package tod.impl.dbgrid.messages;

import static tod.impl.dbgrid.DebuggerGridConfig.MESSAGE_TYPE_BITS;

import java.io.Serializable;

import zz.utils.bit.BitStruct;

/**
 * Base class for messages that are passed between the dispatcher and
 * the database nodes. 
 * @author gpothier
 */
public abstract class GridMessage implements Serializable
{
	private static final long serialVersionUID = -4356933902943147698L;

	/**
	 * Creates an event from a serialized representation, symmetric to
	 * {@link #writeTo(BitStruct)}.
	 */
	public static GridMessage read(BitStruct aBitStruct)
	{
		MessageType theType = MessageType.values()[aBitStruct.readInt(MESSAGE_TYPE_BITS)];
		switch (theType)
		{
		case BEHAVIOR_EXIT:
			return new GridBehaviorExitEvent(aBitStruct);
		case SUPER_CALL:
			return new GridBehaviorCallEvent(aBitStruct, MessageType.SUPER_CALL);
		case EXCEPTION_GENERATED:
			return new GridExceptionGeneratedEvent(aBitStruct);
		case FIELD_WRITE:
			return new GridFieldWriteEvent(aBitStruct);
		case ARRAY_WRITE:
			return new GridArrayWriteEvent(aBitStruct);
		case INSTANTIATION:
			return new GridBehaviorCallEvent(aBitStruct, MessageType.INSTANTIATION);
		case LOCAL_VARIABLE_WRITE:
			return new GridVariableWriteEvent(aBitStruct);
		case METHOD_CALL:
			return new GridBehaviorCallEvent(aBitStruct, MessageType.METHOD_CALL);
		default: throw new RuntimeException("Not handled: "+theType); 
		}
	}
	
	/**
	 * Returns the type of this event.
	 */
	public abstract MessageType getMessageType();

	
	/**
	 * Writes out a representation of this event to a {@link BitStruct}.
	 * Subclasses must override this method to serialize their attributes, and
	 * must call super first.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
		aBitStruct.writeInt(getMessageType().ordinal(), MESSAGE_TYPE_BITS);
	}
	
	/**
	 * Returns the number of bits necessary to serialize this message. Subclasses
	 * should override this method and sum the number of bits they need to the
	 * number of bits returned by super.
	 */
	public int getBitCount()
	{
		return MESSAGE_TYPE_BITS;
	}
}
