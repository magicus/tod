/*
 * Created on Oct 23, 2005
 */
package tod.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A registrer that actually dispathes messages to other registrers.
 * @author gpothier
 */
public class MultiRegistrer implements ILocationRegistrer
{
	private List<ILogCollector> itsCollectors = new ArrayList<ILogCollector>();

	public MultiRegistrer(ILogCollector... aCollectors)
	{
		itsCollectors = Arrays.asList(aCollectors);
	}

	public MultiRegistrer(List<ILogCollector> aCollectors)
	{
		itsCollectors = aCollectors;
	}
	
	protected List<ILogCollector> getCollectors()
	{
		return itsCollectors;
	}


	public void registerBehavior(BehaviourKind aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName, String aSignature)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.registerBehavior(aBehaviourType, aBehaviourId, aTypeId, aBehaviourName, aSignature);
        }
	}

	public void registerBehaviorAttributes(int aBehaviourId, LineNumberInfo[] aLineNumberTable, LocalVariableInfo[] aLocalVariableTable)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.registerBehaviorAttributes(aBehaviourId, aLineNumberTable, aLocalVariableTable);
        }
	}

	public void registerField(int aFieldId, int aTypeId, String aFieldName)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.registerField(aFieldId, aTypeId, aFieldName);
        }
	}

	public void registerFile(int aFileId, String aFileName)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.registerFile(aFileId, aFileName);
        }
	}

	public void registerThread(long aThreadId, String aName)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.registerThread(aThreadId, aName);
        }
	}

	public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.registerType(aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
        }
	}
    
    
}
