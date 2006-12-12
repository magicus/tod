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
package tod.impl.dbgrid;

/**
 * Interface for bidirectional iterators.
 * @author gpothier
 */
public interface BidiIterator<T>
{
	/**
	 * Retrieves the next elements and moves the pointer.
	 */
	public T next();
	
	/**
	 * Returns true if a next element is available.
	 */
	public boolean hasNext();
	
	/**
	 * Returns the element that would be returned by {@link #next()}, without
	 * moving the pointer, or null if no element is available.
	 */
	public T peekNext();
	
	
	public T previous();
	public boolean hasPrevious();
	public T peekPrevious();
}
