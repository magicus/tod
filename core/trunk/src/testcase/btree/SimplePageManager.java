/*
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
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
