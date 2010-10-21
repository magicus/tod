/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.database.structure.standard;

import tod.core.ILogCollector;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IMutableFieldInfo;
import tod.core.database.structure.IShareableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;

/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a field.
 * @author gpothier
 */
public class FieldInfo extends MemberInfo implements IMutableFieldInfo
{
	private static final long serialVersionUID = 1642825455287392890L;

	/**
	 * We keep the type id instead of actual type in order to simplify
	 * the handling of remote structure databases.
	 */
	private final long itsTypePtr;
	
	private final int itsSlotIndex;

	public FieldInfo(
			IShareableStructureDatabase aDatabase, 
			int aId, 
			IClassInfo aDeclaringClass, 
			String aName,
			ITypeInfo aType,
			int aAccessFlags)
	{
		super(aDatabase, aId, aDeclaringClass, aName, aAccessFlags);
		itsTypePtr = getTypePtr(aType);
		itsSlotIndex = getSlotIndex(aDeclaringClass);
	}
	
	private static int getSlotIndex(IClassInfo aDeclaringClass)
	{
		int theCount = 0;
		IClassInfo theClass = aDeclaringClass;
		while(theClass != null)
		{
			theCount += theClass.getFieldCount();
			theClass = theClass.getSupertype();
		}
		
		return theCount;
	}

	@Override
	public IClassInfo getDeclaringType()
	{
		return (IClassInfo) super.getDeclaringType();
	}
	
	public ITypeInfo getType()
	{
		return getType(getDatabase(), itsTypePtr);
	}

	public int getSlotIndex()
	{
		return itsSlotIndex;
	}
	
	@Override
	public String toString()
	{
		return "Field ("+getId()+", "+getName()+")";
	}

	private static final long TPTR_OTHER = 		0x0000000100000000L;
	private static final long TPTR_ARRAY = 		0x0000000200000000L;
	private static final long TPTR_DIMMASK = 	0x0000ff0000000000L;
	private static final int TPTR_DIMSHIFT = 40;
	
	private static long getTypePtr(ITypeInfo aType)
	{
		if (aType instanceof IArrayTypeInfo)
		{
			IArrayTypeInfo theType = (IArrayTypeInfo) aType;
			return TPTR_ARRAY 
			| ((theType.getDimensions() << TPTR_DIMSHIFT) & TPTR_DIMMASK)
			| theType.getElementType().getId();
		}
		else return TPTR_OTHER | aType.getId();
	}
	
	private static ITypeInfo getType(IStructureDatabase aDatabase, long aTypePtr)
	{
		if ((aTypePtr & TPTR_ARRAY) != 0)
		{
			int theDim = (int)((aTypePtr & TPTR_DIMMASK) >>> TPTR_DIMSHIFT);
			int theId = (int) aTypePtr;
			ITypeInfo theElementType = aDatabase.getType(theId, true);
			return aDatabase.getArrayType(theElementType, theDim);
		}
		else if ((aTypePtr & TPTR_OTHER) != 0)
		{
			int theId = (int) aTypePtr;
			return aDatabase.getType(theId, true);
		}
		else throw new RuntimeException("Not handled: "+aTypePtr);
	}
}
