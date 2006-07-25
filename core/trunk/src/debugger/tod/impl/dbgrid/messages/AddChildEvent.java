/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

/**
 * This message is sent to add a child event to a given behavior call event.
 * @author gpothier
 */
public class AddChildEvent extends GridMessage
{
	private byte[] itsParentPointer;
	private byte[] itsChildPointer;
	
	public AddChildEvent(byte[] aParentPointer, byte[] aChildPointer)
	{
		itsParentPointer = aParentPointer;
		itsChildPointer = aChildPointer;
	}
	
	public byte[] getChildPointer()
	{
		return itsChildPointer;
	}
	
	public byte[] getParentPointer()
	{
		return itsParentPointer;
	}
}
