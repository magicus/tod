/*
 * Created on Jul 2, 2005
 */
package tod.vbuilder;

import tod.core.database.structure.ObjectId;
import zz.csg.api.IRectangularGraphicContainer;

/**
 * A graphic node that represents a Java object. 
 * @author gpothier
 */
public interface IObjectNode extends IRectangularGraphicContainer
{
	/**
	 * Returns the id of the represented object.
	 */
	public ObjectId getId();
	
	/**
	 * Returns the cell that created this node.
	 */
	public Cell getCell();

	public Object get(String aKey);
	
	public <T> T get(NodeAttribute<T> aAttribute);
	
	public Object set(String aKey, Object aValue);
	
	public <T> T set(NodeAttribute<T> aAttribute, T aValue);
}
