/*
 * Created on Aug 19, 2006
 */
package tod.impl.dbgrid.dbnode.file;

import tod.impl.dbgrid.dbnode.file.SoftPagedFile.SoftPage;

/**
 * A page bank that creates pages of variable size,
 * starting at a minimum size and then doubling the size
 * at each request, until reaching a maximum size.
 * @author gpothier
 */
public class ExponentialPageBank extends PageBank
{
	private SoftPagedFile itsFile;
	
	private int itsCurrentSize;
	private int itsMaxSize;

	/**
	 * Creates an exponential bank using a new soft file based on the specified hard file.
	 */
	public ExponentialPageBank(HardPagedFile aFile, int aMinimumSize)
	{
		this(new SoftPagedFile(aFile, aMinimumSize));
	}
	
	/**
	 * Creates an exponential bank using an existing soft file.
	 */
	public ExponentialPageBank(SoftPagedFile aFile)
	{
		this(aFile, aFile.getMinPageSize());
	}

	/**
	 * Creates an exponential bank using an existing soft file and 
	 * starting with the specified page size.
	 */
	public ExponentialPageBank(SoftPagedFile aFile, int aCurrentSize)
	{
		itsFile = aFile;
		itsCurrentSize = aCurrentSize;
		itsMaxSize = itsFile.getMaxPageSize();
		assert itsCurrentSize <= itsMaxSize;
	}
	
	@Override
	public Page create()
	{
		SoftPage thePage = itsFile.create(itsCurrentSize);
		if (itsCurrentSize < itsMaxSize) itsCurrentSize *= 2;
		return thePage;
	}

	@Override
	public Page get(long aId)
	{
		return itsFile.get(aId);
	}

	@Override
	public int getPagePointerSize()
	{
		return itsFile.getPagePointerSize();
	}
	
	/**
	 * Returns the minimum page size, in bytes, supported by this file.
	 */
	public int getMinPageSize()
	{
		return itsFile.getMinPageSize();
	}

	/**
	 * Returns the maximum page size supported by this file.
	 */
	public int getMaxPageSize()
	{
		return itsFile.getMaxPageSize();
	}
	
	/**
	 * Returns the size of the next page that will be created.
	 */
	public int getCurrentPageSize()
	{
		return itsCurrentSize;
	}



	
}
