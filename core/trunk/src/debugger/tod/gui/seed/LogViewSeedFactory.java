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
package tod.gui.seed;

import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.IGUIManager;

/**
 * A factory of {@link LogViewSeed}s.
 * 
 * @author gpothier
 */
public class LogViewSeedFactory 
{
	private static LogViewSeed createSeed(
			IGUIManager aGUIManager,
			ILogBrowser aLog,
			String aTitle,
			IEventFilter aFilter)
	{
		return new FilterSeed(aGUIManager, aLog, aTitle, aFilter);
	}
	
	/**
	 * Returns a seed that can be used to view the events that are related to
	 * the specified location info.
	 */
	public static LogViewSeed getDefaultSeed(
			IGUIManager aGUIManager,
			ILogBrowser aLog,
			ILocationInfo aInfo)
	{
		if (aInfo instanceof ITypeInfo)
		{
			ITypeInfo theTypeInfo = (ITypeInfo) aInfo;
			return createSeed(
					aGUIManager, 
					aLog,
					"Instantiations of "+theTypeInfo.getName(),
					aLog.createInstantiationsFilter(theTypeInfo));
		}
		else if (aInfo instanceof IBehaviorInfo)
		{
			IBehaviorInfo theBehaviourInfo = (IBehaviorInfo) aInfo;
			return createSeed(
					aGUIManager, 
					aLog, 
					"Calls of "+theBehaviourInfo.getName(),
					aLog.createBehaviorCallFilter(theBehaviourInfo));
		}
		else if (aInfo instanceof IFieldInfo)
		{
			IFieldInfo theFieldInfo = (IFieldInfo) aInfo;

			return createSeed(
					aGUIManager, 
					aLog, 
					"Assignments of "+theFieldInfo.getName(),
					aLog.createFieldFilter(theFieldInfo));
		}
		else return null;
	}
}
