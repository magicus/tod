package btree;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Queue;


/*
 * Created on May 26, 2006
 */

/**
 * This class manages the allocation, freeing, reading and
 * writing fixed-size pages within a file. Freed pages are recycled
 * in subsequent allocations
 */
public class SimplePageManager
{
	/**
	 * Number of pages currently allocated
	 */
	private int itsPageCount;
	
	private int itsReadCount;
	private int itsWriteCount;
	
	private RandomAccessFile itsFile;
	
	private Queue<Integer> itsFreePages = new LinkedList<Integer>();
	
	public SimplePageManager(File aFile, boolean aWriteCache)
	{
		try
		{
			itsFile = new RandomAccessFile(aFile, aWriteCache ? "rw" : "rwd");
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Allocates a new page and returns its id.
	 * @return A page id that can be used to read, write or free the 
	 * newly allocated page.
	 */
	public int alloc()
	{
		if (itsFreePages.isEmpty())
		{
			return itsPageCount++;
		}
		else return itsFreePages.remove();
	}
	
	/**
	 * Frees a previously allocated page.
	 */
	public void free(int aId)
	{
		itsFreePages.add(aId);
	}

	/**
	 * Reads the content of a page from the disk
	 * @param aId Id of the page
	 * @return Data contained in the page. If the page has never been written,
	 * its content is undefined.
	 */
	public byte[] read(int aId)
	{
		itsReadCount++;
		try
		{
			byte[] theData = new byte[Config.pageSize()];
			itsFile.seek(aId*Config.pageSize());
			itsFile.read(theData);
			return theData;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes out the content of a page to the disk.
	 * @param aId Id of the page.
	 * @param aData The data to write
	 */
	public void write(int aId, byte[] aData)
	{
		itsWriteCount++;
		try
		{
			itsFile.seek(aId*Config.pageSize());
			itsFile.write(aData);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public int getReadCount()
	{
		return itsReadCount;
	}

	public int getWriteCount()
	{
		return itsWriteCount;
	}
}