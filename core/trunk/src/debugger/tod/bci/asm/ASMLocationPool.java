/*
 * Created on Oct 25, 2005
 */
package tod.bci.asm;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tod.bci.LocationPoolPersister;
import tod.core.BehaviourType;
import tod.core.ILocationRegistrer;
import tod.core.ILogCollector;
import tod.session.ASMDebuggerConfig;

public class ASMLocationPool
{
	private Map<String, Integer> itsMethodIds = new HashMap<String, Integer>();
	private int itsNextMethodId = 1;
	
	private Map<String, Integer> itsFieldIds = new HashMap<String, Integer>();
	private int itsNextFieldId = 1;
	
	private Map<String, Integer> itsTypeIds = new HashMap<String, Integer>();
	private int itsNextTypeId = 1;
	
	private final ASMDebuggerConfig itsConfig;
	private ILocationRegistrer itsLocationRegistrer;
	
	public ASMLocationPool(ASMDebuggerConfig aConfig)
	{
		itsConfig = aConfig;
		ILogCollector theCollector = itsConfig.getCollector();
		
		File theLocationsFile = itsConfig.getLocationsFile();
		if (theLocationsFile != null)
		{
			DataOutputStream theOutputStream = null;
			try
			{
				if (! theLocationsFile.exists()) theLocationsFile.createNewFile();
				theOutputStream = new DataOutputStream(new FileOutputStream(theLocationsFile, true));
				
				// Read existing locations
				LocationPoolPersister.read(theLocationsFile, new LoadRegistrerAdapter(theCollector));
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
					theCollector,
					theOutputStream);			
		}
		else
		{
			itsLocationRegistrer = itsConfig.getCollector();
		}
	}

	public int getMethodId(int aTypeId, String aName, String aDescriptor)
	{
		String theKey = ""+aTypeId+":"+aName+aDescriptor;
		Integer theId = itsMethodIds.get(theKey);
		if (theId == null)
		{
			theId = itsNextMethodId++;
			itsMethodIds.put(theKey, theId);
			itsLocationRegistrer.registerBehavior(BehaviourType.METHOD, theId, aTypeId, aName, aDescriptor);
		}
		
		return theId.intValue();
	}
	
	public int getFieldId(int aTypeId, String aName, String aDescriptor)
	{
		String theKey = ""+aTypeId+":"+aName+aDescriptor;
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
		
		public void registerBehavior(BehaviourType aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName, String aSignature)
		{
			itsTargetRegistrer.registerBehavior(aBehaviourType, aBehaviourId, aTypeId, aBehaviourName, aSignature);
		}

		public void registerBehaviorAttributes(int aBehaviourId, LineNumberInfo[] aLineNumberTable, LocalVariableInfo[] aLocalVariableTable)
		{
			itsTargetRegistrer.registerBehaviorAttributes(aBehaviourId, aLineNumberTable, aLocalVariableTable);
		}

		public void registerField(int aFieldId, int aTypeId, String aFieldName)
		{
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
			itsTargetRegistrer.registerType(aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
		}

	}
}
