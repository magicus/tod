/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.tools.parsers.workingset;

/**
 * Base class for sets that reason only on package names
 * 
 * @author gpothier
 */
public abstract class AbstractPackageSet extends AbstractClassSet
{
    private String itsPackageName;

    public AbstractPackageSet(String aName)
    {
        itsPackageName = aName;
    }

    public final boolean accept(String aClassname)
    {
        int theIndex = aClassname.lastIndexOf('.');
        String thePackageName = theIndex >= 0 ? aClassname.substring(0, theIndex) : "";
        return acceptPackage(itsPackageName, thePackageName);
    }

    /**
     * Whether to accept or reject a class of a given package
     * 
     * @param aReferencePackage
     *            The package that was given in the constructor
     * @param aPackageName
     *            The package to accept or reject
     */
    protected abstract boolean acceptPackage(String aReferencePackage, String aPackageName);
}