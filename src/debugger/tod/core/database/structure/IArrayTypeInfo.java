/*
 * Created on Nov 22, 2005
 */
package tod.core.database.structure;

public interface IArrayTypeInfo extends ITypeInfo
{

	public int getDimensions();

	public ITypeInfo getElementType();
}