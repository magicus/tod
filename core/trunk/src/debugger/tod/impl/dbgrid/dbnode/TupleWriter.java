/*
 * Created on Aug 10, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;
import tod.impl.dbgrid.dbnode.PagedFile.Page;
import tod.impl.dbgrid.dbnode.PagedFile.PageBitStruct;

/**
 * Writes out {@link Tuple}s in a linked list of {@link Page}s.
 * @author gpothier
 */
public class TupleWriter<T extends Tuple>
{
	private PagedFile itsFile;
	private TupleCodec<T> itsTupleCodec;
	private Page itsCurrentPage;
	private PageBitStruct itsCurrentStruct;
	private int itsPagesCount;

	/**
	 * Creates a tuple writer that starts a new linked list of pages.
	 */
	public TupleWriter(PagedFile aFile, TupleCodec<T> aTupleCodec)
	{
		itsFile = aFile;
		itsTupleCodec = aTupleCodec;
		itsCurrentPage = itsFile.createPage();
		itsCurrentStruct = itsCurrentPage.asBitStruct();
		itsPagesCount = 1;
	}

	/**
	 * Creates a tuple writer that resumes writing at the specified page.
	 */
	public TupleWriter(PagedFile aFile, TupleCodec<T> aTupleCodec, Page aPage, int aPos)
	{
		itsFile = aFile;
		itsTupleCodec = aTupleCodec;
		
		itsCurrentPage = aPage;
		itsCurrentStruct = itsCurrentPage.asBitStruct();
		itsCurrentStruct.setPos(aPos);
	}
	
	/**
	 * Writes a tuple to the file.
	 */
	public void add(T aTuple)
	{
		if (itsCurrentStruct.getRemainingBits() < itsTupleCodec.getTupleSize() + DB_PAGE_POINTER_BITS)
		{
			Page theNextPage = itsFile.createPage();
			itsPagesCount++;
			
			PageBitStruct theNextStruct = theNextPage.asBitStruct();
			long theNextPageId = theNextStruct.getPage().getPageId();
			
			// Write next page id (+1: 0 means no next page).
			itsCurrentStruct.writeLong(theNextPageId+1, DB_PAGE_POINTER_BITS);
			
			newPageHook(itsCurrentStruct, theNextPageId);
			
			// Save old page
			itsFile.writePage(itsCurrentPage);
//			itsFile.freePage(itsCurrentPage);
			
			itsCurrentStruct = theNextStruct;
			itsCurrentPage = theNextPage;
		}

		if (itsCurrentStruct.getPos() == 0) startPageHook(itsCurrentStruct, aTuple);
		
		itsTupleCodec.write(itsCurrentStruct, aTuple);
	}
	
	/**
	 * A hook method that is called whenever a new page is started.
	 * The method does nothing by default.
	 * @param aStruct The struct of the page that is being finished.
	 */
	protected void newPageHook(PageBitStruct aStruct, long aNewPageId)
	{
	}

	/**
	 * A hook method that is called whenever a page is about to receive
	 * its first tuple.
	 * The method does nothing by default.
	 * @param aStruct The struct of the page that is being started
	 * @param aTuple The tuple that is being written
	 */
	protected void startPageHook(PageBitStruct aStruct, T aTuple)
	{
	}

	/**
	 * Returns the number of pages used by this writer.
	 */
	public int getPagesCount()
	{
		return itsPagesCount;
	}

	/**
	 * The currently used page.
	 */
	public Page getCurrentPage()
	{
		return itsCurrentPage;
	}
}