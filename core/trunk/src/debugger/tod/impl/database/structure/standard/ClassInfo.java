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
package tod.impl.database.structure.standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.database.browser.LocationUtils;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ILocationInfo.ISerializableLocationInfo;
import tod.impl.database.structure.standard.StructureDatabase.ClassNameInfo;
import zz.utils.Utils;

/**
 * Default implementation of {@link IClassInfo}.
 * @author gpothier
 */
public class ClassInfo extends TypeInfo 
implements IClassInfo, ISerializableLocationInfo
{
	private static final long serialVersionUID = -2583314414851419966L;

	private transient ClassNameInfo itsClassNameInfo;
	
	private boolean itsInScope;
	private boolean itsInterface;
	
	private String itsChecksum;
	
	/**
	 * Id of the supertype.
	 * Important: we keep the id and not the object for serialization purposes.
	 */
	private int itsSupertypeId;
	
	/**
	 * Ids of the interfaces implemented by this class.
	 * Important: we keep the ids and not the objects for serialization purposes.
	 */
	private int[] itsInterfacesIds;
	
	private Map<String, IFieldInfo> itsFieldsMap = new HashMap<String, IFieldInfo>();
	private Map<String, IBehaviorInfo> itsBehaviorsMap = new HashMap<String, IBehaviorInfo>();
	
	/**
	 * Whether this class info can be disposed.
	 * At the start of the system,
	 * and when all debugged VMs are disconnected from the database,
	 * every class is marked disposable.
	 * Once operation starts, classes are marked not disposable
	 * as they are used or added to the database. This permits to free the space
	 * used by old versions of classes that are not used anymore, while preserving
	 * various versions when classes are redefined at runtime.
	 */
	private boolean itsDisposable = false;
	
	private long itsStartTime;

	public ClassInfo(IStructureDatabase aDatabase, ClassNameInfo aClassNameInfo, String aName, int aId)
	{
		super(aDatabase, aId, aName);
		assert aDatabase != null;
		itsClassNameInfo = aClassNameInfo;
		
//		System.out.println(String.format("[Struct] class info [id: %d, name: %s]", aId, aName));
	}

	public void setup(
			boolean aIsInterface, 
			boolean aInScope, 
			String aChecksum, 
			IClassInfo[] aInterfaces, 
			IClassInfo aSuperclass)
	{
		itsInterface = aIsInterface;
		itsInScope = aInScope;
		itsChecksum = aChecksum;
		setInterfaces(aInterfaces);
		setSupertype(aSuperclass);
	}
	
	/**
	 * Same as {@link #getDatabase()} but casts to {@link StructureDatabase}.
	 * This is only for registration methods, that are used only where the original
	 * structure database exists.
	 */
	public StructureDatabase getStructureDatabase()
	{
		return (StructureDatabase) super.getDatabase();
	}
	
	@Override
	public void setDatabase(IStructureDatabase aDatabase)
	{
		super.setDatabase(aDatabase);
		for (IMemberInfo theMember : getMembers())
		{
			((MemberInfo) theMember).setDatabase(aDatabase);
		}
	}
	
	protected IMemberInfo[] getMembers()
	{
		List<IMemberInfo> theMembers = new ArrayList<IMemberInfo>();
		Utils.fillCollection(theMembers, itsBehaviorsMap.values());
		Utils.fillCollection(theMembers, itsFieldsMap.values());
		return theMembers.toArray(new IMemberInfo[theMembers.size()]);
	}
	
	public boolean isInScope()
	{
		return itsInScope;
	}
	
	public boolean isInterface()
	{
		return itsInterface;
	}
	
	public long getStartTime()
	{
		return itsStartTime;
	}
	
	public String getChecksum()
	{
		return itsChecksum;
	}

	public boolean isDisposable()
	{
		return itsDisposable;
	}

	public void setDisposable(boolean aDisposable)
	{
		itsDisposable = aDisposable;
	}

	/**
	 * Registers the given field info object.
	 */
	public void register(IFieldInfo aField)
	{
		itsFieldsMap.put (aField.getName(), aField);
		getStructureDatabase().registerField(aField);
	}
	
	/**
	 * Registers the given behavior info object.
	 */
	public void register(IBehaviorInfo aBehavior)
	{
		itsBehaviorsMap.put(getKey(aBehavior), aBehavior);
		getStructureDatabase().registerBehavior(aBehavior);
	}
	
	public IFieldInfo getField(String aName)
	{
		return itsFieldsMap.get(aName);
	}
	
	public IBehaviorInfo getBehavior(String aName, ITypeInfo[] aArgumentTypes)
	{
		return itsBehaviorsMap.get(getBehaviorKey(aName, aArgumentTypes));
	}
	
	public IBehaviorInfo getNewBehavior(String aName, String aDescriptor)
	{
		ITypeInfo[] theArgumentTypes = LocationUtils.getArgumentTypes(getDatabase(), aDescriptor, true);
		ITypeInfo theReturnType = LocationUtils.getReturnType(getDatabase(), aDescriptor, true);
		
		IBehaviorInfo theBehavior = getBehavior(aName, theArgumentTypes);
		if (theBehavior == null)
		{
			int theId = itsClassNameInfo.getBehaviorId(aName, theArgumentTypes);
			theBehavior = new BehaviorInfo(
					getStructureDatabase(), 
					theId,
					this,
					aName,
					aDescriptor,
					theArgumentTypes,
					theReturnType);
			
			register(theBehavior);
		}
		
		return theBehavior;
	}
	
	public IFieldInfo getNewField(String aName, ITypeInfo aType)
	{
		IFieldInfo theField = getField(aName);
		if (theField == null)
		{
			int theId = itsClassNameInfo.getFieldId(aName, aType);
			theField = new FieldInfo(getStructureDatabase(), theId, this, aName);
			
			register(theField);
		}
	
		return theField;
	}
	
	public Iterable<IFieldInfo> getFields()
	{
		return itsFieldsMap.values();
	}
	
	public Iterable<IBehaviorInfo> getBehaviors()
	{
		return itsBehaviorsMap.values();
	}
	
	public IClassInfo[] getInterfaces()
	{
		if (itsInterfacesIds == null) return new IClassInfo[0];
		
		IClassInfo[] theResult = new ClassInfo[itsInterfacesIds.length];
		for(int i=0;i<itsInterfacesIds.length;i++)
		{
			theResult[i] = getDatabase().getClass(itsInterfacesIds[i], true);
		}
		return theResult;
	}

	private void setInterfaces(IClassInfo[] aInterfaces)
	{
		itsInterfacesIds = new int[aInterfaces.length];
		for(int i=0;i<itsInterfacesIds.length;i++)
		{
			itsInterfacesIds[i] = aInterfaces[i].getId();
		}		
	}

	private void setSupertype(IClassInfo aSupertype)
	{
		itsSupertypeId = aSupertype != null ? aSupertype.getId() : 0;
	}

	public IClassInfo getSupertype()
	{
		return itsSupertypeId != 0 ? getDatabase().getClass(itsSupertypeId, true) : null;
	}

	public int getSize()
	{
		return 1;
	}

	public boolean isArray()
	{
		return false;
	}

	public boolean isPrimitive()
	{
		return false;
	}

	public boolean isVoid()
	{
		return false;
	}
	
	private String getKey(IBehaviorInfo aBehavior)
	{
		return getBehaviorKey(aBehavior.getName(), aBehavior.getArgumentTypes());
	}
	
	/**
	 * Returns a key (signature) for identifying a behavior.
	 */
	public static String getBehaviorKey(String aName, ITypeInfo[] aArgumentTypes)
	{
		StringBuilder theBuilder = new StringBuilder("b");
		theBuilder.append(aName);
		theBuilder.append('|');
		for (ITypeInfo theType : aArgumentTypes)
		{
			theBuilder.append('|');
			theBuilder.append(theType.getName());
		}
		
		return theBuilder.toString();
	}

	/**
	 * Returns a key (signature) for identifying a field.
	 */
	public static String getFieldKey(String aName, ITypeInfo aType)
	{
		return "f" + aName + "|" + aType.getName();
	}
	
	@Override
	public String toString()
	{
		return "Class ("+getId()+", "+getName()+")";
	}
	
	
}
