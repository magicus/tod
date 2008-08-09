/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid.db.file;

import tod.impl.dbgrid.db.file.PageBank.Page;
import tod.impl.dbgrid.db.file.SoftPagedFile.SoftPage;

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
	public void free(Page aPage)
	{
		throw new UnsupportedOperationException();
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
