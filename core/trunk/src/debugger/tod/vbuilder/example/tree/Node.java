/*
 * Created on Jun 12, 2005
 */
package tod.vbuilder.example.tree;

import java.util.List;

import tod.core.model.structure.ObjectId;
import tod.vbuilder.eval.IEvaluationContext;

public class Node implements ObjectId
{
	private Node itsParent;
	private List<Node> itsChildren;
	private Object itsValue;
	
	public Object getValue()
	{
		return itsValue;
	}
	
	public void setValue(Object aValue)
	{
		itsValue = aValue;
	}
	
	public List<Node> getChildren()
	{
		return itsChildren;
	}
	
	public Node getParent()
	{
		return itsParent;
	}
}
