/*
 * Created on Jun 12, 2005
 */
package tod.vbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.model.structure.ObjectId;
import tod.vbuilder.eval.IEvaluationContext;
import zz.utils.tree.AbstractTree;

public class Tree1 extends AbstractTree<ObjectId, ObjectId>
{
	private ITreeDefinition1 itsDefinition;
	private ObjectId itsRoot;
	private IEvaluationContext itsContext;
	
	private Map<ObjectId, List<ObjectId>> itsChildrenMap = 
		new HashMap<ObjectId, List<ObjectId>>();
	
	private Map<ObjectId, ObjectId> itsParentMap =
		new HashMap<ObjectId, ObjectId>();

	public ObjectId getRoot()
	{
		return itsRoot;
	}
	
	public List<ObjectId> getChildren(ObjectId aNode)
	{
		List<ObjectId> theList = itsChildrenMap.get(aNode);
		if (theList == null)
		{
			theList = new ArrayList<ObjectId>();
			
			Object theClientNode = itsContext.wrap(aNode);
			Iterable theChildren = itsDefinition.getChildren(theClientNode);
			if (theChildren != null) for (Object theClientChildNode : theChildren)
			{
				ObjectId theChildNode = itsContext.unwrap(theClientChildNode);
				theList.add(theChildNode);
			}
			
			itsChildrenMap.put(aNode, theList);
		}
		return theList;
	}

	public ObjectId getParent(ObjectId aNode)
	{
		ObjectId theParentNode = itsParentMap.get(aNode);
		if (theParentNode == null)
		{
			Object theClientNode = itsContext.wrap(aNode);
			Object theClientParentNode = itsDefinition.getParent(theClientNode);
			theParentNode = itsContext.unwrap(theClientParentNode);
			itsParentMap.put (aNode, theParentNode);
		}
		return theParentNode;
	}

	public int getChildCount(ObjectId aParent)
	{
		return getChildren(aParent).size();
	}

	public ObjectId getChild(ObjectId aParent, int aIndex)
	{
		return getChildren(aParent).get(aIndex);
	}

	public int getIndexOfChild(ObjectId aParent, ObjectId aChild)
	{
		return getChildren(aParent).indexOf(aChild);
	}

	public ObjectId getValue(ObjectId aNode)
	{
		return aNode;
	}

	public ObjectId setValue(ObjectId aNode, ObjectId aValue)
	{
		throw new UnsupportedOperationException();
	}

}
