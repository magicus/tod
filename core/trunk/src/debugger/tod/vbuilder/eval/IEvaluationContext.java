/*
 * Created on Jun 4, 2005
 */
package tod.vbuilder.eval;

import tod.core.model.structure.ObjectId;

public interface IEvaluationContext
{
	public Object wrap(ObjectId aId);
	public ObjectId unwrap(Object aObject);
}
