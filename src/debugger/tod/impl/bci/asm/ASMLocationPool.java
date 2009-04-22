/*
 * Created on Oct 25, 2005
 */
package tod.impl.bci.asm;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import tod.core.BehaviourKind;
import tod.core.ILocationRegistrer;
import tod.core.bci.LocationPoolPersister;

public class ASMLocationPool
{
	private Map<String, Integer> itsMethodIds = new HashMap<String, Integer>();
	private int itsNextMethodId = 1;
	
	private Map<String, Integer> itsFieldIds = new HashMap<String, Integer>();
	private int itsNextFieldId = 1;
	
	private Map<String, Integer> itsTypeIds = new HashMap<String, Integer>();
	private int itsNextTypeId = 1;
	
	private ILocationRegistrer itsLocationRegistrer;
	
	public ASMLocationPool(ILocationRegistrer aLocationRegistrer, File aLocationsFile)
	{
		if (aLocationsFile != null)
		{
			DataOutputStream theOutputStream = null;
			try
			{
				if (! aLocationsFile.exists()) aLocationsFile.createNewFile();
				theOutputStream = new DataOutputStream(new FileOutputStream(aLocationsFile, true));
				
				// Read existing locations
				LocationPoolPersister.read(
						aLocationsFile,
						new LoadRegistrerAdapter(aLocationRegistrer));
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

	protected String getBehaviorKey(int aTypeId, String aName, String aDescriptor)
	{
		return ""+aTypeId+":"+aName+aDescriptor;
	}
	
	public int getBehaviorId(int aTypeId, String aName, String aDescriptor)
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
			itsLocationRegistrer.registerBehavior(BehaviourKind.METHOD, theId, aTypeId, aName, aDescriptor);
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
	 * We pass an instance of this class to {@link LocationPoolPersister#read(File, ILocationRegistrer)}
	 * so as to initialize our tables.
	 */
	private class LoadRegistrerAdapter implements ILocationRegistrer
	{
		private ILocationRegistrer itsTargetRegistrer;

		public LoadRegistrerAdapter(ILocationRegistrer aTargetRegistrer)
		{
			itsTargetRegistrer = aTargetRegistrer;
		}
		
		public void registerBehavior(BehaviourKind aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName, String aSignature)
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
