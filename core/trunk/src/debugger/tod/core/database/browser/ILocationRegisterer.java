 /*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

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
package tod.core.database.browser;


import tod.core.BehaviorKind;
import tod.core.database.structure.ILocationsRepository.LineNumberInfo;
import tod.core.database.structure.ILocationsRepository.LocalVariableInfo;


/**
 * Interface for objects that collect static code information, such as
 * methods, classes, fields, etc.
 * @author gpothier
 */
public interface ILocationRegisterer 
{
	
	public void registerFile (
			int aFileId,
			String aFileName);
	
	public void registerType (
			int aTypeId,
			String aTypeName,
			int aSupertypeId,
			int[] aInterfaceIds);
	
	/**
	 * Registers a behavior.
	 * @param aBehaviourType Type of behavior (constructor, static init, method)
	 * @param aBehaviourId Id assigned to the behavior
	 * @param aTypeId Id of the type that declares the behavior
	 * @param aBehaviourName Name of the behavior
	 * @param aSignature JVM signature of the method.
	 */
	public void registerBehavior (
			BehaviorKind aBehaviourType,
			int aBehaviourId,
			int aTypeId,
			String aBehaviourName,
			String aSignature);

	/**
	 * Registers additional attributes of a behavior.
	 * These attributes cannot be registered at the same time the behavior is registered
	 * because the information might be unstable at that time.
	 */
	public void registerBehaviorAttributes (
			int aBehaviourId,
			LineNumberInfo[] aLineNumberTable,
			LocalVariableInfo[] aLocalVariableTable);
	
	public void registerField (
			int aFieldId,
			int aTypeId,
			String aFieldName);

}
