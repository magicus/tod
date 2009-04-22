/*
 * Created on Jun 12, 2005
 */
package tod.vbuilder.example.tree;

import tod.vbuilder.ITreeDefinition1;

public class TodTreeDefinition implements ITreeDefinition1<Node>
{
	public Node getParent(Node aNode)
	{
		return aNode.getParent();
	}

	public Iterable<Node> getChildren(Node aNode)
	{
		return aNode.getChildren();
	}
}
