/*
 * Created on Nov 22, 2005
 */
package tod.core.database.structure;

public interface IThreadInfo
{
	/**
	 * Returns the host on which this thread is run.
	 */
	public IHostInfo getHost();
	
	public long getId();

	public String getName();

}