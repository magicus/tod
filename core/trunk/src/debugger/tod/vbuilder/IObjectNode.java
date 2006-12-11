/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
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
