/*
 * Created on Jun 5, 2005
 */
package tod.vbuilder;


public interface ITreeDefinition1<N>
{
	public N getParent (N aNode);
	public Iterable<N> getChildren (N aNode);
}
