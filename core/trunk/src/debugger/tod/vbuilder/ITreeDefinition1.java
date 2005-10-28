/*
 * Created on Jun 5, 2005
 */
package tod.vbuilder;

import tod.core.model.structure.ObjectId;

public interface ITreeDefinition1<N>
{
	public N getParent (N aNode);
	public Iterable<N> getChildren (N aNode);
}
