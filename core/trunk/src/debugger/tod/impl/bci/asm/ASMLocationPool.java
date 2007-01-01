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
package tod.impl.bci.asm;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

import tod.core.BehaviorKind;
import tod.core.ILocationRegisterer;
import tod.core.bci.LocationPoolPersister;
import tod.core.database.structure.ILocationInfo;

public class ASMLocationPool
{
	private Map<String, Integer> itsMethodIds = new HashMap<String, Integer>();
	private int itsNextMethodId = 1;
	
	private Map<String, Integer> itsFieldIds = new HashMap<String, Integer>();
	private int itsNextFieldId = 1;
	
	private Map<String, Integer> itsTypeIds = new HashMap<String, Integer>();
	private int itsNextTypeId = 1;
	
	private ILocationRegisterer itsLocationRegistrer;
	
	/**
	 * We keep track of the methods that have already been traced.
	 * @see ASMBehaviorInstrumenter#hasTrace(String)
	 */
	private Set<Integer> itsTracedMethods = new HashSet<Integer>();
	
	public ASMLocationPool(ILocationRegisterer aLocationRegistrer, File aLocationsFile)
	{
		if (aLocationsFile != null)
		{
			DataOutputStream theOutputStream = null;
			try
			{
				System.out.println("Setting up locations pool using "+aLocationsFile);
				if (! aLocationsFile.exists()) 
				{
					aLocationsFile.getParentFile().mkdirs();
					aLocationsFile.createNewFile();
				}
				theOutputStream = new DataOutputStream(new FileOutputStream(aLocationsFile, true));
				
				// Read existing locations
				LocationPoolPersister.read(
						aLocationsFile,
						new LoadRegistererAdapter(aLocationRegistrer));
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			// prepare to store new locations
			itsLocationRegistrer = new LocationPoolPersister.Store(
					aLocationRegistrer,
					theOutputStream);			
		}
		else
		{
			itsLocationRegistrer = aLocationRegistrer;
		}
	}
	
	public void setTraced(int aId)
	{
		itsTracedMethods.add(aId);
	}
	
	public boolean isTraced(int aId)
	{
		return itsTracedMethods.contains(aId);
	}

	protected String getBehaviorKey(int aTypeId, String aName, String aDescriptor)
	{
		return ""+aTypeId+":"+aName+aDescriptor;
	}
	
	public int getBehaviorId(
			int aTypeId, 
			String aName, 
			String aDescriptor,
			boolean aStatic)
	{
		String theKey = getBehaviorKey(aTypeId, aName, aDescriptor);
		Integer theId = itsMethodIds.get(theKey);
		if (theId == null)
		{
			// Pregerister argument and return types
			preregisterType(Type.getReturnType(aDescriptor));
			
			for (Type theType : Type.getArgumentTypes(aDescriptor))
			{
				preregisterType(theType);
			}

			// Register the behavior
			theId = itsNextMethodId++;
			itsMethodIds.put(theKey, theId);
			
			BehaviorKind theKind;
			if ("<init>".equals(aName)) theKind = BehaviorKind.CONSTRUCTOR;
			else if ("<clinit>".equals(aName)) theKind = BehaviorKind.STATIC_BLOCK;
			else theKind = aStatic ? BehaviorKind.STATIC_METHOD : BehaviorKind.METHOD;
			
			itsLocationRegistrer.registerBehavior(theKind, theId, aTypeId, aName, aDescriptor);
		}
		
		return theId.intValue();
	}
	
	private void preregisterType (Type aType)
	{
		if (aType.getSort() == Type.OBJECT)
		{
			getTypeId(aType.getInternalName());
		}
		else if (aType.getSort() == Type.ARRAY)
		{
			Type theElementType = aType.getElementType();
			preregisterType(theElementType);
		}
	}
	
	protected String getFieldKey(int aTypeId, String aName)
	{
		return ""+aTypeId+":"+aName;
	}
	
	public int getFieldId(int aTypeId, String aName, String aDescriptor)
	{
		String theKey = getFieldKey(aTypeId, aName);
		Integer theId = itsFieldIds.get(theKey);
		if (theId == null)
		{
			theId = itsNextFieldId++;
			itsFieldIds.put(theKey, theId);
			itsLocationRegistrer.registerField(theId, aTypeId, aName);
		}
		
		return theId.intValue();
	}

	public int getTypeId(String aName)
	{
		Integer theId = itsTypeIds.get(aName);
		if (theId == null)
		{
			theId = itsNextTypeId++;
			itsTypeIds.put (aName, theId);
			
			// preregister type
			registerType(theId, aName, -1, null);
		}
		
		return theId.intValue();
	}
	
	public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
	{
		itsLocationRegistrer.registerType(
				aTypeId, 
				BCIUtils.getClassName(aTypeName), 
				aSupertypeId, 
				aInterfaceIds);
	}
	
	public void registerBehaviorAttributes (int aMethodId, ASMMethodInfo aInfo)
	{
		itsLocationRegistrer.registerBehaviorAttributes(
				aMethodId, 
				aInfo.createLineNumberTable(), 
				aInfo.createLocalVariableTable());
	}
	
	/**
	 * We pass an instance of this class to {@link LocationPoolPersister#read(File, ILocationRegisterer)}
	 * so as to initialize our tables.
	 */
	private class LoadRegistererAdapter implements ILocationRegisterer
	{
		private ILocationRegisterer itsTargetRegistrer;

		public LoadRegistererAdapter(ILocationRegisterer aTargetRegistrer)
		{
			itsTargetRegistrer = aTargetRegistrer;
		}
		
		public void registerBehavior(BehaviorKind aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName, String aSignature)
		{
			itsNextMethodId = Math.max(itsNextMethodId, aBehaviourId+1);
			itsMethodIds.put(getBehaviorKey(aTypeId, aBehaviourName, aSignature), aBehaviourId);
			
			itsTargetRegistrer.registerBehavior(aBehaviourType, aBehaviourId, aTypeId, aBehaviourName, aSignature);
		}

		public void registerBehaviorAttributes(int aBehaviourId, LineNumberInfo[] aLineNumberTable, LocalVariableInfo[] aLocalVariableTable)
		{
			itsNextMethodId = Math.max(itsNextMethodId, aBehaviourId+1);
			itsTargetRegistrer.registerBehaviorAttributes(aBehaviourId, aLineNumberTable, aLocalVariableTable);
		}

		public void registerField(int aFieldId, int aTypeId, String aFieldName)
		{
			itsNextFieldId = Math.max(itsNextFieldId, aFieldId+1);
			itsFieldIds.put(getFieldKey(aTypeId, aFieldName), aFieldId);
			
			itsTargetRegistrer.registerField(aFieldId, aTypeId, aFieldName);
		}

		public void registerFile(int aFileId, String aFileName)
		{
			itsTargetRegistrer.registerFile(aFileId, aFileName);
		}

		public void registerThread(long aThreadId, String aName)
		{
			throw new UnsupportedOperationException();
		}

		public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
		{
			itsNextTypeId = Math.max(itsNextTypeId, aTypeId+1);
			String theTypeKey = aTypeName.replace('.', '/');
			itsTypeIds.put (theTypeKey, aTypeId);
			
			
			itsTargetRegistrer.registerType(aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
		}

	}
}
