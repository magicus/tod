/*
 * Created on Jul 11, 2005
 */
package tod.vbuilder.example.tree;

import tod.core.model.structure.ObjectId;
import tod.vbuilder.eval.IEvaluationContext;

/**
 * This dummy evaluation context works with aobjects that implement
 * {@link tod.core.model.structure.ObjectId}.
 * @author gpothier
 */
public class DummyEvaluationContext implements IEvaluationContext
{
	public Object wrap(ObjectId aId)
	{
		return aId;
	}

	public ObjectId unwrap(Object aObject)
	{
		return (ObjectId) aObject;
	}

}
