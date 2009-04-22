/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import java.io.Serializable;

import static tod.impl.dbgrid.DebuggerGridConfig.*;

import zz.utils.bit.BitStruct;

/**
 * Base class for messages that are passed between the dispatcher and
 * the database nodes. 
 * @author gpothier
 */
public abstract class GridMessage implements Serializable
{
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
		case CONSTRUCTOR_CHAINING:
			return new GridBehaviorCallEvent(aBitStruct, MessageType.CONSTRUCTOR_CHAINING);
		case EXCEPTION_GENERATED:
			return new GridExceptionGeneratedEvent(aBitStruct);
		case FIELD_WRITE:
			return new GridFieldWriteEvent(aBitStruct);
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
