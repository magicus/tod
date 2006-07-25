/*
 * Created on Jul 21, 2006
 */
package tod.core.model.structure;

/**
 * Holds information about a debugged host.
 * @author gpothier
 */
public class HostInfo implements IHostInfo
{
	private int itsId;
	private String itsName;
	
	public HostInfo(int aId)
	{
		itsId = aId;
	}

	public HostInfo(int aId, String aName)
	{
		itsId = aId;
		itsName = aName;
	}
	
	public int getId()
	{
		return itsId;
	}
	
	public String getName()
	{
		return itsName;
	}

	public void setName(String aName)
	{
		itsName = aName;
	}

}
