/*
 * Created on Nov 22, 2005
 */
package tod.core.database.structure;

public interface IClassInfo extends ITypeInfo
{
	public IClassInfo getSupertype();
	
	public IClassInfo[] getInterfaces();

	public IFieldInfo getField(String aName);

	public IBehaviorInfo getBehavior(String aName, ITypeInfo[] aArgumentTypes);

	public Iterable<IFieldInfo> getFields();

	public Iterable<IBehaviorInfo> getBehaviors();
}