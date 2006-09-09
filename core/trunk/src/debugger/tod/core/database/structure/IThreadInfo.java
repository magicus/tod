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

	/**
	 * Returns the internal (sequential, per host) id of the thread.
	 */
	public int getId();
	
	/**
	 * Returns the external (JVM) id of the thread.
	 */
	public long getJVMId();

	public String getName();

}