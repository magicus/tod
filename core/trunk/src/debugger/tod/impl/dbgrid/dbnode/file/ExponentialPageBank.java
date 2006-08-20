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
	
	public ExponentialPageBank(HardPagedFile aFile, int aMinimumSize)
	{
		itsFile = new SoftPagedFile(aFile, aMinimumSize);
		itsCurrentSize = aMinimumSize;
		itsMaxSize = aFile.getPageSize();
	}

	@Override
	public Page create()
	{
		SoftPage thePage = itsFile.create(itsCurrentSize);
		if (itsCurrentSize < itsMaxSize) itsCurrentSize *= 2;
		return thePage;
	}

//	@Override
//	public void free(Page aPage)
//	{
//		itsFile.free(aPage);
//	}
//
//	@Override
//	public void store(Page aPage)
//	{
//		itsFile.store(aPage);
//	}
//	
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

	
}
