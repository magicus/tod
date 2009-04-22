/*
 * Created on Jul 4, 2005
 */
package tod.vbuilder;

public interface ICellEventProcessor
{
	public void nodeImported(IObjectNode aNode);
	public void nodeRemoved(IObjectNode aNode);
	public void attributeChanged(IObjectNode aNode, String aAttributeName, Object aOldValue, Object aNewValue); 

}
