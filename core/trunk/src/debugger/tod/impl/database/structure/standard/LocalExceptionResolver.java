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
package tod.impl.database.structure.standard;

import java.rmi.RemoteException;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IStructureDatabase.IBehaviorListener;
import tod.impl.database.structure.standard.ExceptionResolver.BehaviorInfo;
import tod.impl.database.structure.standard.ExceptionResolver.ClassInfo;
import tod.impl.dbgrid.dispatch.RIDatabaseNode;
import tod.utils.TODUtils;

/**
 * A version of {@link ExceptionResolver} that is tightly coupled
 * with a {@link IStructureDatabase}. It does two interesting things:
 * <li>it registers itself as a listener to the structure database
 * to be notified of new behaviors
 * <li>When an unknown behavior is requested, it creates one in the 
 * structure database
 * @author gpothier
 */
public class LocalExceptionResolver extends ExceptionResolver
implements IBehaviorListener
{
	private final IStructureDatabase itsStructureDatabase;

	public LocalExceptionResolver(IStructureDatabase aStructureDatabase)
	{
		itsStructureDatabase = aStructureDatabase;
		itsStructureDatabase.addBehaviorListener(this);
	}
	
	public void behaviorRegistered(IBehaviorInfo aBehavior)
	{
		BehaviorInfo theBehaviorInfo = TODUtils.createBehaviorInfo(aBehavior);
		registerBehavior(theBehaviorInfo);
	}
	
	@Override
	public int getBehaviorId(String aClassName, String aMethodName, String aMethodSignature)
	{
		ClassInfo theClassInfo = getClassInfo(aClassName);
		if (theClassInfo == null) 
		{
			IClassInfo theClass = itsStructureDatabase.getNewClass(aClassName);
			
			// This will register both the class and the behavior
			theClass.getNewBehavior(aMethodName, aMethodSignature);
			theClassInfo = getClassInfo(aClassName);
		}

		int theId = theClassInfo.getBehavior(aMethodName, aMethodSignature, false);
		if (theId == 0)
		{
			IClassInfo theClass = itsStructureDatabase.getClass(aClassName, true);
			
			// This will register the behavior
			theClass.getNewBehavior(aMethodName, aMethodSignature);
			theId = theClassInfo.getBehavior(aMethodName, aMethodSignature, true);
		}
		
		return theId;
	}
	
}
