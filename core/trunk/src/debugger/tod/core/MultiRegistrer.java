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
	private List<ILocationRegistrer> itsRegistrers = new ArrayList<ILocationRegistrer>();

	public MultiRegistrer(ILocationRegistrer... aCollectors)
	{
		itsRegistrers = Arrays.asList(aCollectors);
	}

	public MultiRegistrer(List<ILocationRegistrer> aCollectors)
	{
		itsRegistrers = aCollectors;
	}
	
	protected List<ILocationRegistrer> getRegistrers()
	{
		return itsRegistrers;
	}


	public void registerBehavior(BehaviourKind aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName, String aSignature)
	{
        for (ILocationRegistrer theRegistrer : getRegistrers())
        {
            theRegistrer.registerBehavior(aBehaviourType, aBehaviourId, aTypeId, aBehaviourName, aSignature);
        }
	}

	public void registerBehaviorAttributes(int aBehaviourId, LineNumberInfo[] aLineNumberTable, LocalVariableInfo[] aLocalVariableTable)
	{
        for (ILocationRegistrer theRegistrer : getRegistrers())
        {
            theRegistrer.registerBehaviorAttributes(aBehaviourId, aLineNumberTable, aLocalVariableTable);
        }
	}

	public void registerField(int aFieldId, int aTypeId, String aFieldName)
	{
        for (ILocationRegistrer theRegistrer : getRegistrers())
        {
            theRegistrer.registerField(aFieldId, aTypeId, aFieldName);
        }
	}

	public void registerFile(int aFileId, String aFileName)
	{
        for (ILocationRegistrer theRegistrer : getRegistrers())
        {
            theRegistrer.registerFile(aFileId, aFileName);
        }
	}

	public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
	{
        for (ILocationRegistrer theRegistrer : getRegistrers())
        {
            theRegistrer.registerType(aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
        }
	}
    
    
}
