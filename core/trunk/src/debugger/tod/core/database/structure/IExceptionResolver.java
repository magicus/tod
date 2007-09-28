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
package tod.core.database.structure;

/**
 * Permits to resolve exception locations to behavior ids.
 * This is in fact a very reduced view of the {@link IStructureDatabase},
 * holding only class and behavior names.
 * @author gpothier
 */
public interface IExceptionResolver
{
	/**
	 * Returns the id of the specified behavior
	 * @param aMethodSignature JVM signature of the method.
	 */
	public int getBehaviorId(String aClassName, String aMethodName, String aMethodSignature);
}
