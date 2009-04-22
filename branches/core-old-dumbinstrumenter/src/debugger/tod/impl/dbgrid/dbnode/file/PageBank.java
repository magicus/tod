/*
 * Created on Aug 19, 2006
 */
package tod.impl.dbgrid.dbnode.file;

import zz.utils.bit.IntBitStruct;

/**
 * A page bank stores and manages data pages, each of which identified by 
 * a unique number, or pointer.
 * @author gpothier
 */
public abstract class PageBank<P extends PageBank.Page, S extends PageBank.PageBitStruct>
{
	/**
	 * Retrieve a page given its id.
	 */
	public abstract P get(long aId);
	
	/**
	 * Creates a new, blank page
	 */
	public abstract P create();
	
	/**
	 * Returns the number of bits necessary to represent a 
	 * page pointer.
	 */
	public abstract int getPagePointerSize();
	
	public static abstract class Page
	{
		private long itsPageId;

		public Page(long aPageId)
		{
			itsPageId = aPageId;
		}

		public long getPageId()
		{
			return itsPageId;
		}
		
		/**
		 * Marks this page as modified.
		 */
		abstract void modified();

		/**
		 * Returns the size of this page, in bytes.
		 */
		public abstract int getSize();
		
		public abstract PageBitStruct asBitStruct();
	}
	
	public static abstract class PageBitStruct extends IntBitStruct
	{
		private Page itsPage;

		public PageBitStruct(int aOffset, int aSize, Page aPage)
		{
			super(null, aOffset, aSize);
			itsPage = aPage;
		}

		public Page getPage()
		{
			return itsPage;
		}
		
		@Override
		protected void grow(int aMinSize)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		protected abstract int[] getData();
		
		@Override
		protected void setData(int[] aData)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeBoolean(boolean aValue)
		{
			super.writeBoolean(aValue);
			getPage().modified();
		}

		@Override
		public void writeBytes(byte[] aBytes, int aBitCount)
		{
			super.writeBytes(aBytes, aBitCount);
			getPage().modified();
		}

		@Override
		public void writeBytes(byte[] aBytes)
		{
			super.writeBytes(aBytes);
			getPage().modified();
		}

		@Override
		public void writeInt(int aValue, int aBitCount)
		{
			super.writeInt(aValue, aBitCount);
			getPage().modified();
		}

		@Override
		public void writeLong(long aValue, int aBitCount)
		{
			super.writeLong(aValue, aBitCount);
			getPage().modified();
		}
	}
}
